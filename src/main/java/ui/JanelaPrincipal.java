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
        
        // Botão de Debug
        btnDebug = new JButton("Debug: OFF");
        btnDebug.setBackground(Color.LIGHT_GRAY);
        btnDebug.addActionListener(e -> alternarModoDebug());

        JPanel botoesPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        botoesPanel.add(btnNovoJogo);
        botoesPanel.add(btnSalvar);
        botoesPanel.add(btnCarregar);
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
                        botao.setToolTipText("Pokémon do Computador: " + pokemon.getNome());
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
                
                // No modo debug, permite clicar em qualquer célula
                botao.setEnabled(!celula.foiVisitada());
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
                    
                case "ATAQUE":
                    Object[] ataqueInfo = (Object[]) dados;
                    atualizarBatalhaUI((Pokemon)ataqueInfo[0], (Pokemon)ataqueInfo[1], (Integer)ataqueInfo[2]);
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
        batalhaDialog.setSize(600, 400);
        batalhaDialog.setLocationRelativeTo(this);

        JPanel painelBatalha = new JPanel(new GridLayout(1, 2));
        
        // Painel do Pokémon Aliado
        JPanel painelAliado = criarPainelPokemon(aliado, true);
        
        // Painel do Pokémon Inimigo
        JPanel painelInimigo = criarPainelPokemon(inimigo, false);

        painelBatalha.add(painelAliado);
        painelBatalha.add(painelInimigo);

        batalhaDialog.add(painelBatalha, BorderLayout.CENTER);
        
        JButton btnAtacar = new JButton("Atacar!");
        btnAtacar.addActionListener(e -> {
            motorJogo.jogar(-1, -1); // Código especial para continuar a batalha
        });
        
        batalhaDialog.add(btnAtacar, BorderLayout.SOUTH);
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
            batalhaDialog.dispose();
        }
        areaLog.append(vencedor.getNome() + " venceu a batalha!\n");
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
}