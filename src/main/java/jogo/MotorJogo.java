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
    import javax.swing.SwingUtilities;
    import estrategia.AtaqueEletrico;
    import estrategia.AtaqueTerra;

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
        private int dicasDisponiveis = 3;
        private boolean[][] celulasComPokemon; // Matriz para rastrear células com Pokémon

        // Campos para controlar o estado de captura
        private transient Pokemon pokemonSelvagemEncontrado;
        private transient int ultimaCelulaX;
        private transient int ultimaCelulaY;
        private transient boolean aguardandoAcaoJogador; // Novo campo para controlar o estado

        private transient Pokemon pokemonJogadorBatalha;
        private transient Pokemon pokemonComputadorBatalha;
        private transient boolean batalhaEmAndamento;
        private transient boolean aguardandoAcaoJogadorBatalha; 

    public int getTurno() {
        return turno;
    }
    public class RegiaoInvalidaException extends Exception {
    
    public RegiaoInvalidaException(String message) {
        super(message);
    }
    
    public RegiaoInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}

    public MotorJogo() {
        tabuleiro = new Celula[TAMANHO_GRID][TAMANHO_GRID];
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        random = new Random();
        turno = 0;
        aguardandoAcaoJogador = false;

        // JOGADOR: Pokémon inicial vai para a mochila primeiro
        jogador.getMochila().adicionarPokemon(PokemonFactory.criarPokemonInicialJogador());

        // COMPUTADOR: Pokémon inicial vai direto para o time (método tradicional)
        computador.capturarPokemon(PokemonFactory.criarPokemonInicialComputador());

        // Transfere da mochila do jogador para o time ativo
        if (jogador.getMochila().getQuantidade() > 0) {
            jogador.getMochila().transferirParaTime(0);
        }

        inicializarTabuleiro();
        executorService = Executors.newSingleThreadExecutor();

        batalhaEmAndamento = false;
        aguardandoAcaoJogadorBatalha = false;
    }

        public Treinador getJogador() {
        return jogador;
    }

    public Treinador getComputador() {
        return computador;
    }

        private void inicializarTabuleiro() {
    for (int i = 0; i < TAMANHO_GRID; i++) {
        for (int j = 0; j < TAMANHO_GRID; j++) {
            tabuleiro[i][j] = new Celula(i, j);
        }
    }
    
    // Primeiro distribui os Pokémons selvagens por região
    distribuirPokemonsSelvagens();
    
    // Depois inicializa a matriz de controle
    inicializarMatrizPokemon(); 
    
    // Log das distribuições para debug
    contarPokemonsPorRegiao();
}
        private void contarPokemonsPorRegiao() {
    int[] contadores = new int[4]; // [agua, floresta, terra, eletrico]
    String[] nomeRegioes = {"ÁGUA", "FLORESTA", "TERRA", "ELÉTRICO"};
    
    for (int i = 0; i < TAMANHO_GRID; i++) {
        for (int j = 0; j < TAMANHO_GRID; j++) {
            if (!tabuleiro[i][j].estaVazia()) {
                String tipo = obterTipoRegiao(i, j);
                switch (tipo) {
                    case "AGUA": contadores[0]++; break;
                    case "FLORESTA": contadores[1]++; break;
                    case "TERRA": contadores[2]++; break;
                    case "ELETRICO": contadores[3]++; break;
                }
            }
        }
    }
    
    notificarObservadores("MENSAGEM", "=== DISTRIBUIÇÃO DE POKÉMONS POR REGIÃO ===");
    for (int i = 0; i < 4; i++) {
        notificarObservadores("MENSAGEM", 
            "Região " + nomeRegioes[i] + ": " + contadores[i] + " Pokémons");
    }
    notificarObservadores("MENSAGEM", "===========================================");
}

        public Celula[][] getTabuleiro() {
            return tabuleiro;
        }

    public void iniciarNovoJogo() {
        // Reset completo do estado do jogo
        jogador = new Treinador("Jogador");
        computador = new Treinador("Computador");
        turno = 0; // Importante: turno 0 = fase de posicionamento inicial
        aguardandoAcaoJogador = false;
        pokemonSelvagemEncontrado = null;

        // Reset do estado de batalha
        batalhaEmAndamento = false;
        aguardandoAcaoJogadorBatalha = false;
        pokemonJogadorBatalha = null;
        pokemonComputadorBatalha = null;

        // JOGADOR: Pokémon inicial vai para a mochila primeiro
        jogador.getMochila().adicionarPokemon(PokemonFactory.criarPokemonInicialJogador());

        // COMPUTADOR: Pokémon inicial vai direto para o time (método tradicional)
        computador.capturarPokemon(PokemonFactory.criarPokemonInicialComputador());

        // Transfere o primeiro Pokémon da mochila do jogador para o time ativo
        if (jogador.getMochila().getQuantidade() > 0) {
            jogador.getMochila().transferirParaTime(0);
        }

        // Inicializa o tabuleiro limpo (sem posicionar os Pokémons ainda)
        inicializarTabuleiro();

        // Notificações para a UI
        super.notificarObservadores("STATUS_ATUALIZADO", new String[]{jogador.getStatus(), computador.getStatus()});
        super.notificarObservadores("MENSAGEM", "=== NOVO JOGO INICIADO ===");
        super.notificarObservadores("MENSAGEM", "Fase de Posicionamento Inicial:");
        super.notificarObservadores("MENSAGEM", "• Você precisa escolher onde posicionar seu " + jogador.getPokemonAtual().getNome());
        super.notificarObservadores("MENSAGEM", "• O computador escolherá automaticamente após você");
        super.notificarObservadores("MENSAGEM", "Escolha a posição inicial do seu Pokémon clicando em qualquer célula do tabuleiro.");
        super.notificarObservadores("SOLICITAR_POSICAO_INICIAL", null);
    }

    // ADICIONE ESTES MÉTODOS E CORREÇÕES NO MOTOJOGO.JAVA:

    // 1. ADICIONAR este método getter no MotorJogo:
    /**
     * Retorna o turno atual do jogo
     * @return número do turno atual
     */

    // 2. CORRIGIR o método jogar() completo:
    public void jogar(int x, int y) {
        // Verifica se é o início do jogo e o Pokémon ainda não foi posicionado
        if (turno == 0 && !pokemonInicialPosicionado()) {
            posicionarPokemonInicial(x, y);
            return;
        }

        // Verifica se é uma ação de batalha
        if (x == -1 && y == -1 && batalhaEmAndamento) {
            processarTurnoBatalha();
            return;
        }

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

        // Verifica se há uma batalha em andamento
        if (batalhaEmAndamento) {
            super.notificarObservadores("MENSAGEM", "Há uma batalha em andamento! Use os botões da batalha.");
            return;
        }

        // Verifica se as coordenadas são válidas
        if (x < 0 || x >= TAMANHO_GRID || y < 0 || y >= TAMANHO_GRID) {
            super.notificarObservadores("MENSAGEM", "Coordenadas inválidas! Escolha entre (0,0) e (" + (TAMANHO_GRID-1) + "," + (TAMANHO_GRID-1) + ")");
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
        // Verifica se há uma batalha em andamento
        if (batalhaEmAndamento) {
            notificarObservadores("MENSAGEM", "Computador está em batalha e não pode iniciar novo turno.");
            return;
        }

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
                if (jogador.getMochila().adicionarPokemon(pokemonSelvagemEncontrado)) {
                    pokemonSelvagemEncontrado.setTreinador(jogador);
                    notificarObservadores("MENSAGEM", "Você capturou " + pokemonSelvagemEncontrado.getNome() + "!");
                    jogador.adicionarPontos(50);

                    // IMPORTANTE: NÃO remove o Pokémon do tabuleiro
                    // O Pokémon permanece no tabuleiro mas agora pertence ao jogador
                    tabuleiro[ultimaCelulaX][ultimaCelulaY].setVisitada(true);

                    // Notifica que foi capturado (para UI ocultar visualmente)
                    notificarObservadores("POKEMON_CAPTURADO", pokemonSelvagemEncontrado);
                    notificarObservadores("MOCHILA_ATUALIZADA", jogador.getMochila().getPokemonsCapturados());
                } else {
                    notificarObservadores("MENSAGEM", "Mochila cheia! Não foi possível capturar " + pokemonSelvagemEncontrado.getNome());
                }
            }
        } else {
            // CORREÇÃO: Falhou na captura - Pokémon foge para outra célula
            notificarObservadores("MENSAGEM", "Você falhou ao tentar capturar " + pokemonSelvagemEncontrado.getNome() + "!");
            
            // Remove o Pokémon da célula atual
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
            
            // NOVO: Atualiza a matriz de controle se existir
            if (celulasComPokemon != null) {
                celulasComPokemon[ultimaCelulaX][ultimaCelulaY] = false;
            }
            
            // CORREÇÃO PRINCIPAL: Limpa a célula e torna clicável novamente
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setVisitada(false); // IMPORTANTE: Marca como NÃO visitada
            notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
            
            // Tenta fazer o Pokémon fugir para uma nova célula
            if (fazerPokemonFugir(pokemonSelvagemEncontrado)) {
                notificarObservadores("MENSAGEM", pokemonSelvagemEncontrado.getNome() + " fugiu para outro local do mapa!");
            } else {
                notificarObservadores("MENSAGEM", pokemonSelvagemEncontrado.getNome() + " fugiu do mapa completamente!");
            }
        }
    }
    else if (codigoX == -3 && codigoY == -3) { // Fugir
        if (pokemonSelvagemEncontrado.tentarFugir()) {
            notificarObservadores("MENSAGEM", "Você conseguiu fugir do Pokémon selvagem!");
            // Pokémon selvagem foge - remove do tabuleiro
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
            
            // NOVO: Atualiza a matriz de controle se existir
            if (celulasComPokemon != null) {
                celulasComPokemon[ultimaCelulaX][ultimaCelulaY] = false;
            }
            
            // CORREÇÃO: Marca como não visitada para permitir cliques futuros
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setVisitada(false);
            notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
        } else {
            notificarObservadores("MENSAGEM", "Você não conseguiu fugir do Pokémon selvagem!");
            // Pokémon ainda está lá, mas oculto
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
                // Captura bem-sucedida - computador usa método tradicional
                computador.capturarPokemon(pokemonSelvagemEncontrado);
                pokemonSelvagemEncontrado.setTreinador(computador);
                notificarObservadores("MENSAGEM", "Computador capturou " + pokemonSelvagemEncontrado.getNome() + "!");
                computador.adicionarPontos(50);

                // O Pokémon permanece no tabuleiro mas agora pertence ao computador
                // Marca a célula como visitada
                tabuleiro[ultimaCelulaX][ultimaCelulaY].setVisitada(true);

                // Notifica que há um Pokémon do computador nesta posição
                notificarObservadores("POKEMON_ENCONTRADO", new int[]{ultimaCelulaX, ultimaCelulaY});
            }
        } else {
            // CORREÇÃO: Falha na captura - Pokémon foge para outra célula
            notificarObservadores("MENSAGEM", "Computador falhou ao tentar capturar " + pokemonSelvagemEncontrado.getNome());
            
            // Remove o Pokémon da célula atual
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setPokemon(null);
            
            // NOVO: Atualiza a matriz de controle se existir
            if (celulasComPokemon != null) {
                celulasComPokemon[ultimaCelulaX][ultimaCelulaY] = false;
            }
            
            // CORREÇÃO PRINCIPAL: Limpa a célula e torna clicável novamente
            tabuleiro[ultimaCelulaX][ultimaCelulaY].setVisitada(false); // IMPORTANTE: Marca como NÃO visitada
            notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{ultimaCelulaX, ultimaCelulaY});
            
            // Tenta fazer o Pokémon fugir para uma nova célula
            if (fazerPokemonFugir(pokemonSelvagemEncontrado)) {
                notificarObservadores("MENSAGEM", pokemonSelvagemEncontrado.getNome() + " fugiu para outro local!");
            } else {
                notificarObservadores("MENSAGEM", pokemonSelvagemEncontrado.getNome() + " fugiu do mapa!");
            }
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

        // Se o Pokémon pertence a um treinador
        if (pokemonEncontrado.getTreinador() != null) {

            // NOVA VERIFICAÇÃO: Se é um Pokémon capturado pelo jogador (na mochila)
            // mas encontrado pelo computador, pode iniciar batalha
            if (treinador == computador && 
                pokemonEncontrado.getTreinador() == jogador &&
                !jogador.getTime().contains(pokemonEncontrado)) {

                // Computador encontrou um Pokémon capturado do jogador
                // Precisa de um Pokémon do time ativo do jogador para batalhar
                if (jogador.getPokemonAtual() != null) {
                    notificarObservadores("MENSAGEM", 
                        "Computador encontrou um dos seus Pokémons capturados! " +
                        pokemonEncontrado.getNome() + " vs " + jogador.getPokemonAtual().getNome());
                    notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
                    notificarObservadores("BATALHA_INICIADA", 
                        new Pokemon[]{computador.getPokemonAtual(), pokemonEncontrado});
                    batalhar(computador.getPokemonAtual(), pokemonEncontrado);
                    return;
                }
            }

            // Se o JOGADOR encontra um Pokémon do COMPUTADOR, inicia batalha
            if (treinador == jogador && pokemonEncontrado.getTreinador() == computador) {
                notificarObservadores("MENSAGEM", "Você encontrou um Pokémon do computador!");
                notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
                notificarObservadores("BATALHA_INICIADA", 
                    new Pokemon[]{jogador.getPokemonAtual(), pokemonEncontrado});
                batalhar(jogador.getPokemonAtual(), pokemonEncontrado);
                return;
            }

            // Se o COMPUTADOR encontra um Pokémon ATIVO do JOGADOR, inicia batalha
            if (treinador == computador && 
                pokemonEncontrado.getTreinador() == jogador &&
                jogador.getTime().contains(pokemonEncontrado)) {
                notificarObservadores("MENSAGEM", "Computador encontrou seu Pokémon ativo!");
                notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
                notificarObservadores("BATALHA_INICIADA", 
                    new Pokemon[]{computador.getPokemonAtual(), pokemonEncontrado});
                batalhar(computador.getPokemonAtual(), pokemonEncontrado);
                return;
            }

            // Se encontra próprio Pokémon (não deveria acontecer normalmente)
            if (pokemonEncontrado.getTreinador().equals(treinador)) {
                notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
                String mensagem = treinador == jogador ? 
                    "Você encontrou seu próprio Pokémon." : 
                    "Computador encontrou seu próprio Pokémon.";
                notificarObservadores("MENSAGEM", mensagem);
                return;
            }
        }

        // Se é um Pokémon selvagem
        this.pokemonSelvagemEncontrado = pokemonEncontrado;
        this.ultimaCelulaX = x;
        this.ultimaCelulaY = y;

        notificarObservadores("POKEMON_SELVAGEM_ENCONTRADO", pokemonEncontrado);
        notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{x, y});
    }


    private void batalhar(Pokemon pokemonJogador, Pokemon pokemonOponente) {
        // NOVAS LINHAS: Reset dos sistemas especiais
        AtaqueTerra.resetarTurnos();       // Reset turnos para Pokémon Terra
        AtaqueEletrico.limparParalisia();  // Limpa paralisia anterior

        this.pokemonJogadorBatalha = pokemonJogador;
        this.pokemonComputadorBatalha = pokemonOponente;
        this.batalhaEmAndamento = true;
        this.aguardandoAcaoJogadorBatalha = true;

        notificarObservadores("BATALHA_INICIADA", 
            new Pokemon[]{pokemonJogador, pokemonOponente});

        notificarObservadores("MENSAGEM", 
            "Batalha iniciada! " + pokemonJogador.getNome() + " vs " + pokemonOponente.getNome());
    }


        private void atualizarStatus() {
            String statusJogador = jogador.getStatus();
            String statusComputador = computador.getStatus();
            super.notificarObservadores("STATUS_ATUALIZADO", new String[]{statusJogador, statusComputador});
        }

    private int calcularValorAtaque(Pokemon pokemon) {
        // valor_ataque = random(força) * nível + experiência
        int forcaAleatoria = random.nextInt(pokemon.getForca()) + 1; // +1 para evitar 0
        int valorAtaque = (forcaAleatoria * pokemon.getNivel()) + pokemon.getNivel();

        return Math.max(1, valorAtaque); // Garantir que o ataque seja pelo menos 1
    }

    // 2. MODIFICAR o método processarTurnoBatalha():
    public void processarTurnoBatalha() {

        if (!batalhaEmAndamento || !aguardandoAcaoJogadorBatalha) {
            notificarObservadores("MENSAGEM", "Não há batalha em andamento.");
            return;
        }

        // Verifica se o Pokémon está paralisado
        if (!AtaqueEletrico.podeAtacar(pokemonJogadorBatalha)) {
            aguardandoAcaoJogadorBatalha = false;
            executorService.submit(() -> {
                try {
                    Thread.sleep(1500);
                    processarTurnoComputadorBatalha();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return;
        }

        // NOVO: Armazena energia anterior do ATACANTE também (para regeneração)
        int energiaAnteriorAtacante = pokemonJogadorBatalha.getEnergia();
        int energiaAnteriorOponente = pokemonComputadorBatalha.getEnergia();

        // Turno do jogador
        int ataqueJogador = pokemonJogadorBatalha.atacar(pokemonComputadorBatalha);

        // Aplica o dano no oponente
        pokemonComputadorBatalha.receberDano(ataqueJogador);

        // NOVO: Verifica se houve regeneração no atacante
        int energiaAtualAtacante = pokemonJogadorBatalha.getEnergia();
        if (energiaAtualAtacante > energiaAnteriorAtacante) {
            // Houve regeneração - notifica a UI
            notificarObservadores("REGENERACAO_REALIZADA", 
                new Object[]{
                    pokemonJogadorBatalha.getNome(),
                    energiaAnteriorAtacante,
                    energiaAtualAtacante,
                    pokemonJogadorBatalha.getEnergiaMaxima(),
                    energiaAtualAtacante - energiaAnteriorAtacante // quantidade regenerada
                });
        }

        // Notifica o ataque normal
        notificarObservadores("ATAQUE_REALIZADO", 
            new Object[]{
                pokemonJogadorBatalha.getNome(), 
                pokemonComputadorBatalha.getNome(),
                ataqueJogador,
                energiaAnteriorOponente,
                pokemonComputadorBatalha.getEnergia()
            });

        notificarObservadores("MENSAGEM", 
            pokemonJogadorBatalha.getNome() + " atacou " + pokemonComputadorBatalha.getNome() + 
            " causando " + ataqueJogador + " de dano!");

        // Verifica se o oponente foi nocauteado
        if (pokemonComputadorBatalha.estaNocauteado()) {
            finalizarBatalha(pokemonJogadorBatalha, pokemonComputadorBatalha, true);
            return;
        }

        AtaqueEletrico.processarTurno();

        // Turno do computador
        executorService.submit(() -> {
            try {
                Thread.sleep(1500);
                processarTurnoComputadorBatalha();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        aguardandoAcaoJogadorBatalha = false;
    }

    private void processarTurnoComputadorBatalha() {
        if (!batalhaEmAndamento) return;

        if (!AtaqueEletrico.podeAtacar(pokemonComputadorBatalha)) {
            aguardandoAcaoJogadorBatalha = true;
            notificarObservadores("TURNO_JOGADOR", 
                new Pokemon[]{pokemonJogadorBatalha, pokemonComputadorBatalha});
            return;
        }

        // NOVO: Armazena energia anterior do ATACANTE também (para regeneração)
        int energiaAnteriorAtacante = pokemonComputadorBatalha.getEnergia();
        int energiaAnteriorJogador = pokemonJogadorBatalha.getEnergia();

        // Turno do computador
        int ataqueComputador = pokemonComputadorBatalha.atacar(pokemonJogadorBatalha);

        // Aplica o dano no jogador
        pokemonJogadorBatalha.receberDano(ataqueComputador);

        // NOVO: Verifica se houve regeneração no atacante (computador)
        int energiaAtualAtacante = pokemonComputadorBatalha.getEnergia();
        if (energiaAtualAtacante > energiaAnteriorAtacante) {
            // Houve regeneração - notifica a UI
            notificarObservadores("REGENERACAO_REALIZADA", 
                new Object[]{
                    pokemonComputadorBatalha.getNome(),
                    energiaAnteriorAtacante,
                    energiaAtualAtacante,
                    pokemonComputadorBatalha.getEnergiaMaxima(),
                    energiaAtualAtacante - energiaAnteriorAtacante // quantidade regenerada
                });
        }

        // Notifica o ataque normal
        notificarObservadores("ATAQUE_REALIZADO", 
            new Object[]{
                pokemonComputadorBatalha.getNome(), 
                pokemonJogadorBatalha.getNome(),
                ataqueComputador,
                energiaAnteriorJogador,
                pokemonJogadorBatalha.getEnergia()
            });

        notificarObservadores("MENSAGEM", 
            pokemonComputadorBatalha.getNome() + " atacou " + pokemonJogadorBatalha.getNome() + 
            " causando " + ataqueComputador + " de dano!");

        // Verifica se o jogador foi nocauteado
        if (pokemonJogadorBatalha.estaNocauteado()) {
            finalizarBatalha(pokemonComputadorBatalha, pokemonJogadorBatalha, false);
            return;
        }

        AtaqueEletrico.processarTurno();

        // Continua a batalha
        aguardandoAcaoJogadorBatalha = true;
        notificarObservadores("TURNO_JOGADOR", 
            new Pokemon[]{pokemonJogadorBatalha, pokemonComputadorBatalha});
    }


    private void finalizarBatalha(Pokemon vencedor, Pokemon perdedor, boolean jogadorVenceu) {
        batalhaEmAndamento = false;
        aguardandoAcaoJogadorBatalha = false;

        AtaqueEletrico.limparParalisia();
        notificarObservadores("BATALHA_TERMINADA", vencedor);

        if (jogadorVenceu) {
            int expGanha = vencedor.ganharExperienciaPorVitoria(perdedor);

            notificarObservadores("MENSAGEM", 
                "Você venceu a batalha! " + vencedor.getNome() + " derrotou " + perdedor.getNome() + "!");
            notificarObservadores("MENSAGEM", 
                vencedor.getNome() + " ganhou " + expGanha + " pontos de experiência!");

            if (vencedor.getExpParaProximoNivel() > 0) {
                notificarObservadores("MENSAGEM", 
                    "Faltam " + vencedor.getExpParaProximoNivel() + " pontos para o próximo nível.");
            } else {
                notificarObservadores("MENSAGEM", 
                    vencedor.getNome() + " atingiu o nível máximo!");
            }

            jogador.adicionarPontos(100);
            removerPokemonCompletamente(perdedor, computador);

        } else {
            int expGanha = vencedor.ganharExperienciaPorVitoria(perdedor);

            notificarObservadores("MENSAGEM", 
                "Você perdeu a batalha! " + perdedor.getNome() + " foi derrotado por " + vencedor.getNome() + "!");
            notificarObservadores("MENSAGEM", 
                vencedor.getNome() + " do computador ganhou " + expGanha + " pontos de experiência!");

            computador.adicionarPontos(100);
            removerPokemonCompletamente(perdedor, jogador);
        }

        // Limpar referências da batalha
        pokemonJogadorBatalha = null;
        pokemonComputadorBatalha = null;

        atualizarStatus();
        atualizarTabuleiroUI();

        if (!jogadorVenceu) {
            notificarObservadores("MOCHILA_ATUALIZADA", jogador.getMochila().getPokemonsCapturados());
        }
    }



    /**
     * Remove completamente um Pokémon derrotado do jogo:
     * - Remove do tabuleiro
     * - Remove do time ativo do treinador
     * - Remove da mochila (se for do jogador)
     * - Define um novo Pokémon ativo se necessário
     */
    private void removerPokemonCompletamente(Pokemon pokemonDerrotado, Treinador treinador) {
        // 1. Remove do tabuleiro
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                if (tabuleiro[i][j].getPokemon() == pokemonDerrotado) {
                    tabuleiro[i][j].setPokemon(null);
                    notificarObservadores("CELULA_VAZIA", new int[]{i, j});
                    break;
                }
            }
        }

        // 2. Remove do time ativo do treinador
        treinador.removerPokemon(pokemonDerrotado);

        // 3. Se for do jogador, também remove da mochila
        if (treinador == jogador) {
            jogador.getMochila().removerPokemon(pokemonDerrotado);
            notificarObservadores("MENSAGEM", 
                pokemonDerrotado.getNome() + " foi removido do seu time e mochila!");
        } else {
            notificarObservadores("MENSAGEM", 
                pokemonDerrotado.getNome() + " foi removido do time do computador!");
        }

        // 4. Verifica se o treinador ainda tem Pokémons ativos
        if (treinador.getTime().isEmpty()) {
            if (treinador == jogador) {
                // Se jogador ficou sem Pokémons, verifica se tem algum na mochila para transferir
                if (jogador.getMochila().getQuantidade() > 0) {
                    jogador.getMochila().transferirParaTime(0);
                    notificarObservadores("MENSAGEM", 
                        "Um Pokémon da sua mochila foi automaticamente transferido para o time ativo!");
                    notificarObservadores("MOCHILA_ATUALIZADA", jogador.getMochila().getPokemonsCapturados());
                } else {
                    notificarObservadores("MENSAGEM", 
                        "GAME OVER: Você ficou sem Pokémons! O computador venceu!");
                    // Aqui você pode implementar lógica de fim de jogo
                }
            } else {
                notificarObservadores("MENSAGEM", 
                    "O computador ficou sem Pokémons! Você venceu o jogo!");
                // Aqui você pode implementar lógica de vitória
            }
        }
    }

    /**
     * Remove completamente um Pokémon derrotado do jogo:
     * - Remove do tabuleiro
     * - Remove do time ativo do treinador
     * - Remove da mochila (se for do jogador)
     * - Define um novo Pokémon ativo se necessário
     */

    // Método para desistir da batalha
    public void desistirBatalha() {
        if (!batalhaEmAndamento) {
            notificarObservadores("MENSAGEM", "Não há batalha em andamento para desistir.");
            return;
        }

        notificarObservadores("MENSAGEM", "Você desistiu da batalha!");
        finalizarBatalha(pokemonComputadorBatalha, pokemonJogadorBatalha, false);
    }

    private void atualizarTabuleiroUI() {
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                Celula celula = tabuleiro[i][j];

                if (!celula.estaVazia() && celula.getPokemon() != null) {
                    Pokemon pokemon = celula.getPokemon();

                    // Mostra TODOS os Pokémons que pertencem a treinadores no mapa
                    if (pokemon.getTreinador() != null) {
                        super.notificarObservadores("POKEMON_ENCONTRADO", new int[]{i, j});
                    } 
                    // Pokémons selvagens ficam ocultos
                    else {
                        super.notificarObservadores("CELULA_VAZIA_SEM_NOME", new int[]{i, j});
                    }
                } else if (celula.foiVisitada()) {
                    super.notificarObservadores("CELULA_VAZIA", new int[]{i, j});
                }
            }
        }
    }

        // Substitua o método distribuirPokemonsSelvagens() no MotorJogo.java por este:

private void distribuirPokemonsSelvagens() {
    int numPokemonsPorRegiao = (int) ((TAMANHO_GRID * TAMANHO_GRID * 0.3) / 4); // Divide igualmente entre 4 regiões
    int meio = TAMANHO_GRID / 2;
    
    // Região ÁGUA (quadrante superior esquerdo - azul)
    distribuirPokemonsNaRegiao(0, meio-1, 0, meio-1, "AGUA", numPokemonsPorRegiao);
    
    // Região FLORESTA (quadrante superior direito - verde)  
    distribuirPokemonsNaRegiao(0, meio-1, meio, TAMANHO_GRID-1, "FLORESTA", numPokemonsPorRegiao);
    
    // Região TERRA (quadrante inferior esquerdo - marrom)
    distribuirPokemonsNaRegiao(meio, TAMANHO_GRID-1, 0, meio-1, "TERRA", numPokemonsPorRegiao);
    
    // Região ELÉTRICO (quadrante inferior direito - amarelo)
    distribuirPokemonsNaRegiao(meio, TAMANHO_GRID-1, meio, TAMANHO_GRID-1, "ELETRICO", numPokemonsPorRegiao);
}

private void distribuirPokemonsNaRegiao(int xMin, int xMax, int yMin, int yMax, String tipoRegiao, int quantidade) {
    List<int[]> celulasDisponiveis = new ArrayList<>();
    
    // Coleta todas as células vazias da região
    for (int i = xMin; i <= xMax; i++) {
        for (int j = yMin; j <= yMax; j++) {
            if (tabuleiro[i][j].estaVazia()) {
                celulasDisponiveis.add(new int[]{i, j});
            }
        }
    }
    
    // Embaralha a lista para distribuição aleatória
    java.util.Collections.shuffle(celulasDisponiveis, random);
    
    // Distribui os Pokémons específicos da região
    int pokemonsColocados = 0;
    for (int[] posicao : celulasDisponiveis) {
        if (pokemonsColocados >= quantidade) break;
        
        int x = posicao[0];
        int y = posicao[1];
        
        Pokemon pokemon = criarPokemonPorRegiao(tipoRegiao);
        pokemon.setTreinador(null); // Pokémon selvagem
        tabuleiro[x][y].setPokemon(pokemon);
        
        pokemonsColocados++;
    }
    
    notificarObservadores("MENSAGEM", 
        "Distribuídos " + pokemonsColocados + " Pokémons de " + tipoRegiao + 
        " na região correspondente.");
}

        private void processarTurnoComputador() {
        try {
            // Verifica se há uma batalha em andamento
            if (batalhaEmAndamento) {
                notificarObservadores("MENSAGEM", "Computador está em batalha e não pode explorar agora.");
                return;
            }

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

        public boolean isBatalhaEmAndamento() {
        return batalhaEmAndamento;
    }

    public boolean isAguardandoAcaoJogadorBatalha() {
        return aguardandoAcaoJogadorBatalha;
    }

    public Pokemon getPokemonJogadorBatalha() {
        return pokemonJogadorBatalha;
    }

    public Pokemon getPokemonComputadorBatalha() {
        return pokemonComputadorBatalha;
    }
    public void mostrarInformacoesPokemon(Pokemon pokemon) {
        if (pokemon == null) return;

        StringBuilder info = new StringBuilder();
        info.append("=== ").append(pokemon.getNome()).append(" ===\n");
        info.append("Nível: ").append(pokemon.getNivel()).append("\n");
        info.append("Força: ").append(pokemon.getForca()).append("\n");
        info.append("Energia: ").append(pokemon.getEnergia()).append("/").append(pokemon.getEnergiaMaxima()).append("\n");
        info.append("Experiência: ").append(pokemon.getExperiencia()).append("\n");

        if (pokemon.getExpParaProximoNivel() > 0) {
            info.append("EXP para próximo nível: ").append(pokemon.getExpParaProximoNivel()).append("\n");
            info.append("Progresso: ").append(String.format("%.1f", pokemon.getProgressoNivel())).append("%\n");
        } else {
            info.append("Nível máximo atingido!\n");
        }

        notificarObservadores("MENSAGEM", info.toString());
    }
    public void posicionarPokemonInicial(int x, int y) {
    try {
        // Verifica se as coordenadas são válidas
        if (x < 0 || x >= TAMANHO_GRID || y < 0 || y >= TAMANHO_GRID) {
            notificarObservadores("MENSAGEM", "Posição inválida! Escolha coordenadas entre 0 e " + (TAMANHO_GRID-1));
            return;
        }

        // Verifica se a célula já tem um Pokémon
        if (!tabuleiro[x][y].estaVazia()) {
            notificarObservadores("MENSAGEM", "Esta célula já contém um Pokémon! Escolha outra posição.");
            return;
        }

        // Verifica se o jogador tem Pokémon ativo
        if (jogador.getPokemonAtual() == null) {
            notificarObservadores("MENSAGEM", "Erro: Jogador não possui Pokémon ativo!");
            return;
        }

        // Obtém o Pokémon inicial do jogador
        Pokemon pokemonInicial = jogador.getPokemonAtual();

        // NOVA VALIDAÇÃO: Verifica se a região é compatível
        validarRegiaoParaPokemon(pokemonInicial, x, y);

        // Se chegou até aqui, a região é válida - posiciona o Pokémon
        tabuleiro[x][y].setPokemon(pokemonInicial);
        tabuleiro[x][y].setVisitada(true);

        // Notifica a UI
        notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
        notificarObservadores("MENSAGEM", pokemonInicial.getNome() + " foi posicionado em (" + x + ", " + y + ") na região " + obterTipoRegiao(x, y));

        // Atualiza o status
        atualizarStatus();

        // Computador posiciona automaticamente
        notificarObservadores("MENSAGEM", "Computador está escolhendo sua posição inicial...");

        executorService.submit(() -> {
            try {
                Thread.sleep(1000);
                posicionarPokemonInicialComputador();

                SwingUtilities.invokeLater(() -> {
                    atualizarTabuleiroUI();
                    turno = 1;
                    notificarObservadores("POSICIONAMENTO_CONCLUIDO", null);
                    notificarObservadores("MENSAGEM", "Ambos os Pokémons foram posicionados!");
                    notificarObservadores("MENSAGEM", "Jogo iniciado! É seu turno! Clique em uma célula para explorar.");
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> {
                    notificarObservadores("MENSAGEM", "Erro ao posicionar Pokémon do computador.");
                });
            }
        });

    } catch (RegiaoInvalidaException e) {
        // Notifica o erro e permite nova tentativa
        notificarObservadores("MENSAGEM", "❌ " + e.getMessage());
        notificarObservadores("MENSAGEM", "💡 Tente novamente escolhendo uma posição na região correta.");
        
        // Mostra dica sobre onde posicionar
        String tipoPokemon = obterTipoPokemon(jogador.getPokemonAtual());
        String regiaoCorreta = obterDescricaoRegiao(tipoPokemon);
        notificarObservadores("MENSAGEM", "🎯 " + regiaoCorreta);
    }
}

    private boolean pokemonInicialPosicionado() {
        // Se o turno já passou de 0, significa que o jogo já começou
        if (turno > 0) {
            return true;
        }

        // Verifica se existe algum Pokémon do jogador no tabuleiro
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                Pokemon pokemon = tabuleiro[i][j].getPokemon();
                if (pokemon != null && pokemon.getTreinador() == jogador) {
                    return true;
                }
            }
        }
        return false;
    }
    private void posicionarPokemonInicialComputador() {
    if (computador.getPokemonAtual() == null) {
        notificarObservadores("MENSAGEM", "Erro: Computador não possui Pokémon ativo!");
        return;
    }

    Pokemon pokemonInicial = computador.getPokemonAtual();
    String tipoPokemon = obterTipoPokemon(pokemonInicial);

    // NOVO: Determina a região correta baseada no tipo do Pokémon
    List<int[]> celulasRegiao = obterCelulasRegiao(tipoPokemon);
    
    if (celulasRegiao.isEmpty()) {
        notificarObservadores("MENSAGEM", "Erro: Não foi possível encontrar região para " + pokemonInicial.getNome());
        return;
    }

    // Filtra apenas células vazias na região correta
    List<int[]> celulasDisponiveis = new ArrayList<>();
    for (int[] posicao : celulasRegiao) {
        int x = posicao[0];
        int y = posicao[1];
        if (tabuleiro[x][y].estaVazia()) {
            celulasDisponiveis.add(new int[]{x, y});
        }
    }

    if (celulasDisponiveis.isEmpty()) {
        notificarObservadores("MENSAGEM", "Erro: Não há células disponíveis na região " + tipoPokemon + " para o computador!");
        return;
    }

    // Escolhe uma posição aleatória DENTRO DA REGIÃO CORRETA
    int[] posicaoEscolhida = celulasDisponiveis.get(random.nextInt(celulasDisponiveis.size()));
    int x = posicaoEscolhida[0];
    int y = posicaoEscolhida[1];

    // Posiciona o Pokémon no tabuleiro
    tabuleiro[x][y].setPokemon(pokemonInicial);
    tabuleiro[x][y].setVisitada(true);

    // Notifica a UI
    notificarObservadores("POKEMON_ENCONTRADO", new int[]{x, y});
    notificarObservadores("MENSAGEM", 
        "Computador posicionou " + pokemonInicial.getNome() + 
        " (" + tipoPokemon + ") em (" + x + ", " + y + ") na região " + obterNomeRegiao(tipoPokemon));

    // Atualiza o status
    atualizarStatus();
}
    private List<int[]> obterCelulasRegiao(String tipoRegiao) {
    List<int[]> celulas = new ArrayList<>();
    int meio = TAMANHO_GRID / 2;
    
    switch (tipoRegiao) {
        case "AGUA":
            // Quadrante superior esquerdo (0-3, 0-3)
            for (int i = 0; i < meio; i++) {
                for (int j = 0; j < meio; j++) {
                    celulas.add(new int[]{i, j});
                }
            }
            break;
            
        case "FLORESTA":
            // Quadrante superior direito (0-3, 4-7)
            for (int i = 0; i < meio; i++) {
                for (int j = meio; j < TAMANHO_GRID; j++) {
                    celulas.add(new int[]{i, j});
                }
            }
            break;
            
        case "TERRA":
            // Quadrante inferior esquerdo (4-7, 0-3)
            for (int i = meio; i < TAMANHO_GRID; i++) {
                for (int j = 0; j < meio; j++) {
                    celulas.add(new int[]{i, j});
                }
            }
            break;
            
        case "ELETRICO":
            // Quadrante inferior direito (4-7, 4-7)
            for (int i = meio; i < TAMANHO_GRID; i++) {
                for (int j = meio; j < TAMANHO_GRID; j++) {
                    celulas.add(new int[]{i, j});
                }
            }
            break;
    }
    
    return celulas;
}

    private void inicializarMatrizPokemon() {
        celulasComPokemon = new boolean[TAMANHO_GRID][TAMANHO_GRID];
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                celulasComPokemon[i][j] = !tabuleiro[i][j].estaVazia();
            }
        }
    }
    public String obterDica(int x, int y) {
        if (dicasDisponiveis <= 0) {
            return "Você não tem mais dicas disponíveis!";
        }

        dicasDisponiveis--;
        StringBuilder dica = new StringBuilder();

        // Verifica a linha
        boolean pokemonNaLinha = false;
        for (int j = 0; j < TAMANHO_GRID; j++) {
            if (celulasComPokemon[x][j] && !tabuleiro[x][j].foiVisitada()) {
                pokemonNaLinha = true;
                break;
            }
        }
        dica.append("Linha ").append(x).append(": ").append(pokemonNaLinha ? "TEM Pokémon!" : "NÃO tem Pokémon").append("\n");

        // Verifica a coluna
        boolean pokemonNaColuna = false;
        for (int i = 0; i < TAMANHO_GRID; i++) {
            if (celulasComPokemon[i][y] && !tabuleiro[i][y].foiVisitada()) {
                pokemonNaColuna = true;
                break;
            }
        }
        dica.append("Coluna ").append(y).append(": ").append(pokemonNaColuna ? "TEM Pokémon!" : "NÃO tem Pokémon");

        notificarObservadores("DICA_USADA", dicasDisponiveis);
        return dica.toString();
    }

    public int getDicasDisponiveis() {
        return dicasDisponiveis;
    }

private boolean fazerPokemonFugir(Pokemon pokemon) {
    // Lista todas as células vazias e não visitadas
    List<int[]> celulasVazias = new ArrayList<>();
    
    for (int i = 0; i < TAMANHO_GRID; i++) {
        for (int j = 0; j < TAMANHO_GRID; j++) {
            // Célula deve estar vazia E não ter sido visitada (para manter o mistério)
            if (tabuleiro[i][j].estaVazia() && !tabuleiro[i][j].foiVisitada()) {
                celulasVazias.add(new int[]{i, j});
            }
        }
    }
    
    // Se não há células vazias disponíveis, o Pokémon some do mapa
    if (celulasVazias.isEmpty()) {
        return false;
    }
    
    // Escolhe uma célula aleatória
    int[] novaPosicao = celulasVazias.get(random.nextInt(celulasVazias.size()));
    int novoX = novaPosicao[0];
    int novoY = novaPosicao[1];
    
    // Coloca o Pokémon na nova posição
    tabuleiro[novoX][novoY].setPokemon(pokemon);
    
    // Atualiza a matriz de controle de Pokémons (se existir)
    if (celulasComPokemon != null) {
        celulasComPokemon[novoX][novoY] = true;
    }
    
    // Log para debug (opcional)
    notificarObservadores("MENSAGEM", 
        "[DEBUG] " + pokemon.getNome() + " fugiu para a posição (" + novoX + ", " + novoY + ")");
    
    return true;
}

private String obterTipoRegiao(int x, int y) {
    int meio = TAMANHO_GRID / 2;
    
    if (x < meio && y < meio) {
        return "AGUA";          // Quadrante superior esquerdo
    } else if (x < meio && y >= meio) {
        return "FLORESTA";      // Quadrante superior direito
    } else if (x >= meio && y < meio) {
        return "TERRA";         // Quadrante inferior esquerdo
    } else {
        return "ELETRICO";      // Quadrante inferior direito
    }
}

private Pokemon criarPokemonPorRegiao(String tipoRegiao) {
    switch (tipoRegiao) {
        case "AGUA":
            return PokemonFactory.criarPokemonAgua();
            
        case "FLORESTA":
            return PokemonFactory.criarPokemonFloresta();
            
        case "TERRA":
            return PokemonFactory.criarPokemonTerra();
            
        case "ELETRICO":
            return PokemonFactory.criarPokemonEletrico();
            
        default:
            // Fallback - retorna um Pokémon aleatório
            String[] tipos = {"AGUA", "FLORESTA", "TERRA", "ELETRICO"};
            String tipoAleatorio = tipos[random.nextInt(tipos.length)];
            return criarPokemonPorRegiao(tipoAleatorio);
    }
}
private void validarRegiaoParaPokemon(Pokemon pokemon, int x, int y) throws RegiaoInvalidaException {
    String tipoRegiao = obterTipoRegiao(x, y);
    String tipoPokemon = obterTipoPokemon(pokemon);
    
    if (!tipoRegiao.equals(tipoPokemon)) {
        String nomeRegiao = obterNomeRegiao(tipoRegiao);
        String nomeRegiaoCorreta = obterNomeRegiao(tipoPokemon);
        
        throw new RegiaoInvalidaException(
            String.format("O Pokémon %s (tipo %s) não pode ser posicionado na região %s! " +
                         "Ele deve ser posicionado na região %s.",
                         pokemon.getNome(), tipoPokemon, nomeRegiao, nomeRegiaoCorreta)
        );
    }
}

/**
 * Obtém o tipo do Pokémon baseado em sua classe
 */
private String obterTipoPokemon(Pokemon pokemon) {
    String nomeClasse = pokemon.getClass().getSimpleName();
    
    if (nomeClasse.contains("Agua") || nomeClasse.contains("Water")) {
        return "AGUA";
    } else if (nomeClasse.contains("Floresta") || nomeClasse.contains("Grass") || nomeClasse.contains("Forest")) {
        return "FLORESTA";
    } else if (nomeClasse.contains("Terra") || nomeClasse.contains("Ground") || nomeClasse.contains("Rock")) {
        return "TERRA";
    } else if (nomeClasse.contains("Eletrico") || nomeClasse.contains("Electric")) {
        return "ELETRICO";
    } else {
        // Fallback: tenta pelo nome do Pokémon
        String nome = pokemon.getNome().toLowerCase();
        if (nome.contains("water") || nome.contains("squirtle") || nome.contains("psyduck")) {
            return "AGUA";
        } else if (nome.contains("grass") || nome.contains("bulbasaur") || nome.contains("oddish")) {
            return "FLORESTA";
        } else if (nome.contains("ground") || nome.contains("rock") || nome.contains("geodude")) {
            return "TERRA";
        } else if (nome.contains("electric") || nome.contains("pikachu") || nome.contains("voltorb")) {
            return "ELETRICO";
        }
        
        // Se não conseguir identificar, assume como FLORESTA por padrão
        return "FLORESTA";
    }
}

/**
 * Converte o código da região para nome legível
 */
private String obterNomeRegiao(String tipoRegiao) {
    switch (tipoRegiao) {
        case "AGUA": return "ÁGUA";
        case "FLORESTA": return "FLORESTA";
        case "TERRA": return "TERRA";
        case "ELETRICO": return "ELÉTRICO";
        default: return tipoRegiao;
    }
}

/**
 * Obtém descrição detalhada da região onde o Pokémon deve ser posicionado
 */
private String obterDescricaoRegiao(String tipoPokemon) {
    int meio = TAMANHO_GRID / 2;
    
    switch (tipoPokemon) {
        case "AGUA":
            return String.format("Pokémon de ÁGUA deve ser posicionado no quadrante SUPERIOR ESQUERDO (linhas 0-%d, colunas 0-%d)", 
                               meio-1, meio-1);
        case "FLORESTA":
            return String.format("Pokémon de FLORESTA deve ser posicionado no quadrante SUPERIOR DIREITO (linhas 0-%d, colunas %d-%d)", 
                               meio-1, meio, TAMANHO_GRID-1);
        case "TERRA":
            return String.format("Pokémon de TERRA deve ser posicionado no quadrante INFERIOR ESQUERDO (linhas %d-%d, colunas 0-%d)", 
                               meio, TAMANHO_GRID-1, meio-1);
        case "ELETRICO":
            return String.format("Pokémon ELÉTRICO deve ser posicionado no quadrante INFERIOR DIREITO (linhas %d-%d, colunas %d-%d)", 
                               meio, TAMANHO_GRID-1, meio, TAMANHO_GRID-1);
        default:
            return "Região não identificada";
    }
}

    }