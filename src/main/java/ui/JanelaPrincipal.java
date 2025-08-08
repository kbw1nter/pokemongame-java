package ui;

import jogo.MotorJogo;
import modelo.Celula;
import modelo.Pokemon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class JanelaPrincipal extends JFrame implements Observador {
    private MotorJogo motorJogo;
    private JButton[][] botoesGrid;
    private JTextArea areaLog;
    private JLabel statusJogadorLabel;
    private JLabel statusComputadorLabel;

    public JanelaPrincipal() {
        setTitle("Pokémon - Jogo de Tabuleiro");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        motorJogo = new MotorJogo();
        motorJogo.adicionarObservador(this);

        criarComponentes();
        motorJogo.iniciarNovoJogo();
    }

    private void criarComponentes() {
        setLayout(new BorderLayout());

        // Painel do Tabuleiro (Esquerda)
        JPanel painelTabuleiro = new JPanel(new GridLayout(MotorJogo.TAMANHO_GRID, MotorJogo.TAMANHO_GRID));
        botoesGrid = new JButton[MotorJogo.TAMANHO_GRID][MotorJogo.TAMANHO_GRID];

        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                JButton botao = new JButton();
                botao.setPreferredSize(new Dimension(60, 60));
                botao.setFont(new Font("Arial", Font.BOLD, 10));
                botao.setFocusPainted(false);
                final int x = i;
                final int y = j;
                botao.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        motorJogo.jogar(x, y);
                    }
                });
                botoesGrid[i][j] = botao;
                painelTabuleiro.add(botao);
            }
        }
        add(painelTabuleiro, BorderLayout.CENTER);

        // Painel Direito (Status e Log)
        JPanel painelDireito = new JPanel();
        painelDireito.setLayout(new BoxLayout(painelDireito, BoxLayout.Y_AXIS));
        painelDireito.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusJogadorLabel = new JLabel("Jogador: 0 pts | Pokémon: ");
        statusComputadorLabel = new JLabel("Computador: 0 pts | Pokémon: ");

        painelDireito.add(new JLabel("--- STATUS ---"));
        painelDireito.add(statusJogadorLabel);
        painelDireito.add(statusComputadorLabel);

        //Botões de Salvar e Carregar
        JButton btnSalvar = new JButton("Salvar Jogo");
        btnSalvar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Jogo");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.sav)", "sav"));
            int userSelection = fileChooser.showSaveDialog(JanelaPrincipal.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".sav")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".sav");
                }
                motorJogo.salvarJogo(fileToSave.getAbsolutePath());
            }
        });
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        painelDireito.add(btnSalvar);

        JButton btnCarregar = new JButton("Carregar Jogo");
        btnCarregar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Carregar Jogo");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.sav)", "sav"));
            int userSelection = fileChooser.showOpenDialog(JanelaPrincipal.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                motorJogo.carregarJogo(fileToLoad.getAbsolutePath());
            }
        });
        painelDireito.add(Box.createRigidArea(new Dimension(0, 5))); // Espaçamento
        painelDireito.add(btnCarregar);

        areaLog = new JTextArea(15, 30);
        areaLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaLog);
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        painelDireito.add(new JLabel("--- LOG ---"));
        painelDireito.add(scrollPane);

        add(painelDireito, BorderLayout.EAST);

        // Configurar cores das regiões
        aplicarCoresRegioes();
    }

    private void aplicarCoresRegioes() {
        int meio = MotorJogo.TAMANHO_GRID / 2;
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                Color cor;
                if (i < meio && j < meio) {
                    cor = new Color(173, 216, 230); // Azul Claro (Água)
                } else if (i < meio && j >= meio) {
                    cor = new Color(144, 238, 144); // Verde Claro (Floresta)
                } else if (i >= meio && j < meio) {
                    cor = new Color(210, 180, 140); // Marrom Claro (Terra)
                } else {
                    cor = new Color(255, 255, 204); // Amarelo Claro (Elétrico)
                }
                botoesGrid[i][j].setBackground(cor);
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
                    areaLog.setCaretPosition(areaLog.getDocument().getLength()); // Auto-scroll
                    break;
                case "POKEMON_ENCONTRADO":
                    int[] coords = (int[]) dados;
                    int x = coords[0];
                    int y = coords[1];
                    JButton botao = botoesGrid[x][y];

                    Pokemon pokemonEncontrado = motorJogo.getTabuleiro()[x][y].getPokemon();

                    if (pokemonEncontrado != null) {
                        String nomeIcone = "/resources/" + pokemonEncontrado.getNome().toLowerCase() + ".png";
                        try {
                            ImageIcon icon = new ImageIcon(getClass().getResource(nomeIcone));
                            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                            botao.setIcon(new ImageIcon(img));
                            botao.setEnabled(false);
                        } catch (Exception e) {
                            botao.setText(pokemonEncontrado.getNome().substring(0, Math.min(pokemonEncontrado.getNome().length(), 3)));
                            System.err.println("Imagem não encontrada para " + pokemonEncontrado.getNome() + ": " + nomeIcone);
                            botao.setEnabled(false);
                        }
                    } else {
                        botao.setText(""); // Limpa o texto do botão
                        botao.setIcon(null); // Remove qualquer ícone
                        botao.setEnabled(false); // Desabilita o botão
                    }
                    break;
                case "JOGO_CARREGADO":
                    Celula[][] tabuleiroCarregado = (Celula[][]) dados;
                    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                            JButton botaoCarregado = botoesGrid[i][j];
                            botaoCarregado.setIcon(null); // Limpa ícones antigos
                            botaoCarregado.setText(""); // Limpa textos antigos
                            botaoCarregado.setEnabled(true); // Reabilita o botão

                            Celula celulaCarregada = tabuleiroCarregado[i][j];
                            if (!celulaCarregada.estaVazia()) {
                                // Se a célula tiver um Pokémon, tenta carregar o ícone
                                String nomeIcone = "/resources/" + celulaCarregada.getPokemon().getNome().toLowerCase() + ".png";
                                try {
                                    ImageIcon icon = new ImageIcon(getClass().getResource(nomeIcone));
                                    Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                                    botaoCarregado.setIcon(new ImageIcon(img));
                                    botaoCarregado.setEnabled(false); // Desabilita o botão se já tiver um Pokémon revelado
                                } catch (Exception e) {
                                    botaoCarregado.setText(celulaCarregada.getPokemon().getNome().substring(0, Math.min(celulaCarregada.getPokemon().getNome().length(), 3)));
                                    System.err.println("Imagem não encontrada para " + celulaCarregada.getPokemon().getNome() + ": " + nomeIcone);
                                    botaoCarregado.setEnabled(false); // Desabilita o botão se já tiver um Pokémon revelado
                                }
                            }
                        }
                    }
                    areaLog.append("Tabuleiro carregado e atualizado.\n");
                    break;
            }
        });
    }
}
