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

    public JanelaPrincipal() {
        setTitle("Pokémon - Jogo de Tabuleiro");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        motorJogo = new MotorJogo();
        motorJogo.adicionarObservador(this);

        criarComponentes();
        motorJogo.iniciarNovoJogo();
        
        pack();
        setVisible(true);
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

        // Status
        statusJogadorLabel = new JLabel("Jogador: 0 pts | Pokémon: ");
        statusComputadorLabel = new JLabel("Computador: 0 pts | Pokémon: ");
        
        JPanel statusPanel = new JPanel(new GridLayout(2, 1));
        statusPanel.add(statusJogadorLabel);
        statusPanel.add(statusComputadorLabel);
        
        painelDireito.add(new JLabel("--- STATUS ---"));
        painelDireito.add(statusPanel);

        // Botões de controle
        JButton btnNovoJogo = new JButton("Novo Jogo");
        btnNovoJogo.addActionListener(e -> {
            motorJogo.iniciarNovoJogo();
            if (modoDebug) {
                revelarMapa();
            }
        });
        
        JButton btnSalvar = new JButton("Salvar Jogo");
        btnSalvar.addActionListener(e -> salvarJogo());
        
        JButton btnCarregar = new JButton("Carregar Jogo");
        btnCarregar.addActionListener(e -> carregarJogo());
        
        // Botão da Mochila - NOVO!
        btnMochila = new JButton("🎒 Mochila");
        btnMochila.setBackground(Color.LIGHT_GRAY);  // Mesma cor de fundo do debug
        btnMochila.setForeground(Color.BLACK);       // Texto preto como no debug
        btnMochila.setFont(new Font("Arial", Font.PLAIN, 12)); // Fonte normal (não bold)
        btnMochila.addActionListener(e -> abrirMochila());
        btnMochila.setToolTipText("Abrir mochila de Pokémons capturados");
        
        // Botão de Debug
        btnDebug = new JButton("Debug: OFF");
        btnDebug.setBackground(Color.LIGHT_GRAY);
        btnDebug.addActionListener(e -> alternarModoDebug());

        // Painel de botões com uma linha extra para a mochila
        JPanel botoesPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        botoesPanel.add(btnNovoJogo);
        botoesPanel.add(btnSalvar);
        botoesPanel.add(btnCarregar);
        botoesPanel.add(btnMochila); // Adiciona o botão da mochila
        botoesPanel.add(btnDebug);
        
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10)));
        painelDireito.add(botoesPanel);

        // Área de log
        areaLog = new JTextArea(15, 30);
        areaLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaLog);
        
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10)));
        painelDireito.add(new JLabel("--- LOG ---"));
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
            
            // Atualiza o status após fechar a janela da mochila
            SwingUtilities.invokeLater(() -> {
                // Força uma atualização do status
                String statusJogador = "Jogador: " + jogador.getPontuacao() + " pts | Pokémon: " + 
                    (jogador.getPokemonAtual() != null ? jogador.getPokemonAtual().getNome() : "Nenhum") +
                    " | Time: " + jogador.getTime().size() + "/6 | Mochila: " + 
                    jogador.getMochila().getQuantidade() + "/" + jogador.getMochila().getCapacidadeMaxima();
                
                statusJogadorLabel.setText(statusJogador);
            });
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
                botoesGrid[i][j].setBackground(cor);
                botoesGrid[i][j].setIcon(null);
                botoesGrid[i][j].setText("");
                botoesGrid[i][j].setBorder(UIManager.getBorder("Button.border")); // Border padrão
                botoesGrid[i][j].setToolTipText(null); // Remove tooltip
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
                        // Muda a cor se estiver quase cheia
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
                
            case "POKEMON_ENCONTRADO":
                int[] coords = (int[]) dados;
                // Em vez de apenas colocar "?", vamos carregar a imagem do Pokémon
                Celula[][] tabuleiro = motorJogo.getTabuleiro();
                Pokemon pokemon = tabuleiro[coords[0]][coords[1]].getPokemon();
                if (pokemon != null) {
                    carregarImagemPokemon(coords[0], coords[1], pokemon);
                }
                break;
                
            case "CELULA_VAZIA":
                int[] coordsVazia = (int[]) dados;
                JButton botaoVazio = botoesGrid[coordsVazia[0]][coordsVazia[1]];
                botaoVazio.setIcon(null);
                botaoVazio.setText("");
                botaoVazio.setEnabled(true);
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
                
            // NOVOS EVENTOS PARA BATALHA POR TURNOS
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
                JOptionPane.showMessageDialog(this, "Você capturou " + capturado.getNome() + "!");
                break;
        }
    });
}

    private void atualizarTabuleiro() {
        Celula[][] tabuleiro = motorJogo.getTabuleiro();
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                JButton botao = botoesGrid[i][j];
                
                // Reseta o botão
                botao.setIcon(null);
                botao.setText("");
                botao.setEnabled(true);
                botao.setBorder(UIManager.getBorder("Button.border")); // Border padrão
                botao.setToolTipText(null); // Remove tooltip
                
                // Aplica a cor da região
                aplicarCorRegiao(botao, i, j);
                
                // Se a célula foi visitada, mostra o conteúdo
                if (tabuleiro[i][j].foiVisitada()) {
                    botao.setEnabled(false);
                    
                    if (!tabuleiro[i][j].estaVazia() && tabuleiro[i][j].getPokemon() != null) {
                        carregarImagemPokemon(i, j, tabuleiro[i][j].getPokemon());
                    } else {
                        botao.setText("Vazia");
                    }
                } 
            }
        }
    }

    private void carregarImagemPokemon(int x, int y, Pokemon pokemon) {
        Celula celula = motorJogo.getTabuleiro()[x][y];
        
        // Só mostra a imagem se a célula foi visitada
        if (celula.foiVisitada()) {
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
                }
            } catch (IOException e) {
                botao.setIcon(null);
                botao.setText(pokemon.getNome());
            }
        }
    }

private void mostrarTelaBatalha(Pokemon aliado, Pokemon inimigo) {
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
    JLabel lblStatusBatalha = new JLabel("É seu turno! Clique em 'Atacar' para começar.", JLabel.CENTER);
    lblStatusBatalha.setFont(new Font("Arial", Font.BOLD, 12));
    batalhaDialog.add(lblStatusBatalha, BorderLayout.NORTH);
    
    batalhaDialog.setVisible(true);
}

    private JPanel criarPainelPokemon(Pokemon pokemon, boolean aliado) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createTitledBorder(pokemon.getNome()));
        
        try {
            String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
            InputStream is = getClass().getResourceAsStream(nomeIcone);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                painel.add(new JLabel(new ImageIcon(scaledImg)), BorderLayout.CENTER);
            } else {
                painel.add(new JLabel(pokemon.getNome()), BorderLayout.CENTER);
            }
        } catch (IOException e) {
            painel.add(new JLabel(pokemon.getNome()), BorderLayout.CENTER);
        }
        
        // Barra de HP (usando 100 como máximo ou implemente getEnergiaMaxima())
        JProgressBar barraHP = new JProgressBar(0, 100);
        barraHP.setValue(pokemon.getEnergia());
        barraHP.setString(pokemon.getEnergia() + "/100");
        barraHP.setStringPainted(true);
        barraHP.setForeground(pokemon.getEnergia() < 25 ? Color.RED : Color.GREEN);
        
        painel.add(barraHP, BorderLayout.SOUTH);
        
        return painel;
    }

    private void atualizarBatalhaUI(Pokemon atacante, Pokemon defensor, int novaEnergia) {
        Component[] components = batalhaDialog.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel painel = (JPanel) comp;
                Border border = painel.getBorder();
                if (border instanceof javax.swing.border.TitledBorder) {
                    String title = ((javax.swing.border.TitledBorder) border).getTitle();
                    if (title.equals(defensor.getNome())) {
                        // Atualiza a barra de HP do defensor
                        for (Component c : painel.getComponents()) {
                            if (c instanceof JProgressBar) {
                                ((JProgressBar) c).setValue(novaEnergia);
                                ((JProgressBar) c).setString(novaEnergia + "/" + defensor.getEnergiaMaxima());
                                ((JProgressBar) c).setForeground(novaEnergia < defensor.getEnergiaMaxima() / 4 ? Color.RED : Color.GREEN);
                            }
                        }
                    }
                }
            }
        }
        
        areaLog.append(atacante.getNome() + " atacou " + defensor.getNome() + "!\n");
    }

    private void encerrarBatalha(Pokemon vencedor) {
    if (batalhaDialog != null) {
        // Mostra resultado da batalha antes de fechar
        String mensagem;
        if (vencedor == motorJogo.getPokemonJogadorBatalha()) {
            mensagem = "🎉 VITÓRIA! " + vencedor.getNome() + " venceu a batalha!";
        } else {
            mensagem = "💀 DERROTA! " + vencedor.getNome() + " venceu a batalha!";
        }
        
        JOptionPane.showMessageDialog(batalhaDialog, mensagem, "Fim da Batalha", JOptionPane.INFORMATION_MESSAGE);
        batalhaDialog.dispose();
        batalhaDialog = null;
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
    JPanel painelStats = new JPanel(new GridLayout(4, 1, 2, 2));
    
    // Barra de HP
    JProgressBar barraHP = new JProgressBar(0, pokemon.getEnergiaMaxima());
    barraHP.setValue(pokemon.getEnergia());
    barraHP.setString("HP: " + pokemon.getEnergia() + "/" + pokemon.getEnergiaMaxima());
    barraHP.setStringPainted(true);
    barraHP.setForeground(pokemon.getEnergia() < pokemon.getEnergiaMaxima() / 4 ? Color.RED : 
                         pokemon.getEnergia() < pokemon.getEnergiaMaxima() / 2 ? Color.ORANGE : Color.GREEN);
    
    // Labels de estatísticas
    JLabel lblNivel = new JLabel("Nível: " + pokemon.getNivel(), JLabel.CENTER);
    JLabel lblForca = new JLabel("Força: " + pokemon.getForca(), JLabel.CENTER);
    JLabel lblExp = new JLabel("Exp: " + pokemon.getNivel(), JLabel.CENTER);
    
    painelStats.add(barraHP);
    painelStats.add(lblNivel);
    painelStats.add(lblForca);
    painelStats.add(lblExp);
    
    painel.add(painelStats, BorderLayout.SOUTH);
    
    return painel;
}
    private void atualizarBatalhaUITurnos(String nomeAtacante, String nomeDefensor, int dano, int energiaAnterior, int energiaAtual) {
    if (batalhaDialog == null) return;
    
    // Procura o painel do defensor para atualizar a barra de HP
    Component[] components = ((JPanel)batalhaDialog.getContentPane().getComponent(1)).getComponents();
    for (Component comp : components) {
        if (comp instanceof JPanel) {
            JPanel painel = (JPanel) comp;
            Border border = painel.getBorder();
            if (border instanceof javax.swing.border.TitledBorder) {
                String title = ((javax.swing.border.TitledBorder) border).getTitle();
                if (title.contains(nomeDefensor)) {
                    // Encontrou o painel do defensor - atualiza a barra de HP
                    Component painelStats = painel.getComponent(1); // BorderLayout.SOUTH
                    if (painelStats instanceof JPanel) {
                        Component barraHP = ((JPanel) painelStats).getComponent(0);
                        if (barraHP instanceof JProgressBar) {
                            JProgressBar barra = (JProgressBar) barraHP;
                            barra.setValue(energiaAtual);
                            barra.setString("HP: " + energiaAtual + "/" + barra.getMaximum());
                            barra.setForeground(energiaAtual < barra.getMaximum() / 4 ? Color.RED : 
                                              energiaAtual < barra.getMaximum() / 2 ? Color.ORANGE : Color.GREEN);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    // Atualiza o log
    areaLog.append(nomeAtacante + " atacou " + nomeDefensor + " causando " + dano + " de dano! " +
                  "(" + energiaAnterior + " → " + energiaAtual + " HP)\n");
    areaLog.setCaretPosition(areaLog.getDocument().getLength());
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
                    ((JButton) btnAtacar).setText("Aguarde...");
                    ((JButton) btnAtacar).setBackground(Color.GRAY);
                }
            }
        }
        
        // Atualiza o label de status
        Component lblStatus = batalhaDialog.getContentPane().getComponent(0); // BorderLayout.NORTH
        if (lblStatus instanceof JLabel) {
            if (habilitar) {
                ((JLabel) lblStatus).setText("É seu turno! Clique em 'Atacar'.");
            } else {
                ((JLabel) lblStatus).setText("Aguardando turno do computador...");
            }
        }
    }
}


}
