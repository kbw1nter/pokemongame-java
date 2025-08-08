package jogo;

import utils.PokemonFactory;
import modelo.*;
import ui.Observador;
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
    private ExecutorService executorComputador; // Para a jogada do computador em outra thread

    public MotorJogo() {
        this.executorComputador = Executors.newSingleThreadExecutor();
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        inicializarTabuleiro();
        distribuirPokemonsIniciais();
        atualizarStatus();
        notificarObservadores("MENSAGEM", "Novo jogo iniciado!");
    }

    private void inicializarTabuleiro() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j] = new Celula(i, j);
            }
        }
        distribuirPokemonsSelvagens();
    }

    private void distribuirPokemonsIniciais() {
        //Pikachu para o jogador e um Squirtle para o computador
        jogador.capturarPokemon(PokemonFactory.criarPokemon("Elétrico", "Pikachu", 10, 20));
        computador.capturarPokemon(PokemonFactory.criarPokemon("Água", "Squirtle", 10, 20));
    }

    private void distribuirPokemonsSelvagens() {
        Random random = new Random();
        int numPokemonsSelvagens = TAMANHO_GRID * TAMANHO_GRID / 4; // Ex: 16 Pokémons para um grid 8x8

        for (int k = 0; k < numPokemonsSelvagens; k++) {
            int x = random.nextInt(TAMANHO_GRID);
            int y = random.nextInt(TAMANHO_GRID);

            if (tabuleiro[x][y].estaVazia()) {
                String tipo = determinarTipoPorRegiao(x, y);
                // Mude esta linha para usar o novo método da Factory
                Pokemon selvagem = PokemonFactory.criarPokemonSelvagem(tipo, 5, 15);
                selvagem.setSelvagem(true);
                tabuleiro[x][y].setPokemon(selvagem);
            } else {
                k--; // Tenta novamente se a célula já estiver ocupada
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

                // Lógica de captura
                if (new Random().nextBoolean()) { // 50% de chance de capturar
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
        tabuleiro[oldX][oldY].esvaziar(); // Remove da posição antiga
        notificarObservadores("POKEMON_ESCAPOU_MOVER", new int[]{oldX, oldY}); // Notifica a UI para limpar o botão

        Random random = new Random();
        int newX, newY;
        do {
            newX = random.nextInt(TAMANHO_GRID);
            newY = random.nextInt(TAMANHO_GRID);
        } while (!tabuleiro[newX][newY].estaVazia()); // Encontra uma nova célula vazia

        tabuleiro[newX][newY].setPokemon(pokemon); // Coloca o Pokémon na nova posição
        notificarObservadores("MENSAGEM", pokemon.getNome() + " moveu-se para (" + newX + ", " + newY + ").");
    }

    private void iniciarBatalha(Treinador t1, Treinador t2) {
        notificarObservadores("MENSAGEM", "--- BATALHA INICIADA ---");
        Pokemon p1 = t1.getPokemonPrincipal();
        Pokemon p2 = t2.getPokemonPrincipal();

        if (p1 == null || p2 == null) {
            notificarObservadores("MENSAGEM", "Um dos treinadores não tem Pokémon para batalhar!");
            return;
        }

        notificarObservadores("MENSAGEM", t1.getNome() + " vs " + t2.getNome() + "! " + p1.getNome() + " (HP: " + p1.getEnergia() + ") vs " + p2.getNome() + " (HP: " + p2.getEnergia() + ")");

        // Loop de batalha
        while (!p1.estaNocauteado() && !p2.estaNocauteado()) {
            p1.atacar(p2);
            if (p2.estaNocauteado()) break;
            p2.atacar(p1);
        }

        String vencedorBatalha;
        if (p1.estaNocauteado()) {
            vencedorBatalha = t2.getNome();
            t2.adicionarPontos(100);
        } else {
            vencedorBatalha = t1.getNome();
            t1.adicionarPontos(100);
        }

        notificarObservadores("MENSAGEM", vencedorBatalha + " venceu a batalha!");
        p1.restaurarEnergia();
        p2.restaurarEnergia();
        notificarObservadores("MENSAGEM", "Energia dos Pokémons restaurada.");
        notificarObservadores("MENSAGEM", "--- FIM DA BATALHA ---");
        atualizarStatus();

        // lógica para a jogada do computador após a batalha
        executorComputador.submit(() -> {
            try {
                Thread.sleep(2000); // simula o tempo de "pensamento" do computador
                notificarObservadores("MENSAGEM", "Computador está se preparando...");
                // Aqui da pra adicionar a lógica de jogada do computador, tipo fazer ele clicar em uma celula aleatória
                Random rand = new Random();
                int compX = rand.nextInt(TAMANHO_GRID);
                int compY = rand.nextInt(TAMANHO_GRID);
                notificarObservadores("MENSAGEM", "O Computador te desafiou para uma batalha!"); // Simula a batalha de volta

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void atualizarStatus() {
        String statusJogador = String.format("Jogador: %d pts | Pokémon: %s (HP: %d)",
                jogador.getPontuacao(),
                jogador.getPokemonPrincipal() != null ? jogador.getPokemonPrincipal().getNome() : "Nenhum",
                jogador.getPokemonPrincipal() != null ? jogador.getPokemonPrincipal().getEnergia() : 0);

        String statusComputador = String.format("Computador: %d pts | Pokémon: %s (HP: %d)",
                computador.getPontuacao(),
                computador.getPokemonPrincipal() != null ? computador.getPokemonPrincipal().getNome() : "Nenhum",
                computador.getPokemonPrincipal() != null ? computador.getPokemonPrincipal().getEnergia() : 0);

        notificarObservadores("STATUS_JOGADOR", statusJogador);
        notificarObservadores("STATUS_COMPUTADOR", statusComputador);

        // verifica condição de fim de jogo
        if (jogador.getTime().isEmpty() || computador.getTime().isEmpty()) {
            String vencedor = jogador.getTime().isEmpty() ? computador.getNome() : jogador.getNome();
            notificarObservadores("FIM_DE_JOGO", "O vencedor é: " + vencedor);
            executorComputador.shutdown();
        }
    }

    // métodos de persistência (salvar/carregar)
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

    // getter para o tabuleiro (necessário para JanelaPrincipal)
    public Celula[][] getTabuleiro() {
        return tabuleiro;
    }
}