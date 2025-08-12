package jogo;

import modelo.*;
import estrategia.*;
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
    private static final int TEMPO_ESPERA_COMPUTADOR = 1500; // 1.5 segundos
    
    private Celula[][] tabuleiro;
    private Treinador jogador;
    private Treinador computador;
    private Random random;
    private int turno;
    private transient ExecutorService executorService;
    private transient Future<?> computadorTask;
    
    // Campos para controlar o estado de captura
    private transient Pokemon pokemonSelvagemEncontrado;
    private transient int ultimaCelulaX;
    private transient int ultimaCelulaY;
    private transient boolean aguardandoAcaoJogador; // Novo campo para controlar o estado

    public MotorJogo() {
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        random = new Random();
        turno = 0;
        aguardandoAcaoJogador = false;
        
        // Adiciona um Pokémon inicial para cada jogador
        jogador.capturarPokemon(PokemonFactory.criarPokemonInicialJogador());
        computador.capturarPokemon(PokemonFactory.criarPokemonInicialComputador());
        
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

    public Celula[][] getTabuleiro() {
        return tabuleiro;
    }

    public void iniciarNovoJogo() {
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        turno = 0;
        aguardandoAcaoJogador = false;
        pokemonSelvagemEncontrado = null;
        
        // Garante que cada jogador comece com um Pokémon
        jogador.capturarPokemon(PokemonFactory.criarPokemonInicialJogador());
        computador.capturarPokemon(PokemonFactory.criarPokemonInicialComputador());
        
        inicializarTabuleiro();
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{jogador.getStatus(), computador.getStatus()});
        super.notificarObservadores("MENSAGEM", "Novo jogo iniciado!");
        atualizarTabuleiroUI();
    }

    public void jogar(int x, int y) {
        // Verifica se é uma ação especial (captura/fuga)
        if (x < 0 || y < 0) {
            if (aguardandoAcaoJogador && pokemonSelvagemEncontrado != null) {
                tratarAcaoEspecialJogador(x, y);
                aguardandoAcaoJogador = false;
                // Após processar a ação do jogador, inicia o turno do computador
                iniciarTurnoComputador();
            } else {
                super.notificarObservadores("MENSAGEM", "Nenhum Pokémon selvagem para interagir.");
            }
            return;
        }

        // Verifica se o computador ainda está jogando
        if (computadorTask != null && !computadorTask.isDone()) {
            super.notificarObservadores("MENSAGEM", "Aguarde o computador terminar o turno dele.");
            return;
        }

        // Verifica se está aguardando ação do jogador
        if (aguardandoAcaoJogador) {
            super.notificarObservadores("MENSAGEM", "Você precisa decidir o que fazer com o Pokémon selvagem encontrado!");
            return;
        }

        super.notificarObservadores("MENSAGEM", "\n--- Turno do Jogador ---");
        processarTurnoJogador(x, y);
    }

    private void processarTurnoJogador(int x, int y) {
        processarTurno(jogador, x, y);
        
        // Se encontrou um Pokémon selvagem, aguarda ação do jogador
        if (pokemonSelvagemEncontrado != null) {
            aguardandoAcaoJogador = true;
            return; // Não avança o turno ainda
        }
        
        // Verifica vitória do jogador
        if (jogador.getPontuacao() >= 500) {
            super.notificarObservadores("MENSAGEM", "Jogador venceu o jogo!");
            return;
        }

        // Inicia turno do computador
        iniciarTurnoComputador();
    }

    private void iniciarTurnoComputador() {
        turno++;
        super.notificarObservadores("MENSAGEM", "\n--- Turno do Computador ---");
        
        computadorTask = executorService.submit(() -> {
            try {
                Thread.sleep(1000);
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
    
    private void tratarAcaoEspecialJogador(int codigoX, int codigoY) {
        if (pokemonSelvagemEncontrado == null) return;

        if (codigoX == -2 && codigoY == -2) { // Tentar capturar
            if (pokemonSelvagemEncontrado.tentarCapturar()) {
                if (pokemonSelvagemEncontrado.getTreinador() == null) {
                    // Captura bem-sucedida
                    jogador.capturarPokemon(pokemonSelvagemEncontrado);
                    pokemonSelvagemEncontrado.setTreinador(jogador);
                    notificarObservadores("MENSAGEM", "Você capturou " + pokemonSelvagemEncontrado.getNome() + "!");
                    jogador.adicionarPontos(50);
                    tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
                    notificarObservadores("CELULA_VAZIA", new int[]{ultimaCelulaX, ultimaCelulaY});
                }
            } else {
                // Falha na captura
                notificarObservadores("MENSAGEM", "Você falhou ao tentar capturar " + pokemonSelvagemEncontrado.getNome());
                notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
            }
        } 
        else if (codigoX == -3 && codigoY == -3) { // Fugir
            if (pokemonSelvagemEncontrado.tentarFugir()) {
                notificarObservadores("MENSAGEM", "Você conseguiu fugir do Pokémon selvagem!");
                tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
                notificarObservadores("CELULA_VAZIA", new int[]{ultimaCelulaX, ultimaCelulaY});
            } else {
                notificarObservadores("MENSAGEM", "Você não conseguiu fugir do Pokémon selvagem!");
                notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
            }
        }
        
        pokemonSelvagemEncontrado = null;
        atualizarStatus();
        atualizarTabuleiroUI();
    }

    private void tratarAcaoEspecialComputador(int codigoX, int codigoY) {
        if (pokemonSelvagemEncontrado == null) return;

        if (codigoX == -2 && codigoY == -2) { // Tentar capturar
            if (pokemonSelvagemEncontrado.tentarCapturar()) {
                if (pokemonSelvagemEncontrado.getTreinador() == null) {
                    // Captura bem-sucedida
                    computador.capturarPokemon(pokemonSelvagemEncontrado);
                    pokemonSelvagemEncontrado.setTreinador(computador);
                    notificarObservadores("MENSAGEM", "Computador capturou " + pokemonSelvagemEncontrado.getNome() + "!");
                    computador.adicionarPontos(50);
                    tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
                    notificarObservadores("CELULA_VAZIA", new int[]{ultimaCelulaX, ultimaCelulaY});
                }
            } else {
                // Falha na captura
                notificarObservadores("MENSAGEM", "Computador falhou ao tentar capturar " + pokemonSelvagemEncontrado.getNome());
                notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
            }
        }
        
        pokemonSelvagemEncontrado = null;
        atualizarStatus();
        atualizarTabuleiroUI();
    }

    private void processarTurno(Treinador treinador, int x, int y) {
        Celula celula = tabuleiro[x][y];
        celula.setVisitada(true);
        
        if (celula.estaVazia()) {
            String mensagem = treinador == jogador ? 
                "Nada encontrado nesta célula." : 
                "Computador não encontrou nada na célula.";
            notificarObservadores("MENSAGEM", mensagem);
            notificarObservadores("CELULA_VAZIA", new int[]{x, y});
            return;
        }

        Pokemon pokemonEncontrado = celula.getPokemon();
        
        // Se o Pokémon pertence a um treinador (incluindo o jogador)
        if (pokemonEncontrado.getTreinador() != null) {
            // Mostra o Pokémon no tabuleiro
            notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
            
            // Se pertence ao oponente, inicia batalha
            if (!pokemonEncontrado.getTreinador().equals(treinador)) {
                notificarObservadores("BATALHA_INICIADA", 
                    new Pokemon[]{treinador.getPokemonAtual(), pokemonEncontrado});
                batalhar(treinador.getPokemonAtual(), pokemonEncontrado);
            }
            return;
        }
        
        // Se é um Pokémon selvagem
        this.pokemonSelvagemEncontrado = pokemonEncontrado;
        this.ultimaCelulaX = x;
        this.ultimaCelulaY = y;
        
        notificarObservadores("POKEMON_SELVAGEM_ENCONTRADO", pokemonEncontrado);
        notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{x, y});
    }

    private void batalhar(Pokemon pokemonJogador, Pokemon pokemonOponente) {
        notificarObservadores("BATALHA_INICIADA", 
            new Pokemon[]{pokemonJogador, pokemonOponente});
        
        // Turno do jogador
        pokemonJogador.atacar(pokemonOponente);
        notificarObservadores("ATAQUE", 
            new Object[]{pokemonJogador, pokemonOponente, pokemonOponente.getEnergia()});
        
        if (pokemonOponente.estaNocauteado()) {
            notificarObservadores("BATALHA_TERMINADA", pokemonJogador);
            jogador.adicionarPontos(100);
            return;
        }
        
        // Turno do oponente
        pokemonOponente.atacar(pokemonJogador);
        notificarObservadores("ATAQUE", 
            new Object[]{pokemonOponente, pokemonJogador, pokemonJogador.getEnergia()});
        
        if (pokemonJogador.estaNocauteado()) {
            notificarObservadores("BATALHA_TERMINADA", pokemonOponente);
            if (pokemonOponente.getTreinador() == computador) {
                computador.adicionarPontos(100);
            }
            return;
        }
        
        notificarObservadores("BATALHA_CONTINUA", 
            new Pokemon[]{pokemonJogador, pokemonOponente});
    }

    private void atualizarStatus() {
        String statusJogador = jogador.getStatus();
        String statusComputador = computador.getStatus();
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{statusJogador, statusComputador});
    }

    private void atualizarTabuleiroUI() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (!tabuleiro[i][j].estaVazia()) {
                    // Mostra apenas Pokémon que pertencem a treinadores
                    if (tabuleiro[i][j].getPokemon().getTreinador() != null) {
                        super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{i, j});
                    } else {
                        // Pokémon selvagem - mostra como célula vazia sem nome
                        super.notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{i, j});
                    }
                } else if (tabuleiro[i][j].foiVisitada()) {
                    super.notificarObservadores("CELULA_VAZIA", new int[]{i, j});
                } else {
                    super.notificarObservadores("CELULA_VAZIA", new int[]{i, j});
                }
            }
        }
    }

    private void distribuirPokemonsSelvagens() {
        int numPokemons = (int) (TAMANHO_GRID * TAMANHO_GRID * 0.3);
        
        for (int i = 0; i < numPokemons; i++) {
            int x, y;
            do {
                x = random.nextInt(TAMANHO_GRID);
                y = random.nextInt(TAMANHO_GRID);
            } while (!tabuleiro[x][y].estaVazia());
            
            Pokemon pokemon = PokemonFactory.criarPokemonAleatorio();
            pokemon.setTreinador(null);
            tabuleiro[x][y].setPokemon(pokemon);
        }
    }

    private void processarTurnoComputador() {
        try {
            Thread.sleep(TEMPO_ESPERA_COMPUTADOR); // Espera antes de processar
            
            List<int[]> celulasNaoVisitadas = new ArrayList<>();
            
            for (int i = 0; i < TAMANHO_GRID; i++) {
                for (int j = 0; j < TAMANHO_GRID; j++) {
                    if (!tabuleiro[i][j].foiVisitada()) {
                        celulasNaoVisitadas.add(new int[]{i, j});
                    }
                }
            }
            
            if (!celulasNaoVisitadas.isEmpty()) {
                int[] coordenadas = celulasNaoVisitadas.get(random.nextInt(celulasNaoVisitadas.size()));
                processarTurno(computador, coordenadas[0], coordenadas[1]);
                
                // Verifica se encontrou um Pokémon selvagem
                if (pokemonSelvagemEncontrado != null) {
                    Thread.sleep(TEMPO_ESPERA_COMPUTADOR);
                    tratarAcaoEspecialComputador(-2, -2); // Computador sempre tenta capturar
                }
            } else {
                super.notificarObservadores("MENSAGEM", "Computador não encontrou células não exploradas.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            this.random = jogoCarregado.random;
            this.aguardandoAcaoJogador = false;
            this.pokemonSelvagemEncontrado = null;

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