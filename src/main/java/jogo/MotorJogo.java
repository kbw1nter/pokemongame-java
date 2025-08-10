package jogo;

import modelo.*;
import estrategia.*;
import excecoes.RegiaoInvalidaException;
import utils.PokemonFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MotorJogo extends Observado implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int TAMANHO_GRID = 8;
    private Celula[][] tabuleiro;
    private Treinador jogador;
    private Treinador computador;
    private Random random;
    private int turno;
    private transient ExecutorService executorService;
    private transient Future<?> computadorTask;

    public MotorJogo() {
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        random = new Random();
        turno = 0;
        inicializarTabuleiro();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void inicializarTabuleiro() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                tabuleiro[i][j] = new Celula(i, j);
            }
        }
        distribuirPokemonsSelvagens();
    }

    private void distribuirPokemonsSelvagens() {
        int numPokemons = (int) (TAMANHO_GRID * TAMANHO_GRID * 0.2); // 20% das células terão Pokémons
        for (int k = 0; k < numPokemons; k++) {
            int x, y;
            do {
                x = random.nextInt(TAMANHO_GRID);
                y = random.nextInt(TAMANHO_GRID);
            } while (!tabuleiro[x][y].estaVazia()); // Garante que a célula esteja vazia

            Pokemon pokemon = PokemonFactory.criarPokemonAleatorio();
            tabuleiro[x][y].setPokemon(pokemon);
            super.notificarObservadores("MENSAGEM", "Um " + pokemon.getNome() + " selvagem apareceu em (" + x + ", " + y + ")!");
        }
    }

    public Celula[][] getTabuleiro() {
        return tabuleiro;
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        turno = 0;
        inicializarTabuleiro();
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{jogador.getStatus(), computador.getStatus()});
        super.notificarObservadores("MENSAGEM", "Novo jogo iniciado!");
        atualizarTabuleiroUI();
    }

    public void jogar(int x, int y) {
        if (computadorTask != null && !computadorTask.isDone()) {
            super.notificarObservadores("MENSAGEM", "Aguarde o computador terminar o turno dele.");
            return;
        }

        super.notificarObservadores("MENSAGEM", "\n--- Turno do Jogador ---");
        processarTurno(jogador, x, y);

        if (jogador.getPontuacao() >= 500) {
            super.notificarObservadores("MENSAGEM", "Jogador venceu o jogo!");
            return;
        }

        turno++;
        super.notificarObservadores("MENSAGEM", "\n--- Turno do Computador ---");
        computadorTask = executorService.submit(() -> {
            try {
                Thread.sleep(1000); // Simula o "pensar" do computador
                processarTurnoComputador();
                if (computador.getPontuacao() >= 500) {
                    super.notificarObservadores("MENSAGEM", "Computador venceu o jogo!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                super.notificarObservadores("MENSAGEM", "Turno do computador interrompido.");
            }
        });
    }

    private void processarTurno(Treinador treinador, int x, int y) {
        if (x < 0 || x >= TAMANHO_GRID || y < 0 || y >= TAMANHO_GRID) {
            super.notificarObservadores("MENSAGEM", "Coordenadas inválidas. Tente novamente.");
            return;
        }

        Celula celula = tabuleiro[x][y];

        if (celula.estaVazia()) {
            super.notificarObservadores("MENSAGEM", treinador.getNome() + " encontrou uma célula vazia. Nada encontrado.");
            celula.setVisitada(true);
            // Notifica a UI para desabilitar o botão da célula vazia
            super.notificarObservadores("CELULA_VAZIA", new int[]{x, y});
        } else {
            Pokemon pokemonSelvagem = celula.getPokemon();
            super.notificarObservadores("MENSAGEM", treinador.getNome() + " encontrou um " + pokemonSelvagem.getNome() + " selvagem!");

            if (treinador.getPokemonAtual() == null) {
                treinador.setPokemonAtual(pokemonSelvagem);
                celula.setPokemon(null); // Remove o Pokémon da célula
                treinador.adicionarPontos(50); // Pontos por encontrar o primeiro Pokémon
                super.notificarObservadores("MENSAGEM", treinador.getNome() + " capturou " + pokemonSelvagem.getNome() + "!");
                super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y}); // Notifica a UI para mostrar a imagem
            } else {
                super.notificarObservadores("MENSAGEM", treinador.getNome() + " já tem um " + treinador.getPokemonAtual().getNome() + ". Batalha iniciada!");
                batalhar(treinador.getPokemonAtual(), pokemonSelvagem);
                if (pokemonSelvagem.getPontosVida() <= 0) {
                    celula.setPokemon(null); // Remove o Pokémon da célula se for derrotado
                    super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y}); // Notifica a UI para limpar a célula
                }
            }
            celula.setVisitada(true);
        }
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{jogador.getStatus(), computador.getStatus()});
    }

    private void processarTurnoComputador() {
        int x, y;
        Celula celula;
        boolean encontrouPokemon = false;
        // Tenta encontrar um Pokémon selvagem não visitado
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia() && !tabuleiro[i][j].foiVisitada()) {
                    x = i;
                    y = j;
                    processarTurno(computador, x, y);
                    encontrouPokemon = true;
                    return;
                }
            }
        }

        // Se não encontrou Pokémon, escolhe uma célula aleatória não visitada
        if (!encontrouPokemon) {
            do {
                x = random.nextInt(TAMANHO_GRID);
                y = random.nextInt(TAMANHO_GRID);
                celula = tabuleiro[x][y];
            } while (celula.foiVisitada());
            processarTurno(computador, x, y);
        }
    }

    private void batalhar(Pokemon p1, Pokemon p2) {
        super.notificarObservadores("MENSAGEM", "--- BATALHA INICIADA ---");

        if (p1 == null || p2 == null) {
            super.notificarObservadores("MENSAGEM", "Um dos Pokémons para batalha é nulo.");
            return;
        }

        while (!p1.estaNocauteado() && !p2.estaNocauteado()) {
            // Turno do P1
            if (!p1.estaNocauteado()) {
                p1.atacar(p2);
                super.notificarObservadores("MENSAGEM", p1.getNome() + " HP: " + p1.getPontosVida() + " | " + p2.getNome() + " HP: " + p2.getPontosVida());
            }
            // Turno do P2
            if (!p2.estaNocauteado()) {
                p2.atacar(p1);
                super.notificarObservadores("MENSAGEM", p2.getNome() + " HP: " + p2.getPontosVida() + " | " + p1.getNome() + " HP: " + p1.getPontosVida());
            }
        }

        String vencedor = "Ninguém";
        if (p1.estaNocauteado() && p2.estaNocauteado()) {
            super.notificarObservadores("MENSAGEM", "Ambos os Pokémons foram nocauteados! Empate.");
        } else if (p1.estaNocauteado()) {
            vencedor = p2.getNome();
            super.notificarObservadores("MENSAGEM", p2.getNome() + " venceu a batalha!");
            if (p2 == jogador.getPokemonAtual()) { // Se o vencedor for o Pokémon do jogador
                jogador.adicionarPontos(50);
            } else if (p2 == computador.getPokemonAtual()) { // Se o vencedor for o Pokémon do computador
                computador.adicionarPontos(50);
            }
        } else {
            vencedor = p1.getNome();
            super.notificarObservadores("MENSAGEM", p1.getNome() + " venceu a batalha!");
            if (p1 == jogador.getPokemonAtual()) { // Se o vencedor for o Pokémon do jogador
                jogador.adicionarPontos(50);
            } else if (p1 == computador.getPokemonAtual()) { // Se o vencedor for o Pokémon do computador
                computador.adicionarPontos(50);
            }
        }

        p1.restaurarEnergia();
        p2.restaurarEnergia();
        super.notificarObservadores("MENSAGEM", "Energia dos Pokémons restaurada.");
        super.notificarObservadores("MENSAGEM", "--- FIM DA BATALHA ---");

        atualizarStatus();

        // Verifica condição de fim de jogo (exemplo: se todos os Pokémons selvagens foram capturados)
        if (todosPokemonsSelvagensCapturados()) {
            super.notificarObservadores("FIM_DE_JOGO", "Todos os Pokémons selvagens foram capturados!");
            determinarVencedorFinal();
            executorService.shutdown();
        }
    }

    private boolean todosPokemonsSelvagensCapturados() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia() && tabuleiro[i][j].getPokemon().isSelvagem()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void determinarVencedorFinal() {
        if (jogador.getPontuacao() > computador.getPontuacao()) {
            super.notificarObservadores("MENSAGEM", "FIM DE JOGO! O Jogador venceu com " + jogador.getPontuacao() + " pontos!");
        } else if (computador.getPontuacao() > jogador.getPontuacao()) {
            super.notificarObservadores("MENSAGEM", "FIM DE JOGO! O Computador venceu com " + computador.getPontuacao() + " pontos!");
        } else {
            super.notificarObservadores("MENSAGEM", "FIM DE JOGO! Empate!");
        }
    }

    private void atualizarStatus() {
        String statusJogador = jogador.getStatus();
        String statusComputador = computador.getStatus();
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{statusJogador, statusComputador});
    }

    public void salvarJogo(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
            super.notificarObservadores("MENSAGEM", "Jogo salvo com sucesso em " + filePath);
        } catch (IOException e) {
            super.notificarObservadores("MENSAGEM", "Erro ao salvar o jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void carregarJogo(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            MotorJogo jogoCarregado = (MotorJogo) ois.readObject();
            this.tabuleiro = jogoCarregado.tabuleiro;
            this.jogador = jogoCarregado.jogador;
            this.computador = jogoCarregado.computador;
            this.turno = jogoCarregado.turno;
            this.random = jogoCarregado.random; // Garante que o Random seja restaurado

            // Reinicializa o executor de threads após a desserialização
            if (this.executorService != null) {
                this.executorService.shutdownNow();
            }
            this.executorService = Executors.newSingleThreadExecutor();

            super.notificarObservadores("MENSAGEM", "Jogo carregado com sucesso de " + filePath);
            super.notificarObservadores("STATUS_ATUALIZADO", new String[]{jogador.getStatus(), computador.getStatus()});
            atualizarTabuleiroUI();
        } catch (IOException | ClassNotFoundException e) {
            super.notificarObservadores("MENSAGEM", "Erro ao carregar o jogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void atualizarTabuleiroUI() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia()) {
                    super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{i, j});
                } else if (tabuleiro[i][j].foiVisitada()) {
                    super.notificarObservadores("CELULA_VAZIA", new int[]{i, j});
                } else {
                    // Célula não visitada e vazia, garantir que não tenha imagem
                    super.notificarObservadores("CELULA_VAZIA", new int[]{i, j}); // Reutiliza o evento para limpar a célula
                }
            }
        }
    }

    // Garante que o executor de threads seja desligado ao finalizar o jogo
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}