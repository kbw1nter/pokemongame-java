package jogo;

import modelo.*;
import ui.Observador;
import utils.PokemonFactory;
import excecoes.RegiaoInvalidaException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MotorJogo extends Observado {
    public static final int TAMANHO_GRID = 8;

    private Treinador jogador;
    private Treinador computador;
    private Celula[][] tabuleiro;
    private ExecutorService executorComputador;
    private Random random;

    public MotorJogo() {
        this.executorComputador = Executors.newSingleThreadExecutor();
        this.random = new Random();
        inicializarTabuleiro();
    }

    // O método notificarObservadores já está na classe Observado e é chamado por super.notificarObservadores

    private void inicializarTabuleiro() {
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j] = new Celula(i, j);
            }
        }
        distribuirPokemonsSelvagens();
    }

    private void distribuirPokemonsSelvagens() {
        int numPokemonsSelvagens = 10; // Número de Pokémons selvagens no tabuleiro

        for (int i = 0; i < numPokemonsSelvagens; i++) {
            int x, y;
            do {
                x = random.nextInt(TAMANHO_GRID);
                y = random.nextInt(TAMANHO_GRID);
            } while (!tabuleiro[x][y].estaVazia());

            // Determinar o tipo baseado na região
            String tipo = determinarTipoRegiao(x, y);
            Pokemon pokemonSelvagem = PokemonFactory.criarPokemonSelvagem(tipo,
                    random.nextInt(5) + 1, random.nextInt(10) + 5);

            tabuleiro[x][y].setPokemon(pokemonSelvagem);
        }
    }

    private String determinarTipoRegiao(int x, int y) {
        int meio = TAMANHO_GRID / 2;
        if (x < meio && y < meio) {
            return "água";
        } else if (x < meio && y >= meio) {
            return "floresta";
        } else if (x >= meio && y < meio) {
            return "terra";
        } else {
            return "elétrico";
        }
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");

        // Pokémons iniciais
        jogador.capturarPokemon(PokemonFactory.criarPokemon("elétrico", "pikachu", 5, 12));
        computador.capturarPokemon(PokemonFactory.criarPokemon("água", "squirtle", 5, 10));

        super.notificarObservadores("MENSAGEM", "Novo jogo iniciado!");
        atualizarStatus();
    }

    public void jogar(int x, int y) {
        if (x < 0 || x >= TAMANHO_GRID || y < 0 || y >= TAMANHO_GRID) {
            super.notificarObservadores("MENSAGEM", "Posição inválida!");
            return;
        }

        Celula celula = tabuleiro[x][y];

        if (!celula.estaVazia()) {
            Pokemon pokemonEncontrado = celula.getPokemon();
            super.notificarObservadores("MENSAGEM", "Você encontrou um " + pokemonEncontrado.getNome() + "!");

            // Notificar a UI para mostrar o Pokémon
            super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});

            // Tentar capturar o Pokémon
            if (tentarCaptura(pokemonEncontrado)) {
                jogador.capturarPokemon(pokemonEncontrado);
                celula.esvaziar();
                super.notificarObservadores("MENSAGEM", "Você capturou " + pokemonEncontrado.getNome() + "!");
                jogador.adicionarPontos(50);
            } else {
                // Batalha
                batalhar(jogador.getPokemonPrincipal(), pokemonEncontrado);
                if (pokemonEncontrado.estaNocauteado()) {
                    celula.esvaziar();
                    jogador.adicionarPontos(100);
                }
            }
        } else {
            super.notificarObservadores("MENSAGEM", "Célula vazia. Nada encontrado.");
        }

        atualizarStatus();

        // Jogada do computador
        if (!jogoTerminou()) {
            realizarJogadaComputador();
        }
    }

    private boolean tentarCaptura(Pokemon pokemon) {
        // Lógica simples de captura: 30% de chance
        return random.nextDouble() < 0.3;
    }

    private void batalhar(Pokemon atacante, Pokemon defensor) {
        if (atacante != null && defensor != null) {
            atacante.atacar(defensor);
            super.notificarObservadores("MENSAGEM", atacante.getNome() + " atacou " + defensor.getNome());

            if (!defensor.estaNocauteado()) {
                defensor.atacar(atacante);
                super.notificarObservadores("MENSAGEM", defensor.getNome() + " contra-atacou!");
            }
        }
    }

    private void realizarJogadaComputador() {
        executorComputador.submit(() -> {
            try {
                super.notificarObservadores("MENSAGEM", "Computador está pensando...");
                Thread.sleep(2000); // Simula o "tempo de pensar"

                // Lógica simples: escolher uma posição aleatória
                int x, y;
                do {
                    x = random.nextInt(TAMANHO_GRID);
                    y = random.nextInt(TAMANHO_GRID);
                } while (tabuleiro[x][y].estaVazia());

                Celula celula = tabuleiro[x][y];
                Pokemon pokemonEncontrado = celula.getPokemon();

                super.notificarObservadores("MENSAGEM", "Computador encontrou um " + pokemonEncontrado.getNome() + "!");
                super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});

                if (tentarCaptura(pokemonEncontrado)) {
                    computador.capturarPokemon(pokemonEncontrado);
                    celula.esvaziar();
                    super.notificarObservadores("MENSAGEM", "Computador capturou " + pokemonEncontrado.getNome() + "!");
                    computador.adicionarPontos(50);
                } else {
                    batalhar(computador.getPokemonPrincipal(), pokemonEncontrado);
                    if (pokemonEncontrado.estaNocauteado()) {
                        celula.esvaziar();
                        computador.adicionarPontos(100);
                    }
                }

                atualizarStatus();
                verificarFimDeJogo();

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

        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{statusJogador, statusComputador});
    }

    private boolean jogoTerminou() {
        // Verifica se não há mais Pokémons selvagens no tabuleiro
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void verificarFimDeJogo() {
        if (jogoTerminou()) {
            String vencedor = jogador.getPontuacao() > computador.getPontuacao() ?
                    jogador.getNome() : computador.getNome();
            super.notificarObservadores("FIM_DE_JOGO", "O vencedor é: " + vencedor);
            executorComputador.shutdown();
        }
    }

    public void salvarJogo(String caminhoArquivo) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminhoArquivo))) {
            oos.writeObject(jogador);
            oos.writeObject(computador);
            oos.writeObject(tabuleiro);
            super.notificarObservadores("MENSAGEM", "Jogo salvo com sucesso!");
        } catch (IOException e) {
            super.notificarObservadores("MENSAGEM", "Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void carregarJogo(String caminhoArquivo) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminhoArquivo))) {
            jogador = (Treinador) ois.readObject();
            computador = (Treinador) ois.readObject();
            tabuleiro = (Celula[][]) ois.readObject();

            super.notificarObservadores("MENSAGEM", "Jogo carregado com sucesso!");
            super.notificarObservadores("JOGO_CARREGADO", tabuleiro);
            atualizarStatus();
        } catch (IOException | ClassNotFoundException e) {
            super.notificarObservadores("MENSAGEM", "Erro ao carregar o jogo: " + e.getMessage());
        }
    }

    public Celula[][] getTabuleiro() {
        return tabuleiro;
    }

    public Treinador getJogador() {
        return jogador;
    }

    public Treinador getComputador() {
        return computador;
    }
}
