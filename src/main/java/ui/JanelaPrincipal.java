package ui;

import jogo.MotorJogo;
import modelo.*;
import estrategia.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.border.Border;


public class JanelaPrincipal extends JFrame implements Observador {
    private final MotorJogo motorJogo;
    private JButton[][] botoesGrid;
    private JTextArea areaLog;
    private JLabel statusJogadorLabel;
    private JLabel statusComputadorLabel;
    private JDialog batalhaDialog;
    private JDialog capturaDialog;
    private boolean modoDebug = false;
    private JButton btnDebug;
    private JButton btnMochila; // Novo botão da mochila
    private JLabel lblInstrucoes;
    private int turno;
    private JProgressBar barraHPAliado;
    private JProgressBar barraHPInimigo;
    private JLabel lblStatusBatalha;
    private Pokemon pokemonAliado;
    private Pokemon pokemonInimigo;
private JButton btnDica;
private int celulaSelecionadaX = -1;
private int celulaSelecionadaY = -1;


    public JanelaPrincipal() {
        setTitle("Pokémon - Jogo de Tabuleiro");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        motorJogo = new MotorJogo();
        motorJogo.adicionarObservador(this);

        criarComponentes();
        configurarCliqueCelulas(); // Adicione esta linha
        motorJogo.iniciarNovoJogo();
        
        pack();
        setVisible(true);
    }
    
public int getTurno() {
    return turno;
}


private void criarComponentes() {
    setLayout(new BorderLayout());

    // Painel do Tabuleiro (Centro)
    JPanel painelTabuleiro = new JPanel(new GridLayout(MotorJogo.TAMANHO_GRID, MotorJogo.TAMANHO_GRID));
    botoesGrid = new JButton[MotorJogo.TAMANHO_GRID][MotorJogo.TAMANHO_GRID];

    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            JButton botao = new JButton();
            botao.setPreferredSize(new Dimension(100, 100));
            botao.setFont(new Font("Arial", Font.BOLD, 10));
            botao.setFocusPainted(false);
            final int x = i;
            final int y = j;
            botao.addActionListener(e -> motorJogo.jogar(x, y));
            botoesGrid[i][j] = botao;
            painelTabuleiro.add(botao);
        }
    }
    add(painelTabuleiro, BorderLayout.CENTER);

    // Painel Direito (Status e Log)
    JPanel painelDireito = new JPanel();
    painelDireito.setLayout(new BoxLayout(painelDireito, BoxLayout.Y_AXIS));
    painelDireito.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // === SEÇÃO DE INSTRUÇÕES (NOVA) ===
    lblInstrucoes = new JLabel("", JLabel.CENTER);
    lblInstrucoes.setFont(new Font("Arial", Font.BOLD, 14));
    lblInstrucoes.setForeground(new Color(0, 100, 200)); // Azul escuro
    lblInstrucoes.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0, 100, 200), 2),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)
    ));
    lblInstrucoes.setOpaque(true);
    lblInstrucoes.setBackground(new Color(240, 248, 255)); // Azul muito claro
    
    // Painel para as instruções com altura fixa
    JPanel painelInstrucoes = new JPanel(new BorderLayout());
    painelInstrucoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
    painelInstrucoes.add(lblInstrucoes, BorderLayout.CENTER);
    
    painelDireito.add(painelInstrucoes);
    painelDireito.add(Box.createRigidArea(new Dimension(0, 10)));

    // === SEÇÃO DE STATUS ===
    statusJogadorLabel = new JLabel("Jogador: 0 pts | Pokémon: ");
    statusComputadorLabel = new JLabel("Computador: 0 pts | Pokémon: ");
    
    JPanel statusPanel = new JPanel(new GridLayout(2, 1));
    statusPanel.add(statusJogadorLabel);
    statusPanel.add(statusComputadorLabel);
    
    JLabel lblStatusTitulo = new JLabel("--- STATUS ---");
    lblStatusTitulo.setFont(new Font("Arial", Font.BOLD, 12));
    painelDireito.add(lblStatusTitulo);
    painelDireito.add(statusPanel);

    // === SEÇÃO DE BOTÕES DE CONTROLE ===
    // Botão Novo Jogo
    JButton btnNovoJogo = new JButton("Novo Jogo");
    btnNovoJogo.setBackground(new Color(34, 139, 34)); // Verde floresta
    btnNovoJogo.setForeground(Color.WHITE);
    btnNovoJogo.setFont(new Font("Arial", Font.BOLD, 12));
    btnNovoJogo.addActionListener(e -> {
        motorJogo.iniciarNovoJogo();
        if (modoDebug) {
            revelarMapa();
        }
    });
    
    // Botão Salvar
    JButton btnSalvar = new JButton("Salvar Jogo");
    btnSalvar.setBackground(new Color(70, 130, 180)); // Azul aço
    btnSalvar.setForeground(Color.WHITE);
    btnSalvar.setFont(new Font("Arial", Font.BOLD, 12));
    btnSalvar.addActionListener(e -> salvarJogo());
    
    btnDica = new JButton("💡Dica (3)");
    btnDica.setBackground(new Color(255, 215, 0)); // Cor dourada
    btnDica.setFont(new Font("Arial", Font.BOLD, 12));
    btnDica.addActionListener(e -> usarDica());
    btnDica.setEnabled(false);
    btnDica.setToolTipText("Obter dica sobre Pokémon na linha/coluna (máx. 3 por jogo)");
    
    // Botão Carregar
    JButton btnCarregar = new JButton("Carregar Jogo"); 
    btnCarregar.setBackground(new Color(123, 104, 238)); // Roxo médio
    btnCarregar.setForeground(Color.WHITE);
    btnCarregar.setFont(new Font("Arial", Font.BOLD, 12));
    btnCarregar.addActionListener(e -> carregarJogo());
    
    // Botão da Mochila - MELHORADO!
    btnMochila = new JButton("Mochila");
    btnMochila.setBackground(new Color(100, 149, 237)); // Azul cornflower
    btnMochila.setForeground(Color.WHITE);
    btnMochila.setFont(new Font("Arial", Font.BOLD, 12));
    btnMochila.addActionListener(e -> abrirMochila());
    btnMochila.setToolTipText("Abrir mochila de Pokémons capturados");
    
    // Botão de Debug
    btnDebug = new JButton("DEBUG: OFF");
    btnDebug.setBackground(Color.LIGHT_GRAY);
    btnDebug.setForeground(Color.BLACK);
    btnDebug.setFont(new Font("Arial", Font.BOLD, 12));
    btnDebug.addActionListener(e -> alternarModoDebug());
    btnDebug.setToolTipText("Ativar/Desativar modo debug (revela mapa)");

    // Painel de botões com espaçamento melhorado
    JPanel botoesPanel = new JPanel(new GridLayout(6, 1, 5, 8)); // Aumente para 6 linhas
    botoesPanel.add(btnNovoJogo);
    botoesPanel.add(btnSalvar);
    botoesPanel.add(btnCarregar);
    botoesPanel.add(btnMochila);
    botoesPanel.add(btnDica); // Adicione esta linha
    botoesPanel.add(btnDebug);
    
    painelDireito.add(Box.createRigidArea(new Dimension(0, 15)));
    painelDireito.add(botoesPanel);
    
    // === SEÇÃO DE LOG ===
    areaLog = new JTextArea(15, 30);
    areaLog.setEditable(false);
    areaLog.setFont(new Font("Consolas", Font.PLAIN, 11)); // Fonte monospace
    areaLog.setBackground(new Color(248, 248, 248)); // Cinza muito claro
    areaLog.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JScrollPane scrollPane = new JScrollPane(areaLog);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
    
    JLabel lblLogTitulo = new JLabel("--- LOG ---");
    lblLogTitulo.setFont(new Font("Arial", Font.BOLD, 12));
    
    painelDireito.add(Box.createRigidArea(new Dimension(0, 15)));
    painelDireito.add(lblLogTitulo);
    painelDireito.add(Box.createRigidArea(new Dimension(0, 5)));
    painelDireito.add(scrollPane);

    add(painelDireito, BorderLayout.EAST);

    aplicarCoresRegioes();
}

    // Novo método para abrir a mochila
   private void abrirMochila() {
    Treinador jogador = motorJogo.getJogador();
    if (jogador != null && jogador.getMochila() != null) {
        JanelaMochila janelaMochila = new JanelaMochila(this, jogador);
        janelaMochila.setVisible(true);
        
        // REMOVIDO: SwingUtilities.invokeLater que estava causando o problema
        // Apenas atualiza o status do jogador diretamente
        String statusJogador = "Jogador: " + jogador.getPontuacao() + " pts | Pokémon: " + 
            (jogador.getPokemonAtual() != null ? jogador.getPokemonAtual().getNome() : "Nenhum") +
            " | Time: " + jogador.getTime().size() + "/6 | Mochila: " + 
            jogador.getMochila().getQuantidade() + "/" + jogador.getMochila().getCapacidadeMaxima();
        
        statusJogadorLabel.setText(statusJogador);
        
        // Atualiza visual do botão da mochila
        int quantidade = jogador.getMochila().getQuantidade();
        int capacidade = jogador.getMochila().getCapacidadeMaxima();
        
        if (quantidade > 0) {
            btnMochila.setText("🎒 Mochila (" + quantidade + "/" + capacidade + ")");
            if (quantidade >= capacidade * 0.8) {
                btnMochila.setBackground(new Color(255, 140, 0)); // Laranja
            } else {
                btnMochila.setBackground(new Color(100, 149, 237)); // Azul normal
            }
        } else {
            btnMochila.setText("🎒 Mochila");
            btnMochila.setBackground(new Color(100, 149, 237)); // Azul normal
        }
    } else {
        JOptionPane.showMessageDialog(this, "Erro: Jogador ou mochila não encontrados!", "Erro", JOptionPane.ERROR_MESSAGE);
    }
}


    private void alternarModoDebug() {
        modoDebug = !modoDebug;
        
        if (modoDebug) {
            btnDebug.setText("Debug: ON");
            btnDebug.setBackground(Color.GREEN);
            revelarMapa();
            areaLog.append("[DEBUG] Modo debug ativado - mapa revelado!\n");
        } else {
            btnDebug.setText("Debug: OFF");
            btnDebug.setBackground(Color.LIGHT_GRAY);
            atualizarTabuleiro(); // Volta ao estado normal
            areaLog.append("[DEBUG] Modo debug desativado - mapa normal.\n");
        }
        
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    private void revelarMapa() {
    Celula[][] tabuleiro = motorJogo.getTabuleiro();
    
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            JButton botao = botoesGrid[i][j];
            Celula celula = tabuleiro[i][j];
            
            // Aplica a cor da região primeiro
            aplicarCorRegiao(botao, i, j);
            
            if (!celula.estaVazia() && celula.getPokemon() != null) {
                Pokemon pokemon = celula.getPokemon();
                
                // Carrega a imagem do Pokémon
                carregarImagemPokemonDebug(i, j, pokemon);
                
                // Se é um Pokémon do computador, destaca com borda vermelha
                if (pokemon.getTreinador() != null && 
                    pokemon.getTreinador().getNome().equals("Computador")) {
                    botao.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    // Adiciona um tooltip para identificar
                    botao.setToolTipText("Pokémon do Computador: " + pokemon.getNome() + " (OCULTO)");
                } else if (pokemon.getTreinador() != null && 
                           pokemon.getTreinador().getNome().equals("Jogador")) {
                    botao.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                    botao.setToolTipText("Seu Pokémon: " + pokemon.getNome());
                } else {
                    // Pokémon selvagem
                    botao.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                    botao.setToolTipText("Pokémon Selvagem: " + pokemon.getNome());
                }
            } else {
                // Célula vazia
                botao.setIcon(null);
                botao.setText("Vazia");
                botao.setFont(new Font("Arial", Font.PLAIN, 8));
                botao.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                botao.setToolTipText("Célula vazia");
            }
            
            // No modo debug, permite clicar em qualquer célula não visitada
            // Mas se tem Pokémon do computador oculto, permite clicar para batalhar
            if (celula.foiVisitada()) {
                if (!celula.estaVazia() && celula.getPokemon() != null && 
                    celula.getPokemon().getTreinador() != null &&
                    celula.getPokemon().getTreinador().getNome().equals("Computador")) {
                    // Permite clicar em Pokémon oculto do computador para batalhar
                    botao.setEnabled(true);
                } else {
                    // Célula já visitada e sem Pokémon oculto do computador
                    botao.setEnabled(false);
                }
            } else {
                // Célula não visitada - sempre pode clicar
                botao.setEnabled(true);
            }
        }
    }
}

    private void aplicarCorRegiao(JButton botao, int i, int j) {
        int meio = MotorJogo.TAMANHO_GRID / 2;
        Color cor;
        if (i < meio && j < meio) {
            cor = new Color(173, 216, 230); // Água
        } else if (i < meio && j >= meio) {
            cor = new Color(144, 238, 144); // Floresta
        } else if (i >= meio && j < meio) {
            cor = new Color(210, 180, 140); // Terra
        } else {
            cor = new Color(255, 255, 204); // Elétrico
        }
        botao.setBackground(cor);
    }

    private void carregarImagemPokemonDebug(int x, int y, Pokemon pokemon) {
        String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
        JButton botao = botoesGrid[x][y];
        
        try {
            InputStream is = getClass().getResourceAsStream(nomeIcone);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                botao.setIcon(new ImageIcon(scaledImg));
                botao.setText("");
            } else {
                botao.setIcon(null);
                botao.setText(pokemon.getNome());
                botao.setFont(new Font("Arial", Font.BOLD, 8));
            }
        } catch (IOException e) {
            botao.setIcon(null);
            botao.setText(pokemon.getNome());
            botao.setFont(new Font("Arial", Font.BOLD, 8));
        }
    }

    private void salvarJogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Jogo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.sav)", "sav"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".sav")) {
                file = new File(file.getAbsolutePath() + ".sav");
            }
            motorJogo.salvarJogo(file.getAbsolutePath());
        }
    }

    private void carregarJogo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Carregar Jogo");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.sav)", "sav"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            motorJogo.carregarJogo(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void aplicarCoresRegioes() {
    int meio = MotorJogo.TAMANHO_GRID / 2;
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            aplicarCorRegiao(botoesGrid[i][j], i, j);
            
            botoesGrid[i][j].setIcon(null);
            botoesGrid[i][j].setText("");
            botoesGrid[i][j].setBorder(UIManager.getBorder("Button.border")); // Border padrão
            botoesGrid[i][j].setToolTipText(null); // Remove tooltip
            botoesGrid[i][j].setEnabled(true); // Sempre habilitado inicialmente
        }
    }
}
    
@Override
public void atualizar(String evento, Object dados) {
    SwingUtilities.invokeLater(() -> {
        switch (evento) {
            case "STATUS_ATUALIZADO":
                String[] status = (String[]) dados;
                statusJogadorLabel.setText(status[0]);
                statusComputadorLabel.setText(status[1]);
                
                // Atualiza o visual do botão da mochila baseado na quantidade de pokémons
                Treinador jogador = motorJogo.getJogador();
                if (jogador != null && jogador.getMochila() != null) {
                    int quantidade = jogador.getMochila().getQuantidade();
                    int capacidade = jogador.getMochila().getCapacidadeMaxima();
                    
                    if (quantidade > 0) {
                        btnMochila.setText("🎒 Mochila (" + quantidade + "/" + capacidade + ")");
                        if (quantidade >= capacidade * 0.8) {
                            btnMochila.setBackground(new Color(255, 140, 0)); // Laranja
                        } else {
                            btnMochila.setBackground(new Color(100, 149, 237)); // Azul normal
                        }
                    } else {
                        btnMochila.setText("🎒 Mochila");
                        btnMochila.setBackground(new Color(100, 149, 237)); // Azul normal
                    }
                }
                break;
                
            case "MENSAGEM":
                areaLog.append(dados.toString() + "\n");
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
                break;
                
            case "SOLICITAR_POSICAO_INICIAL":
                mostrarInstrucao("🎯 CLIQUE EM UMA CÉLULA PARA POSICIONAR SEU POKÉMON INICIAL");
                destacarCelulasDisponiveis();
                
                // Habilita todos os botões do tabuleiro para escolha
                for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                    for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                        botoesGrid[i][j].setEnabled(true);
                        botoesGrid[i][j].setToolTipText("🎯 Clique aqui para posicionar seu Pokémon inicial");
                    }
                }
                
                areaLog.append("=== FASE DE POSICIONAMENTO INICIAL ===\n");
                areaLog.append("Escolha uma célula para posicionar seu Pokémon inicial.\n");
                areaLog.append("O computador escolherá sua posição automaticamente após você.\n");
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
                break;
                
            case "POSICIONAMENTO_CONCLUIDO":
                // Muda a instrução para indicar que ambos posicionaram
                mostrarInstrucao("✅ POSICIONAMENTO CONCLUÍDO - EXPLORE O TABULEIRO!");
                
                // Remove destaques após posicionamento concluído
                removerDestaqueCelulas();
                
                areaLog.append("=== POSICIONAMENTO CONCLUÍDO ===\n");
                areaLog.append("Ambos os Pokémons foram posicionados no tabuleiro!\n");
                areaLog.append("Agora você pode explorar clicando nas células.\n");
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
                
                // Após um tempo, remove a instrução
                Timer timer = new Timer(3000, e -> limparInstrucao());
                timer.setRepeats(false);
                timer.start();
                break;
                
            case "POKEMON_ENCONTRADO":
    int[] coords = (int[]) dados;
    
    // Carrega a imagem do Pokémon APENAS se não for posicionamento inicial OU se for modo debug
    Celula[][] tabuleiro = motorJogo.getTabuleiro();
    if (tabuleiro != null && coords[0] < tabuleiro.length && coords[1] < tabuleiro[0].length) {
        Pokemon pokemon = tabuleiro[coords[0]][coords[1]].getPokemon();
        if (pokemon != null) {
            // Se é posicionamento inicial (turno 0) e não é modo debug, não mostra a imagem
            if (motorJogo.getTurno() == 0 && !modoDebug) {
                // Durante posicionamento inicial, mantém oculto
                JButton botao = botoesGrid[coords[0]][coords[1]];
                botao.setIcon(null);
                botao.setText("");
                
                // Apenas aplica a cor da região
                aplicarCorRegiao(botao, coords[0], coords[1]);
                
                // Se tem treinador, é um Pokémon inicial posicionado
                if (pokemon.getTreinador() != null) {
                    // Remove qualquer destaque visual durante posicionamento
                    botao.setBorder(UIManager.getBorder("Button.border"));
                    botao.setToolTipText(null);
                    botao.setEnabled(true); // Mantém clicável para futuras explorações
                }
            } else {
                // Fora do posicionamento inicial OU em modo debug - mostra normalmente
                carregarImagemPokemon(coords[0], coords[1], pokemon);
                
                // Aplica bordas e tooltips normalmente
                JButton botao = botoesGrid[coords[0]][coords[1]];
                if (pokemon.getTreinador() != null) {
                    if (pokemon.getTreinador().getNome().equals("Jogador")) {
                        botao.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                        botao.setToolTipText("Seu Pokémon: " + pokemon.getNome());
                    } else if (pokemon.getTreinador().getNome().equals("Computador")) {
                        if (modoDebug) {
                            botao.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                            botao.setToolTipText("Pokémon do Computador: " + pokemon.getNome() + " (OCULTO)");
                        } else {
                            // Em jogo normal, Pokémon do computador permanece oculto
                            botao.setIcon(null);
                            botao.setText("");
                            aplicarCorRegiao(botao, coords[0], coords[1]);
                            botao.setBorder(UIManager.getBorder("Button.border"));
                            botao.setToolTipText(null);
                        }
                    }
                }
            }
        }
    }
    break;
               case "DICA_USADA":
                int dicasRestantes = (Integer) dados;
                btnDica.setText("💡 Dica (" + dicasRestantes + ")");
                if (dicasRestantes <= 0) {
                    btnDica.setEnabled(false);
                    btnDica.setBackground(Color.LIGHT_GRAY);
                }
                break;
                
            case "CELULA_VAZIA":
                int[] coordsVazia = (int[]) dados;
                JButton botaoVazio = botoesGrid[coordsVazia[0]][coordsVazia[1]];
                botaoVazio.setIcon(null);
                botaoVazio.setText("Vazia");
                botaoVazio.setFont(new Font("Arial", Font.PLAIN, 10));
                botaoVazio.setEnabled(false);
                break;
                
            case "CELULA_VAZIA_SEM_NOME":
                int[] coordsVaziaSemNome = (int[]) dados;
                JButton botaoVazioSemNome = botoesGrid[coordsVaziaSemNome[0]][coordsVaziaSemNome[1]];
                botaoVazioSemNome.setIcon(null);
                botaoVazioSemNome.setText("");
                botaoVazioSemNome.setEnabled(true);
                botaoVazioSemNome.setToolTipText(null);
                break;
                
            case "MOCHILA_ATUALIZADA":
                java.util.List<Pokemon> pokemonsCapturados = (java.util.List<Pokemon>) dados;
                areaLog.append("Mochila atualizada! Total: " + pokemonsCapturados.size() + " Pokémons.\n");
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
                break;
                
            case "JOGO_CARREGADO":
                if (modoDebug) {
                    revelarMapa();
                } else {
                    atualizarTabuleiro();
                }
                break;
                    
            case "BATALHA_INICIADA":
                Pokemon[] pokemons = (Pokemon[]) dados;
                mostrarTelaBatalha(pokemons[0], pokemons[1]);
                break;
                
            case "ATAQUE_REALIZADO":
                Object[] ataqueInfo = (Object[]) dados;
                String atacante = (String) ataqueInfo[0];
                String defensor = (String) ataqueInfo[1];
                int dano = (Integer) ataqueInfo[2];
                int energiaAnterior = (Integer) ataqueInfo[3];
                int energiaAtual = (Integer) ataqueInfo[4];
                
                atualizarBatalhaUITurnos(atacante, defensor, dano, energiaAnterior, energiaAtual);
                break;
                
            case "TURNO_JOGADOR":
                Pokemon[] pokemonsBatalha = (Pokemon[]) dados;
                habilitarBotaoAtaque(true);
                areaLog.append("É seu turno! Clique em 'Atacar' para continuar.\n");
                break;
                    
            case "BATALHA_TERMINADA":
                Pokemon vencedor = (Pokemon) dados;
                encerrarBatalha(vencedor);
                break;
                    
            case "POKEMON_SELVAGEM_ENCONTRADO":
                mostrarTelaCaptura((Pokemon) dados);
                break;
                
            case "POKEMON_CAPTURADO":
    Pokemon capturado = (Pokemon) dados;
    
    // Mostra o diálogo de captura
    JOptionPane.showMessageDialog(this, "Você capturou " + capturado.getNome() + "!");
    
    // Encontra a posição do Pokémon capturado no tabuleiro e OCULTA COMPLETAMENTE
    Celula[][] tabuleiroAtual = motorJogo.getTabuleiro();
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            Celula celula = tabuleiroAtual[i][j];
            
            // Encontra o Pokémon capturado no tabuleiro
            if (!celula.estaVazia() && celula.getPokemon() == capturado) {
                JButton botao = botoesGrid[i][j];
                
                // OCULTA COMPLETAMENTE o Pokémon visualmente
                botao.setIcon(null);
                botao.setText("");
                botao.setEnabled(true); // MANTÉM CLICÁVEL
                
                // CORREÇÃO: Remove TODAS as bordas especiais
                botao.setBorder(UIManager.getBorder("Button.border"));
                botao.setToolTipText(null); // Remove tooltip completamente
                
                // Restaura APENAS a cor da região (sem misturar cores)
                aplicarCorRegiao(botao, i, j);
                
                // Marca a célula como visitada para que não apareça como "nova"
                celula.setVisitada(true);
                break;
            }
        }
    }
    break;
    case "REGENERACAO_REALIZADA":
                Object[] regenInfo = (Object[]) dados;
                String nomeRegenerador = (String) regenInfo[0];
                int energiaAntes = (Integer) regenInfo[1];
                int energiaDepois = (Integer) regenInfo[2];
                int energiaMaxima = (Integer) regenInfo[3];
                int quantidadeRegenerada = (Integer) regenInfo[4];
                
                // Determina qual barra atualizar
                JProgressBar barraParaRegenerar = null;
                if (nomeRegenerador.equals(pokemonAliado.getNome())) {
                    barraParaRegenerar = barraHPAliado;
                } else if (nomeRegenerador.equals(pokemonInimigo.getNome())) {
                    barraParaRegenerar = barraHPInimigo;
                }
                
                if (barraParaRegenerar != null) {
                    // Aplica animação de regeneração (crescimento da barra)
                    atualizarBarraHPComRegeneracao(barraParaRegenerar, energiaAntes, energiaDepois, energiaMaxima);
                    
                    // Adiciona efeito visual de cura
                    adicionarEfeitoVisualCura(barraParaRegenerar, quantidadeRegenerada);
                }
                
                // Atualiza o log
                areaLog.append("🌱 " + nomeRegenerador + " se regenerou e recuperou " + 
                              quantidadeRegenerada + " pontos de energia! (" + 
                              energiaAntes + " → " + energiaDepois + " HP)\n");
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
                break;
        }
    });
}


private void atualizarTabuleiro() {
    Celula[][] tabuleiro = motorJogo.getTabuleiro();
    
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            JButton botao = botoesGrid[i][j];
            Celula celula = tabuleiro[i][j];
            
            // Aplica a cor da região primeiro
            aplicarCorRegiao(botao, i, j);
            
            // Se a célula foi visitada, mostra o conteúdo
            if (celula.foiVisitada()) {
                if (!celula.estaVazia() && celula.getPokemon() != null) {
                    // MANTÉM a imagem/texto do pokémon que estava lá
                    carregarImagemPokemon(i, j, celula.getPokemon());
                    
                    // Verifica se é um pokémon do jogador para manter habilitado
                    Pokemon pokemon = celula.getPokemon();
                    if (pokemon.getTreinador() != null && 
                        pokemon.getTreinador().getNome().equals("Jogador")) {
                        // CORREÇÃO: Pokémon do jogador - mantém habilitado com borda PADRÃO
                        botao.setEnabled(true);
                        botao.setBorder(UIManager.getBorder("Button.border")); // BORDA PADRÃO
                        botao.setToolTipText("Seu Pokémon: " + pokemon.getNome());
                    } else if (pokemon.getTreinador() != null && 
                               pokemon.getTreinador().getNome().equals("Computador")) {
                        // Pokémon do computador - mantém habilitado para batalha APENAS se não está em modo debug
                        if (modoDebug) {
                            // Em modo debug, mostra normalmente
                            botao.setEnabled(true);
                            botao.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                            botao.setToolTipText("Pokémon do Computador: " + pokemon.getNome());
                        } else {
                            // Em jogo normal, Pokémon do computador fica OCULTO até ser descoberto
                            botao.setIcon(null);
                            botao.setText("");
                            botao.setBorder(UIManager.getBorder("Button.border"));
                            botao.setToolTipText(null);
                            botao.setEnabled(true);
                        }
                    } else {
                        // Pokémon capturado/derrotado - desabilitado
                        botao.setEnabled(false);
                        botao.setBorder(UIManager.getBorder("Button.border"));
                    }
                } else {
                    // Célula vazia visitada (pode ter sido uma captura)
                    botao.setIcon(null);
                    botao.setText("Vazia");
                    botao.setFont(new Font("Arial", Font.PLAIN, 10));
                    botao.setEnabled(false);
                    botao.setBorder(UIManager.getBorder("Button.border"));
                    botao.setToolTipText("Célula vazia");
                }
            } else {
                // Células não visitadas - reseta visual mas mantém habilitadas
                botao.setIcon(null);
                botao.setText("");
                botao.setBorder(UIManager.getBorder("Button.border"));
                botao.setToolTipText(null);
                botao.setEnabled(true);
            }
        }
    }
}

// Substitua o método carregarImagemPokemon por esta versão corrigida:

private void carregarImagemPokemon(int x, int y, Pokemon pokemon) {
    Celula celula = motorJogo.getTabuleiro()[x][y];
    JButton botao = botoesGrid[x][y];
    
    // Só processa se a célula foi visitada OU se está em modo debug
    if (celula.foiVisitada() || modoDebug) {
        
        // VERIFICAÇÃO PRINCIPAL: Se o Pokémon pertence ao jogador mas NÃO está no time ativo,
        // significa que foi capturado e deve ficar COMPLETAMENTE CAMUFLADO
        if (pokemon.getTreinador() != null && 
            pokemon.getTreinador().getNome().equals("Jogador") && 
            !motorJogo.getJogador().getTime().contains(pokemon)) {
            
            // Pokémon foi capturado pelo jogador - fica COMPLETAMENTE oculto
            botao.setIcon(null);
            botao.setText("");
            aplicarCorRegiao(botao, x, y);
            
            // CORREÇÃO: Remove TODAS as bordas especiais e volta ao padrão
            botao.setBorder(UIManager.getBorder("Button.border"));
            botao.setToolTipText(null); // Remove tooltip
            botao.setEnabled(true); // Mantém clicável para futuras interações
            
            // IMPORTANTE: Não aplica nenhuma cor especial, apenas a cor da região
            return;
        }
        
        // VERIFICAÇÃO: Se o Pokémon do computador foi capturado pelo jogador
        if (pokemon.getTreinador() != null && 
            pokemon.getTreinador().getNome().equals("Computador") && 
            !motorJogo.getComputador().getTime().contains(pokemon)) {
            
            // Pokémon do computador foi derrotado/removido - oculta completamente
            botao.setIcon(null);
            botao.setText("");
            aplicarCorRegiao(botao, x, y);
            botao.setBorder(UIManager.getBorder("Button.border"));
            botao.setToolTipText(null);
            botao.setEnabled(true);
            return;
        }
        
        // VERIFICAÇÃO: Se o Pokémon não tem treinador E a célula está vazia,
        // significa que foi capturado - não deve ser exibido
        if (pokemon.getTreinador() == null && celula.estaVazia()) {
            // Pokémon foi capturado - não exibe
            botao.setIcon(null);
            botao.setText("Vazia");
            botao.setFont(new Font("Arial", Font.PLAIN, 10));
            aplicarCorRegiao(botao, x, y);
            botao.setBorder(UIManager.getBorder("Button.border"));
            botao.setToolTipText(null);
            return;
        }
        
        String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
        
        try {
            InputStream is = getClass().getResourceAsStream(nomeIcone);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                botao.setIcon(new ImageIcon(scaledImg));
                botao.setText(""); // Remove texto quando tem imagem
            } else {
                // Se não encontrar a imagem, usa texto
                botao.setIcon(null);
                botao.setText(pokemon.getNome());
                botao.setFont(new Font("Arial", Font.BOLD, 10));
            }
        } catch (IOException e) {
            // Em caso de erro, usa texto
            botao.setIcon(null);
            botao.setText(pokemon.getNome());
            botao.setFont(new Font("Arial", Font.BOLD, 10));
        }
        
        // Define cor de fundo e bordas baseada no treinador APENAS para Pokémons ATIVOS
        if (pokemon.getTreinador() != null) {
            if (pokemon.getTreinador().getNome().equals("Jogador") &&
                motorJogo.getJogador().getTime().contains(pokemon)) {
                // CORREÇÃO: Pokémon do jogador ATIVO - mantém borda padrão como os demais
                Color corRegiao = botao.getBackground();
                Color corMisturada = new Color(
                    (corRegiao.getRed() + 173) / 2,
                    (corRegiao.getGreen() + 216) / 2,
                    (corRegiao.getBlue() + 230) / 2
                );
                botao.setBackground(corMisturada);
                // MUDANÇA PRINCIPAL: Usa borda padrão em vez de borda azul especial
                botao.setBorder(UIManager.getBorder("Button.border"));
                botao.setToolTipText("Seu Pokémon: " + pokemon.getNome());
                
            } else if (pokemon.getTreinador().getNome().equals("Computador") &&
                       motorJogo.getComputador().getTime().contains(pokemon)) {
                // Pokémon do computador ATIVO - só exibe se for modo debug
                if (modoDebug) {
                    Color corRegiao = botao.getBackground();
                    Color corMisturada = new Color(
                        Math.min(255, (corRegiao.getRed() + 255) / 2),
                        (corRegiao.getGreen() + 182) / 2,
                        (corRegiao.getBlue() + 193) / 2
                    );
                    botao.setBackground(corMisturada);
                    // No modo debug, mantém borda vermelha para distinguir do jogador
                    botao.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
                    botao.setToolTipText("Pokémon do Computador: " + pokemon.getNome() + " (OCULTO)");
                } else {
                    // Em modo normal, oculta Pokémon do computador ATIVO
                    botao.setIcon(null);
                    botao.setText("");
                    aplicarCorRegiao(botao, x, y);
                    botao.setBorder(UIManager.getBorder("Button.border"));
                    botao.setToolTipText(null);
                }
            }
        } else {
            // Pokémon selvagem - mantém visual padrão com cor da região
            aplicarCorRegiao(botao, x, y);
            botao.setBorder(UIManager.getBorder("Button.border"));
            botao.setToolTipText("Pokémon Selvagem: " + pokemon.getNome());
        }
        
    } else {
        // Célula não visitada e não está em modo debug - mantém oculto
        botao.setIcon(null);
        botao.setText("");
        aplicarCorRegiao(botao, x, y);
        botao.setBorder(UIManager.getBorder("Button.border"));
        botao.setToolTipText(null);
    }
}

private void mostrarTelaBatalha(Pokemon aliado, Pokemon inimigo) {
    // Armazena referências dos pokémons
    this.pokemonAliado = aliado;
    this.pokemonInimigo = inimigo;
    
    batalhaDialog = new JDialog(this, "Batalha Pokémon", true);
    batalhaDialog.setLayout(new BorderLayout());
    batalhaDialog.setSize(700, 500);
    batalhaDialog.setLocationRelativeTo(this);

    // Painel principal da batalha
    JPanel painelBatalha = new JPanel(new GridLayout(1, 2, 10, 0));
    
    // Painel do Pokémon Aliado (Jogador)
    JPanel painelAliado = criarPainelPokemonBatalha(aliado, true);
    
    // Painel do Pokémon Inimigo (Computador)
    JPanel painelInimigo = criarPainelPokemonBatalha(inimigo, false);

    painelBatalha.add(painelAliado);
    painelBatalha.add(painelInimigo);

    batalhaDialog.add(painelBatalha, BorderLayout.CENTER);
    
    // Painel de controles da batalha
    JPanel painelControles = new JPanel(new FlowLayout());
    
    JButton btnAtacar = new JButton("⚔️ Atacar!");
    btnAtacar.setBackground(new Color(255, 69, 0)); // Vermelho alaranjado
    btnAtacar.setForeground(Color.WHITE);
    btnAtacar.setFont(new Font("Arial", Font.BOLD, 14));
    btnAtacar.addActionListener(e -> {
        motorJogo.jogar(-1, -1); // Código especial para processar turno de batalha
        habilitarBotaoAtaque(false); // Desabilita o botão após o ataque
    });
    
    JButton btnDesistir = new JButton("🏃 Desistir");
    btnDesistir.setBackground(Color.GRAY);
    btnDesistir.setForeground(Color.WHITE);
    btnDesistir.addActionListener(e -> {
        int opcao = JOptionPane.showConfirmDialog(
            batalhaDialog, 
            "Tem certeza que deseja desistir da batalha?", 
            "Confirmar Desistência", 
            JOptionPane.YES_NO_OPTION
        );
        if (opcao == JOptionPane.YES_OPTION) {
            motorJogo.desistirBatalha();
            batalhaDialog.dispose();
        }
    });
    
    painelControles.add(btnAtacar);
    painelControles.add(btnDesistir);
    
    batalhaDialog.add(painelControles, BorderLayout.SOUTH);
    
    // Label de status da batalha
    lblStatusBatalha = new JLabel("É seu turno! Clique em 'Atacar' para começar.", JLabel.CENTER);
    lblStatusBatalha.setFont(new Font("Arial", Font.BOLD, 12));
    batalhaDialog.add(lblStatusBatalha, BorderLayout.NORTH);
    
    batalhaDialog.setVisible(true);
}

    
private Color getCorBarraHP(int energiaAtual, int energiaMaxima) {
    double percentual = (double) energiaAtual / energiaMaxima;
    
    if (percentual <= 0) {
        return new Color(128, 128, 128);  // Cinza para KO
    } else if (percentual <= 0.15) {
        return new Color(255, 0, 0);      // Vermelho crítico
    } else if (percentual <= 0.25) {
        return new Color(255, 69, 0);     // Vermelho alaranjado
    } else if (percentual <= 0.50) {
        return new Color(255, 140, 0);    // Laranja
    } else if (percentual <= 0.75) {
        return new Color(255, 215, 0);    // Amarelo/dourado
    } else {
        return new Color(0, 255, 0);      // Verde saudável
    }
}



private void atualizarBarraHPComAnimacao(JProgressBar barra, int energiaAnterior, int energiaAtual, int energiaMaxima) {
    if (barra == null) return;
    
    Timer animationTimer = new Timer(30, null); // Animação mais suave (30ms)
    final int diferenca = energiaAnterior - energiaAtual;
    final int passos = Math.max(20, Math.min(diferenca * 2, 50)); // Entre 20 e 50 frames
    final double incremento = (double) diferenca / passos;
    
    // Efeito de "shake" na barra durante o dano
    Timer shakeTimer = new Timer(100, null);
    shakeTimer.addActionListener(new ActionListener() {
        private int shakeCount = 0;
        @Override
        public void actionPerformed(ActionEvent e) {
            if (shakeCount < 6) {
                // Alterna entre vermelho claro e a cor normal
                Color corOriginal = getCorBarraHP(barra.getValue(), energiaMaxima);
                barra.setForeground(shakeCount % 2 == 0 ? Color.RED : corOriginal);
                shakeCount++;
            } else {
                shakeTimer.stop();
            }
        }
    });
    shakeTimer.start();
    
    animationTimer.addActionListener(new ActionListener() {
        private int contador = 0;
        private double valorAtual = energiaAnterior;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            contador++;
            valorAtual -= incremento;
            
            int novoValor = Math.max(energiaAtual, (int) Math.round(valorAtual));
            
            // Atualiza a barra
            barra.setValue(novoValor);
            barra.setString("HP: " + novoValor + "/" + energiaMaxima);
            
            // Atualiza a cor dinamicamente conforme o HP diminui
            Color novaCor = getCorBarraHP(novoValor, energiaMaxima);
            barra.setForeground(novaCor);
            
            // Efeito de piscada quando HP fica crítico
            if (novoValor <= energiaMaxima * 0.15) {
                barra.setForeground(contador % 4 < 2 ? Color.RED : new Color(255, 100, 100));
            }
            
            // Para a animação quando chegou ao valor final
            if (contador >= passos || novoValor <= energiaAtual) {
                barra.setValue(energiaAtual);
                barra.setString("HP: " + energiaAtual + "/" + energiaMaxima);
                barra.setForeground(getCorBarraHP(energiaAtual, energiaMaxima));
                animationTimer.stop();
                
                // Se HP chegou a zero, adiciona efeito especial
                if (energiaAtual <= 0) {
                    adicionarEfeitoKnockout(barra);
                }
            }
        }
    });
    
    animationTimer.start();
}

   

private void encerrarBatalha(Pokemon vencedor) {
    if (batalhaDialog != null) {
        // Efeito especial para o vencedor
        JProgressBar barraVencedor = null;
        if (vencedor == pokemonAliado && barraHPAliado != null) {
            barraVencedor = barraHPAliado;
        } else if (vencedor == pokemonInimigo && barraHPInimigo != null) {
            barraVencedor = barraHPInimigo;
        }
        
        // Anima a barra do vencedor com efeito dourado
        if (barraVencedor != null) {
            final JProgressBar barraFinal = barraVencedor; // Torna a variável final
            Timer vencedorTimer = new Timer(300, null);
            vencedorTimer.addActionListener(new ActionListener() {
                private int contador = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (contador < 10) {
                        Color cor = contador % 2 == 0 ? new Color(255, 215, 0) : Color.YELLOW;
                        barraFinal.setForeground(cor);
                        barraFinal.setString("🏆 VENCEDOR! HP: " + barraFinal.getValue() + "/" + barraFinal.getMaximum());
                        contador++;
                    } else {
                        ((Timer) e.getSource()).stop();
                    }
                }
            });
            vencedorTimer.start();
        }
        
        // Mostra resultado da batalha
        String mensagem;
        if (vencedor == pokemonAliado) {
            mensagem = "🎉 VITÓRIA! " + vencedor.getNome() + " venceu a batalha!";
        } else {
            mensagem = "💀 DERROTA! " + vencedor.getNome() + " venceu a batalha!";
        }
        
        // Atualiza o status final
        if (lblStatusBatalha != null) {
            lblStatusBatalha.setText(mensagem);
        }
        
        // Aguarda um pouco antes de mostrar o diálogo final
        Timer finalTimer = new Timer(3000, e -> {
            JOptionPane.showMessageDialog(batalhaDialog, mensagem, "Fim da Batalha", JOptionPane.INFORMATION_MESSAGE);
            batalhaDialog.dispose();
            batalhaDialog = null;
            
            // Limpa as referências
            barraHPAliado = null;
            barraHPInimigo = null;
            lblStatusBatalha = null;
            pokemonAliado = null;
            pokemonInimigo = null;
        });
        finalTimer.setRepeats(false);
        finalTimer.start();
    }
    
    areaLog.append("🏆 " + vencedor.getNome() + " venceu a batalha!\n");
    areaLog.setCaretPosition(areaLog.getDocument().getLength());
}

    private void mostrarTelaCaptura(Pokemon pokemon) {
        capturaDialog = new JDialog(this, "Pokémon Selvagem Encontrado!", true);
        capturaDialog.setLayout(new BorderLayout());
        capturaDialog.setSize(400, 300);
        capturaDialog.setLocationRelativeTo(this);

        JPanel painelCaptura = new JPanel(new BorderLayout());
        
        // Imagem do Pokémon
        try {
            String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
            InputStream is = getClass().getResourceAsStream(nomeIcone);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                painelCaptura.add(new JLabel(new ImageIcon(scaledImg)), BorderLayout.CENTER);
            } else {
                painelCaptura.add(new JLabel(pokemon.getNome()), BorderLayout.CENTER);
            }
        } catch (IOException e) {
            painelCaptura.add(new JLabel(pokemon.getNome()), BorderLayout.CENTER);
        }

        JLabel lblMensagem = new JLabel("Um " + pokemon.getNome() + " selvagem apareceu!", JLabel.CENTER);
        painelCaptura.add(lblMensagem, BorderLayout.NORTH);
        
        JPanel botoesPanel = new JPanel(new GridLayout(1, 2));
        
        JButton btnCapturar = new JButton("Tentar Capturar");
        btnCapturar.addActionListener(e -> {
            motorJogo.jogar(-2, -2); // Código especial para tentar capturar
            capturaDialog.dispose();
        });
        
        JButton btnFugir = new JButton("Fugir");
        btnFugir.addActionListener(e -> {
            motorJogo.jogar(-3, -3); // Código especial para fugir
            capturaDialog.dispose();
        });
        
        botoesPanel.add(btnCapturar);
        botoesPanel.add(btnFugir);
        
        painelCaptura.add(botoesPanel, BorderLayout.SOUTH);
        capturaDialog.add(painelCaptura);
        capturaDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JanelaPrincipal());
    }
    
    private JPanel criarPainelPokemonBatalha(Pokemon pokemon, boolean aliado) {
    JPanel painel = new JPanel(new BorderLayout());
    String titulo = (aliado ? "SEU POKÉMON: " : "OPONENTE: ") + pokemon.getNome();
    painel.setBorder(BorderFactory.createTitledBorder(titulo));
    
    // Painel da imagem
    JPanel painelImagem = new JPanel(new BorderLayout());
    try {
        String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
        InputStream is = getClass().getResourceAsStream(nomeIcone);
        
        if (is != null) {
            BufferedImage img = ImageIO.read(is);
            Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            painelImagem.add(new JLabel(new ImageIcon(scaledImg)), BorderLayout.CENTER);
        } else {
            JLabel lblNome = new JLabel(pokemon.getNome(), JLabel.CENTER);
            lblNome.setFont(new Font("Arial", Font.BOLD, 16));
            painelImagem.add(lblNome, BorderLayout.CENTER);
        }
    } catch (IOException e) {
        JLabel lblNome = new JLabel(pokemon.getNome(), JLabel.CENTER);
        lblNome.setFont(new Font("Arial", Font.BOLD, 16));
        painelImagem.add(lblNome, BorderLayout.CENTER);
    }
    
    painel.add(painelImagem, BorderLayout.CENTER);
    
    // Painel de estatísticas
    JPanel painelStats = new JPanel(new GridLayout(5, 1, 2, 2)); // Aumentado para 5 linhas
    
    // Barra de HP com design melhorado
    JProgressBar barraHP = new JProgressBar(0, pokemon.getEnergiaMaxima());
    barraHP.setValue(pokemon.getEnergia());
    barraHP.setString("HP: " + pokemon.getEnergia() + "/" + pokemon.getEnergiaMaxima());
    barraHP.setStringPainted(true);
    barraHP.setForeground(getCorBarraHP(pokemon.getEnergia(), pokemon.getEnergiaMaxima()));
    barraHP.setBackground(new Color(200, 200, 200));
    barraHP.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    barraHP.setFont(new Font("Arial", Font.BOLD, 11));
    
    // Armazena a referência da barra para atualização posterior
    if (aliado) {
        this.barraHPAliado = barraHP;
    } else {
        this.barraHPInimigo = barraHP;
    }
    
    // Labels de estatísticas
    JLabel lblNivel = new JLabel("Nível: " + pokemon.getNivel(), JLabel.CENTER);
    JLabel lblForca = new JLabel("Força: " + pokemon.getForca(), JLabel.CENTER);
    JLabel lblExp = new JLabel("Exp: " + pokemon.getNivel(), JLabel.CENTER);
    
    // Label de status do Pokémon (novo)
    JLabel lblStatusPokemon = new JLabel("Status: Pronto", JLabel.CENTER);
    lblStatusPokemon.setFont(new Font("Arial", Font.ITALIC, 10));
    lblStatusPokemon.setForeground(Color.BLUE);
    
    painelStats.add(barraHP);
    painelStats.add(lblNivel);
    painelStats.add(lblForca);
    painelStats.add(lblExp);
    painelStats.add(lblStatusPokemon);
    
    painel.add(painelStats, BorderLayout.SOUTH);
    
    return painel;
}
    private void atualizarBatalhaUITurnos(String nomeAtacante, String nomeDefensor, int dano, int energiaAnterior, int energiaAtual) {
    if (batalhaDialog == null) return;
    
    // Determina qual barra atualizar baseado no nome do defensor
    JProgressBar barraParaAtualizar = null;
    Pokemon pokemonDefensor = null;
    
    // Comparação mais robusta (ignora case e espaços)
    String nomeDefensorNormalizado = nomeDefensor.trim().toLowerCase();
    
    if (pokemonAliado != null && pokemonAliado.getNome().trim().toLowerCase().equals(nomeDefensorNormalizado)) {
        barraParaAtualizar = barraHPAliado;
        pokemonDefensor = pokemonAliado;
    } else if (pokemonInimigo != null && pokemonInimigo.getNome().trim().toLowerCase().equals(nomeDefensorNormalizado)) {
        barraParaAtualizar = barraHPInimigo;
        pokemonDefensor = pokemonInimigo;
    }
    
    if (barraParaAtualizar != null && pokemonDefensor != null) {
        // Garante que os valores estão dentro dos limites
        energiaAtual = Math.max(0, Math.min(energiaAtual, pokemonDefensor.getEnergiaMaxima()));
        
        // Atualiza a barra com animação
        atualizarBarraHPComAnimacao(barraParaAtualizar, energiaAnterior, energiaAtual, pokemonDefensor.getEnergiaMaxima());
        
        // Adiciona efeito visual de dano
        adicionarEfeitoVisualDano(barraParaAtualizar, dano);
        
        // Atualiza o log com mais detalhes
        String statusHP = " (" + energiaAnterior + " → " + energiaAtual + " HP)";
        String efeitoDesc = "";
        
        if (dano > pokemonDefensor.getEnergiaMaxima() * 0.3) {
            efeitoDesc = " 💥SUPER EFETIVO!";
        } else if (dano < pokemonDefensor.getEnergiaMaxima() * 0.1) {
            efeitoDesc = "️ Pouco efetivo...";
        }
        
        areaLog.append(nomeAtacante + " atacou " + nomeDefensor + " causando " + dano + " de dano!" + 
                      efeitoDesc + statusHP + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
        
        // Atualiza status na tela de batalha
        if (lblStatusBatalha != null) {
            lblStatusBatalha.setText("💢 " + nomeAtacante + " causou " + dano + " de dano em " + nomeDefensor + "!");
        }
    } else {
        areaLog.append("[ERRO] Não foi possível atualizar a barra de HP para " + nomeDefensor + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}
    
private void habilitarBotaoAtaque(boolean habilitar) {
    if (batalhaDialog != null) {
        Component painelControles = batalhaDialog.getContentPane().getComponent(2); // BorderLayout.SOUTH
        if (painelControles instanceof JPanel) {
            Component btnAtacar = ((JPanel) painelControles).getComponent(0);
            if (btnAtacar instanceof JButton) {
                btnAtacar.setEnabled(habilitar);
                if (habilitar) {
                    ((JButton) btnAtacar).setText("⚔️ Atacar!");
                    ((JButton) btnAtacar).setBackground(new Color(255, 69, 0));
                } else {
                    ((JButton) btnAtacar).setText("⏳ Aguarde...");
                    ((JButton) btnAtacar).setBackground(Color.GRAY);
                }
            }
        }
        
        // Atualiza o label de status
        if (lblStatusBatalha != null) {
            if (habilitar) {
                lblStatusBatalha.setText("💪 É seu turno! Clique em 'Atacar'.");
            } else {
                lblStatusBatalha.setText("🤖 Turno do computador... Preparando ataque!");
            }
        }
    }
}

private void mostrarInstrucao(String texto) {
    lblInstrucoes.setText(texto);
    lblInstrucoes.setVisible(true);
    
    // Define cor destacada para instruções importantes
    lblInstrucoes.setForeground(new Color(255, 100, 0)); // Laranja vibrante
    lblInstrucoes.setBackground(new Color(255, 255, 224)); // Amarelo claro
    
    // Anima a instrução piscando levemente
    Timer timer = new Timer(800, null);
    timer.addActionListener(new ActionListener() {
        private int contador = 0;
        @Override
        public void actionPerformed(ActionEvent e) {
            if (contador < 6) { // Pisca 3 vezes
                lblInstrucoes.setForeground(contador % 2 == 0 ? 
                    new Color(255, 50, 50) : new Color(255, 100, 0)); // Alterna entre vermelho e laranja
                contador++;
            } else {
                lblInstrucoes.setForeground(new Color(255, 100, 0)); // Volta ao laranja
                timer.stop();
            }
        }
    });
    timer.start();
}

/**
 * Limpa a instrução
 */
private void limparInstrucao() {
    lblInstrucoes.setText("");
    lblInstrucoes.setVisible(false);
    lblInstrucoes.setForeground(new Color(0, 100, 200)); // Volta à cor padrão
    lblInstrucoes.setBackground(new Color(240, 248, 255)); // Volta ao fundo padrão
}
/**
 * Método melhorado para destacar células disponíveis para posicionamento inicial
 */
private void destacarCelulasDisponiveis() {
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            JButton botao = botoesGrid[i][j];
            
            // Aplica a cor da região primeiro
            aplicarCorRegiao(botao, i, j);
            
            // Adiciona borda verde piscante para indicar disponibilidade
            botao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 255, 0), 2),
                BorderFactory.createRaisedBevelBorder()
            ));
            
            botao.setEnabled(true);
            botao.setIcon(null);
            botao.setText(""); // Remove qualquer texto
            botao.setToolTipText("🎯 Clique aqui para posicionar seu Pokémon inicial");
            
            // Remove listeners antigos para evitar conflitos
            java.awt.event.MouseListener[] listeners = botao.getMouseListeners();
            for (java.awt.event.MouseListener listener : listeners) {
                String listenerStr = listener.toString();
                if (listenerStr.contains("posicionamento") || listenerStr.contains("$")) {
                    botao.removeMouseListener(listener);
                }
            }
            
            // Adiciona efeito hover
            botao.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (botao.isEnabled()) {
                        botao.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 215, 0), 3), // Ouro
                            BorderFactory.createRaisedBevelBorder()
                        ));
                    }
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (botao.isEnabled()) {
                        botao.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(0, 255, 0), 2), // Verde
                            BorderFactory.createRaisedBevelBorder()
                        ));
                    }
                }
                
                @Override
                public String toString() {
                    return "posicionamento_hover_listener"; // Identificador para remoção
                }
            });
        }
    }
}

private void removerDestaqueCelulas() {
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            JButton botao = botoesGrid[i][j];
            
            // Remove todos os listeners de mouse para evitar conflitos
            java.awt.event.MouseListener[] listeners = botao.getMouseListeners();
            for (java.awt.event.MouseListener listener : listeners) {
                String listenerStr = listener.toString();
                if (listenerStr.contains("posicionamento") || listenerStr.contains("$")) {
                    botao.removeMouseListener(listener);
                }
            }
            
            // Restaura borda padrão
            botao.setBorder(UIManager.getBorder("Button.border"));
            botao.setToolTipText(null);
            
            // Aplica cor da região
            aplicarCorRegiao(botao, i, j);
            
            // IMPORTANTE: Remove qualquer ícone ou texto que possa ter sido adicionado durante o posicionamento
            botao.setIcon(null);
            botao.setText("");
            botao.setEnabled(true);
        }
    }
    
    // Força atualização do tabuleiro, mas sem revelar Pokémons ocultos
    if (!modoDebug) {
        // Em modo normal, aplica apenas as cores das regiões
        aplicarCoresRegioes();
    } else {
        // Em modo debug, mostra tudo
        revelarMapa();
    }
}

private void mostrarStatusPosicionamento() {
    if (motorJogo.getTurno() == 0) {
        // Durante a fase de posicionamento inicial
        boolean jogadorPosicionou = false;
        boolean computadorPosicionou = false;
        
        Celula[][] tabuleiro = motorJogo.getTabuleiro();
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                Pokemon pokemon = tabuleiro[i][j].getPokemon();
                if (pokemon != null && pokemon.getTreinador() != null) {
                    if (pokemon.getTreinador().getNome().equals("Jogador")) {
                        jogadorPosicionou = true;
                    } else if (pokemon.getTreinador().getNome().equals("Computador")) {
                        computadorPosicionou = true;
                    }
                }
            }
        }
        
        String status = "Posicionamento: ";
        status += jogadorPosicionou ? "✅ Jogador " : "⏳ Jogador ";
        status += computadorPosicionou ? "✅ Computador" : "⏳ Computador";
        
        mostrarInstrucao(status);
    }
}
private void adicionarEfeitoVisualDano(JProgressBar barra, int dano) {
    // Cria um label temporário mostrando o dano
    JLabel lblDano = new JLabel("-" + dano, JLabel.CENTER);
    lblDano.setFont(new Font("Arial", Font.BOLD, 16));
    lblDano.setForeground(Color.RED);
    lblDano.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    lblDano.setOpaque(true);
    lblDano.setBackground(new Color(255, 255, 255, 200)); // Fundo semi-transparente
    
    // Adiciona o label sobre a barra temporariamente
    if (barra.getParent() != null) {
        JPanel container = (JPanel) barra.getParent();
        container.setComponentZOrder(lblDano, 0);
        container.add(lblDano);
        
        // Anima o label subindo e desaparecendo
        Timer efeitoTimer = new Timer(50, null);
        efeitoTimer.addActionListener(new ActionListener() {
            private int contador = 0;
            private int posicaoY = lblDano.getY();
            
            @Override
            public void actionPerformed(ActionEvent e) {
                contador++;
                
                // Move o label para cima
                lblDano.setLocation(lblDano.getX(), posicaoY - contador * 2);
                
                // Fade out
                float alpha = Math.max(0f, 1f - (contador / 30f));
                lblDano.setForeground(new Color(255, 0, 0, (int)(255 * alpha)));
                
                if (contador >= 30) {
                    container.remove(lblDano);
                    container.repaint();
                    efeitoTimer.stop();
                }
            }
        });
        efeitoTimer.start();
    }
}
private void adicionarEfeitoKnockout(JProgressBar barra) {
    // Efeito de "knockout" - barra pisca e fica cinza
    Timer koTimer = new Timer(200, null);
    koTimer.addActionListener(new ActionListener() {
        private int piscadas = 0;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (piscadas < 6) {
                Color cor = piscadas % 2 == 0 ? Color.RED : Color.GRAY;
                barra.setForeground(cor);
                barra.setString(piscadas % 2 == 0 ? "💀 K.O." : "HP: 0/" + barra.getMaximum());
                piscadas++;
            } else {
                barra.setForeground(Color.GRAY);
                barra.setString("💀 DERROTADO");
                koTimer.stop();
            }
        }
    });
    koTimer.start();
}
private void atualizarBarraHPComRegeneracao(JProgressBar barra, int energiaAnterior, int energiaAtual, int energiaMaxima) {
    if (barra == null) return;
    
    Timer animationTimer = new Timer(40, null); // Animação suave
    final int diferenca = energiaAtual - energiaAnterior; // Positivo para regeneração
    final int passos = Math.max(15, Math.min(diferenca * 3, 30)); // Entre 15 e 30 frames
    final double incremento = (double) diferenca / passos;
    
    // Efeito de "brilho" verde na barra durante a regeneração
    Timer glowTimer = new Timer(150, null);
    glowTimer.addActionListener(new ActionListener() {
        private int glowCount = 0;
        @Override
        public void actionPerformed(ActionEvent e) {
            if (glowCount < 8) {
                // Alterna entre verde brilhante e a cor normal
                Color corOriginal = getCorBarraHP(barra.getValue(), energiaMaxima);
                Color corBrilho = new Color(0, 255, 0, 150); // Verde brilhante
                barra.setForeground(glowCount % 2 == 0 ? corBrilho : corOriginal);
                glowCount++;
            } else {
                glowTimer.stop();
            }
        }
    });
    glowTimer.start();
    
    animationTimer.addActionListener(new ActionListener() {
        private int contador = 0;
        private double valorAtual = energiaAnterior;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            contador++;
            valorAtual += incremento;
            
            int novoValor = Math.min(energiaAtual, (int) Math.round(valorAtual));
            
            // Atualiza a barra
            barra.setValue(novoValor);
            barra.setString("HP: " + novoValor + "/" + energiaMaxima);
            
            // Atualiza a cor dinamicamente conforme o HP aumenta
            Color novaCor = getCorBarraHP(novoValor, energiaMaxima);
            barra.setForeground(novaCor);
            
            // Efeito de pulsação durante a regeneração
            if (contador % 3 == 0) {
                barra.setBackground(new Color(240, 255, 240)); // Verde muito claro
            } else {
                barra.setBackground(new Color(200, 200, 200)); // Cinza normal
            }
            
            // Para a animação quando chegou ao valor final
            if (contador >= passos || novoValor >= energiaAtual) {
                barra.setValue(energiaAtual);
                barra.setString("HP: " + energiaAtual + "/" + energiaMaxima);
                barra.setForeground(getCorBarraHP(energiaAtual, energiaMaxima));
                barra.setBackground(new Color(200, 200, 200)); // Volta ao normal
                animationTimer.stop();
            }
        }
    });
    
    animationTimer.start();
}

private void adicionarEfeitoVisualCura(JProgressBar barra, int quantidadeCurada) {
    // Cria um label temporário mostrando a cura
    JLabel lblCura = new JLabel("+" + quantidadeCurada, JLabel.CENTER);
    lblCura.setFont(new Font("Arial", Font.BOLD, 16));
    lblCura.setForeground(new Color(0, 255, 0)); // Verde para cura
    lblCura.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    lblCura.setOpaque(true);
    lblCura.setBackground(new Color(240, 255, 240, 200)); // Fundo verde semi-transparente
    
    // Adiciona o label sobre a barra temporariamente
    if (barra.getParent() != null) {
        JPanel container = (JPanel) barra.getParent();
        container.setComponentZOrder(lblCura, 0);
        container.add(lblCura);
        
        // Anima o label subindo e brilhando
        Timer efeitoTimer = new Timer(50, null);
        efeitoTimer.addActionListener(new ActionListener() {
            private int contador = 0;
            private int posicaoY = lblCura.getY();
            
            @Override
            public void actionPerformed(ActionEvent e) {
                contador++;
                
                // Move o label para cima
                lblCura.setLocation(lblCura.getX(), posicaoY - contador * 2);
                
                // Efeito de brilho pulsante
                int alpha = (int)(255 * Math.abs(Math.sin(contador * 0.3)));
                lblCura.setForeground(new Color(0, 255, 0, Math.max(100, alpha)));
                
                // Fade out gradual
                if (contador > 20) {
                    float fadeAlpha = Math.max(0f, 1f - ((contador - 20) / 15f));
                    lblCura.setForeground(new Color(0, 255, 0, (int)(255 * fadeAlpha)));
                }
                
                if (contador >= 35) {
                    container.remove(lblCura);
                    container.repaint();
                    efeitoTimer.stop();
                }
            }
        });
        efeitoTimer.start();
    }
}

private void configurarCliqueCelulas() {
    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
            final int x = i;
            final int y = j;
            botoesGrid[i][j].addActionListener(e -> {
                celulaSelecionadaX = x;
                celulaSelecionadaY = y;
                btnDica.setEnabled(true);
            });
        }
    }
}
private void usarDica() {
    if (celulaSelecionadaX == -1 || celulaSelecionadaY == -1) {
        JOptionPane.showMessageDialog(this, "Selecione uma célula primeiro!", "Erro", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    String dica = motorJogo.obterDica(celulaSelecionadaX, celulaSelecionadaY);
    JOptionPane.showMessageDialog(this, dica, "Dica", JOptionPane.INFORMATION_MESSAGE);
    
    // Desmarca a célula selecionada após usar a dica
    celulaSelecionadaX = -1;
    celulaSelecionadaY = -1;
    btnDica.setEnabled(false);
}



}
