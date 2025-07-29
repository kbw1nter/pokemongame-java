package jogo;

import modelo.Celula;
import modelo.Pokemon;
import modelo.Treinador;
import utils.PokemonFactory;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MotorJogo extends Observado {
    public static final int TAMANHO_GRID = 8;

    private Celula[][] tabuleiro;
    private Treinador jogador;
    private Treinador computador;
    private ExecutorService executorComputador;
    private final Random random = new Random();

    public MotorJogo() {
        this.executorComputador = Executors.newSingleThreadExecutor();
        this.tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j] = new Celula(i, j);
            }
        }
    }

    public void iniciarNovoJogo() {
        limparTabuleiro();
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");

        jogador.capturarPokemon(PokemonFactory.criarPokemon("elétrico", "Pikachu", 5, 12));
        computador.capturarPokemon(PokemonFactory.criarPokemon("água", "Squirtle", 5, 10));

        posicionarPokemonsSelvagens();

        notificarObservadores("JOGO_INICIADO", null);
        atualizarStatus();
    }

    private void limparTabuleiro() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j].esvaziar();
            }
        }
    }

    private void posicionarPokemonsSelvagens() {
        List<Pokemon> selvagens = new ArrayList<>();
        selvagens.add(PokemonFactory.criarPokemon("terra", "Sandshrew", 3, 8));
        selvagens.add(PokemonFactory.criarPokemon("floresta", "Caterpie", 2, 5));
        selvagens.add(PokemonFactory.criarPokemon("água", "Magikarp", 1, 2));
        selvagens.add(PokemonFactory.criarPokemon("elétrico", "Voltorb", 4, 8));

        for (Pokemon p : selvagens) {
            boolean posicionado = false;
            while (!posicionado) {
                int x = random.nextInt(TAMANHO_GRID);
                int y = random.nextInt(TAMANHO_GRID);
                if (tabuleiro[x][y].estaVazia() && ehRegiaoValida(p.getTipo(), x, y)) {
                    tabuleiro[x][y].setPokemon(p);
                    posicionado = true;
                }
            }
        }
    }

    private boolean ehRegiaoValida(String tipo, int x, int y) {
        int meio = TAMANHO_GRID / 2;
        switch (tipo.toLowerCase()) {
            case "água": return x < meio && y < meio;
            case "floresta": return x < meio && y >= meio;
            case "terra": return x >= meio && y < meio;
            case "elétrico": return x >= meio && y >= meio;
            default: return false;
        }
    }

    public void realizarJogadaJogador(int x, int y) {
        Celula celulaClicada = tabuleiro[x][y];
        notificarObservadores("CELULA_REVELADA", celulaClicada);

        if (celulaClicada.estaVazia()) {
            notificarObservadores("MENSAGEM", "A célula está vazia. Nada aconteceu.");
        } else {
            Pokemon pokemonEncontrado = celulaClicada.getPokemon();
            notificarObservadores("MENSAGEM", "Você encontrou um " + pokemonEncontrado.getNome() + " selvagem!");

            boolean capturou = random.nextBoolean();
            if (capturou) {
                notificarObservadores("MENSAGEM", "Você capturou o " + pokemonEncontrado.getNome() + "!");
                jogador.capturarPokemon(pokemonEncontrado);
                celulaClicada.esvaziar();
            } else {
                moverPokemonParaOutraCelula(pokemonEncontrado, celulaClicada);
            }
        }

        atualizarStatus();

        if (!jogoTerminou()) {
            realizarJogadaComputador();
        } else {
            verificarFimDeJogo();
        }
    }

    private void moverPokemonParaOutraCelula(Pokemon pokemon, Celula origem) {
        origem.esvaziar();
        List<Celula> celulasVaziasValidas = new ArrayList<>();
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (tabuleiro[i][j].estaVazia() && ehRegiaoValida(pokemon.getTipo(), i, j)) {
                    celulasVaziasValidas.add(tabuleiro[i][j]);
                }
            }
        }
        if (!celulasVaziasValidas.isEmpty()) {
            Celula novaCelula = celulasVaziasValidas.get(random.nextInt(celulasVaziasValidas.size()));
            novaCelula.setPokemon(pokemon);
            notificarObservadores("MENSAGEM", "O " + pokemon.getNome() + " fugiu para outro local!");
        } else {
            notificarObservadores("MENSAGEM", "O " + pokemon.getNome() + " fugiu e não foi mais visto!");
        }
    }

    private void realizarJogadaComputador() {
        executorComputador.submit(() -> {
            try {
                notificarObservadores("MENSAGEM", "Computador está se preparando...");
                Thread.sleep(2500);
                notificarObservadores("MENSAGEM", "O Computador te desafiou para uma batalha!");
                SwingUtilities.invokeLater(this::iniciarBatalha);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void iniciarBatalha() {
        Pokemon pJogador = jogador.getPokemonPrincipal();
        Pokemon pComputador = computador.getPokemonPrincipal();

        if (pJogador == null || pComputador == null) return;

        notificarObservadores("MENSAGEM", "\n--- BATALHA INICIADA ---");
        while (!pJogador.estaNocauteado() && !pComputador.estaNocauteado()) {
            pJogador.atacar(pComputador);
            notificarObservadores("MENSAGEM", pJogador.getNome() + " atacou! HP de " + pComputador.getNome() + ": " + pComputador.getEnergia());
            if (pComputador.estaNocauteado()) break;
            pComputador.atacar(pJogador);
            notificarObservadores("MENSAGEM", pComputador.getNome() + " atacou! HP de " + pJogador.getNome() + ": " + pJogador.getEnergia());
        }

        if (pJogador.estaNocauteado()) {
            notificarObservadores("MENSAGEM", computador.getNome() + " venceu a batalha!");
            computador.adicionarPontos(100);
        } else {
            notificarObservadores("MENSAGEM", jogador.getNome() + " venceu a batalha!");
            jogador.adicionarPontos(100);
        }
        pJogador.restaurarEnergia();
        pComputador.restaurarEnergia();
        notificarObservadores("MENSAGEM", "Energia dos Pokémons restaurada.");
        notificarObservadores("MENSAGEM", "--- FIM DA BATALHA ---\n");
        atualizarStatus();
    }

    private void atualizarStatus() {
        notificarObservadores("ATUALIZAR_STATUS", new Treinador[]{jogador, computador});
    }

    private boolean jogoTerminou() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia()) return false;
            }
        }
        return true;
    }

    private void verificarFimDeJogo() {
        if (jogoTerminou()) {
            String vencedor = jogador.getPontuacao() > computador.getPontuacao() ? jogador.getNome() : computador.getNome();
            notificarObservadores("FIM_DE_JOGO", "O vencedor é: " + vencedor);
            executorComputador.shutdown();
        }
    }
}
