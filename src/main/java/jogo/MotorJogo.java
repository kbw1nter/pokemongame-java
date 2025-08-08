package jogo;

import modelo.*;
import ui.Observador;
import utils.PokemonFactory; // ADICIONADO
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;

public class MotorJogo extends Observado {
    public static final int TAMANHO_GRID = 8;
    private Treinador jogador;
    private Treinador computador;
    private Celula[][] tabuleiro;
    private final ExecutorService executorComputador; // Adicionado final

    public MotorJogo() {
        this.executorComputador = Executors.newSingleThreadExecutor();
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        inicializarTabuleiro();
        distribuirPokemonsIniciais();
        distribuirPokemonsSelvagens(); // Chamada para distribuir Pokémons selvagens
        atualizarStatus();
        notificarObservadores("MENSAGEM", "Novo jogo iniciado!");
    }

    private void inicializarTabuleiro() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j] = new Celula(i, j);
            }
        }
    }

    private void distribuirPokemonsIniciais() {
        // Jogador
        Pokemon pikachu = PokemonFactory.criarPokemon("Elétrico", "Pikachu", 10, 20);
        jogador.capturarPokemon(pikachu);

        // Computador
        Pokemon squirtle = PokemonFactory.criarPokemon("Água", "Squirtle", 10, 20);
        computador.capturarPokemon(squirtle);
    }

    private void distribuirPokemonsSelvagens() {
        Random random = new Random();
        int numPokemonsSelvagens = TAMANHO_GRID * TAMANHO_GRID / 4;

        for (int k = 0; k < numPokemonsSelvagens; k++) {
            int x = random.nextInt(TAMANHO_GRID);
            int y = random.nextInt(TAMANHO_GRID);

            if (tabuleiro[x][y].estaVazia()) {
                String tipo = determinarTipoPorRegiao(x, y);
                Pokemon selvagem = PokemonFactory.criarPokemonSelvagem(tipo, 5, 15);
                selvagem.setSelvagem(true);
                tabuleiro[x][y].setPokemon(selvagem);
            } else {
                k--;
            }
        }
    }

    private String determinarTipoPorRegiao(int x, int y) {
        int meio = TAMANHO_GRID / 2;
        if (x < meio && y < meio) {
            return "Água";
        } else if (x < meio && y >= meio) {
            return "Floresta";
        } else if (x >= meio && y < meio) {
            return "Terra";
        } else {
            return "Elétrico";
        }
    }

    public void jogar(int x, int y) {
        Celula celulaClicada = tabuleiro[x][y];

        if (celulaClicada.estaVazia()) {
            notificarObservadores("MENSAGEM", "A célula está vazia. Nada aconteceu.");
        } else {
            Pokemon pokemonNaCelula = celulaClicada.getPokemon();
            if (pokemonNaCelula.isSelvagem()) {
                notificarObservadores("MENSAGEM", "Você encontrou um Pokémon selvagem: " + pokemonNaCelula.getNome() + "!");
                notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});

                // Lógica de captura (simplificada para o exemplo)
                Random rand = new Random();
                if (rand.nextDouble() < 0.7) { // 70% de chance de capturar
                    jogador.capturarPokemon(pokemonNaCelula);
                    celulaClicada.esvaziar(); // Remove o Pokémon da célula
                    notificarObservadores("MENSAGEM", jogador.getNome() + " capturou " + pokemonNaCelula.getNome() + "!");
                    atualizarStatus();
                } else {
                    notificarObservadores("MENSAGEM", pokemonNaCelula.getNome() + " escapou!");
                    moverPokemonQueEscapou(pokemonNaCelula, x, y);
                }
            } else { // Se não for selvagem, é do outro treinador
                notificarObservadores("MENSAGEM", "Você encontrou um Pokémon do oponente: " + pokemonNaCelula.getNome() + "!");
                iniciarBatalha(jogador, computador);
            }
        }
    }

    private void moverPokemonQueEscapou(Pokemon pokemon, int oldX, int oldY) {
        Random random = new Random();
        int newX, newY;
        int tentativas = 0;
        do {
            newX = random.nextInt(TAMANHO_GRID);
            newY = random.nextInt(TAMANHO_GRID);
            tentativas++;
            if (tentativas > 100) { // Evita loop infinito em tabuleiro cheio
                notificarObservadores("MENSAGEM", pokemon.getNome() + " não conseguiu se mover para outro lugar.");
                return;
            }
        } while (!tabuleiro[newX][newY].estaVazia());

        tabuleiro[newX][newY].setPokemon(pokemon);
        notificarObservadores("MENSAGEM", pokemon.getNome() + " moveu-se para (" + newX + ", " + newY + ").");
        // Notifica a UI para limpar a célula antiga, se necessário
        notificarObservadores("CELULA_ATUALIZADA", new int[]{oldX, oldY});
    }

    private void iniciarBatalha(Treinador t1, Treinador t2) {
        notificarObservadores("MENSAGEM", "--- BATALHA INICIADA ---");
        Pokemon p1 = t1.getPokemonPrincipal();
        Pokemon p2 = t2.getPokemonPrincipal();

        if (p1 == null || p2 == null) {
            notificarObservadores("MENSAGEM", "Um dos treinadores não tem Pokémon para batalhar.");
            return;
        }

        while (!p1.estaNocauteado() && !p2.estaNocauteado()) {
            // Turno do P1
            if (!p1.estaNocauteado()) {
                p1.atacar(p2);
            }
            // Turno do P2
            if (!p2.estaNocauteado()) {
                p2.atacar(p1);
            }
        }

        String vencedor = "Ninguém";
        if (p1.estaNocauteado() && p2.estaNocauteado()) {
            notificarObservadores("MENSAGEM", "Ambos os Pokémons foram nocauteados! Empate.");
        } else if (p1.estaNocauteado()) {
            vencedor = t2.getNome();
            notificarObservadores("MENSAGEM", t2.getNome() + " venceu a batalha!");
        } else {
            vencedor = t1.getNome();
            notificarObservadores("MENSAGEM", t1.getNome() + " venceu a batalha!");
        }

        p1.restaurarEnergia();
        p2.restaurarEnergia();
        notificarObservadores("MENSAGEM", "Energia dos Pokémons restaurada.");
        notificarObservadores("MENSAGEM", "--- FIM DA BATALHA ---");

        // Lógica de pontuação pós-batalha (exemplo)
        if (vencedor.equals(t1.getNome())) {
            t1.adicionarPontos(50);
        } else if (vencedor.equals(t2.getNome())) {
            t2.adicionarPontos(50);
        }
        atualizarStatus();

        // Verifica condição de fim de jogo (exemplo: se um treinador ficou sem Pokémons)
        if (t1.getTime().isEmpty() || t2.getTime().isEmpty()) {
            vencedor = t1.getTime().isEmpty() ? t2.getNome() : t1.getNome();
            notificarObservadores("FIM_DE_JOGO", "O vencedor é: " + vencedor);
            executorComputador.shutdown();
        }
    }

    private void atualizarStatus() {
        String statusJogador = "Jogador: " + jogador.getPontuacao() + " pts | Pokémon: ";
        if (jogador.getPokemonPrincipal() != null) {
            statusJogador += jogador.getPokemonPrincipal().getNome() + " (HP: " + jogador.getPokemonPrincipal().getEnergia() + ")";
        }

        String statusComputador = "Computador: " + computador.getPontuacao() + " pts | Pokémon: ";
        if (computador.getPokemonPrincipal() != null) {
            statusComputador += computador.getPokemonPrincipal().getNome() + " (HP: " + computador.getPokemonPrincipal().getEnergia() + ")";
        }
        notificarObservadores("STATUS_ATUALIZADO", new String[]{statusJogador, statusComputador});
    }

    public void salvarJogo(String caminhoArquivo) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(this.jogador);
            oos.writeObject(this.computador);
            oos.writeObject(this.tabuleiro);
            notificarObservadores("MENSAGEM", "Jogo salvo com sucesso em: " + caminhoArquivo);
        } catch (IOException e) {
            notificarObservadores("MENSAGEM", "Erro ao salvar o jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void carregarJogo(String caminhoArquivo) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            this.jogador = (Treinador) ois.readObject();
            this.computador = (Treinador) ois.readObject();
            this.tabuleiro = (Celula[][]) ois.readObject();
            notificarObservadores("MENSAGEM", "Jogo carregado com sucesso de: " + caminhoArquivo);
            atualizarStatus();
            notificarObservadores("JOGO_CARREGADO", this.tabuleiro);
        } catch (IOException | ClassNotFoundException e) {
            notificarObservadores("MENSAGEM", "Erro ao carregar o jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Celula[][] getTabuleiro() {
        return tabuleiro;
    }
}
