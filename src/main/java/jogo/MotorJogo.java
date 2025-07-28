package jogo;

import modelo.Treinador;
import modelo.Pokemon;
import utils.PokemonFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MotorJogo extends Observado {
    private Treinador jogador;
    private Treinador computador;
    private ExecutorService executorComputador; // para jogada do computador

    public MotorJogo() {
        this.executorComputador = Executors.newSingleThreadExecutor();
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");

        // Aaiciona um pokemon inicial para cada treinador
        jogador.capturarPokemon(PokemonFactory.criarPokemon("elétrico", "Pikachu", 5, 12));
        computador.capturarPokemon(PokemonFactory.criarPokemon("água", "Squirtle", 5, 10));

        notificarObservadores("JOGO_INICIADO", null);
        atualizarStatus();
    }

    public void realizarJogadaJogador(int x, int y) {
        // lógica da jogada do jogador (ex: procurar pokémon na célula x,y)
        System.out.println("Jogador clicou na célula: " + x + ", " + y);
        notificarObservadores("MENSAGEM", "Você procurou por um Pokémon...");

        // simula uma batalha se encontrar algo
        batalhar();

        // após a jogada do jogador, aciona a jogada do computador
        if (!jogoTerminou()) {
            realizarJogadaComputador();
        }
    }

    private void realizarJogadaComputador() {
        executorComputador.submit(() -> {
            try {
                notificarObservadores("MENSAGEM", "Computador está pensando...");
                Thread.sleep(2000); // simula o "tempo de pensar"

                // lógica da jogada do computador
                System.out.println("Computador realizou sua jogada.");
                notificarObservadores("MENSAGEM", "Computador atacou!");
                batalhar(); // simula a batalha de volta

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void batalhar() {
        Pokemon pJogador = jogador.getPokemonPrincipal();
        Pokemon pComputador = computador.getPokemonPrincipal();

        if (pJogador != null && pComputador != null) {
            pJogador.atacar(pComputador);
            if (pComputador.estaNocauteado()) {
                notificarObservadores("MENSAGEM", "Você venceu a batalha!");
                jogador.adicionarPontos(100);
                // lógica para remover o pokemon do computador ou trocar
            } else {
                pComputador.atacar(pJogador);
                if (pJogador.estaNocauteado()) {
                    notificarObservadores("MENSAGEM", "Você foi derrotado!");
                    computador.adicionarPontos(100);
                }
            }
        }
        atualizarStatus();
        verificarFimDeJogo();
    }

    private void atualizarStatus() {
        notificarObservadores("ATUALIZAR_STATUS", new Treinador[]{jogador, computador});
    }

    private boolean jogoTerminou() {
        // condição de término (ex: um dos treinadores não tem mais pokémons)
        return jogador.getTime().isEmpty() || computador.getTime().stream().allMatch(Pokemon::estaNocauteado);
    }

    private void verificarFimDeJogo() {
        if (jogoTerminou()) {
            String vencedor = jogador.getPontuacao() > computador.getPontuacao() ? jogador.getNome() : computador.getNome();
            notificarObservadores("FIM_DE_JOGO", "O vencedor é: " + vencedor);
            executorComputador.shutdown();
        }
    }
}
