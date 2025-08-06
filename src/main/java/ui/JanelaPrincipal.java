package ui;

import jogo.MotorJogo;
import modelo.Celula;
import modelo.Pokemon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter; // NOVO IMPORT
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; // NOVO IMPORT

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
                botao.setBackground(Color.LIGHT_GRAY);
                botao.setActionCommand(i + "," + j);
                botao.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String[] coords = e.getActionCommand().split(",");
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
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

        areaLog = new JTextArea(20, 30);
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);

        painelDireito.add(new JLabel("--- STATUS ---"));
        painelDireito.add(statusJogadorLabel);
        painelDireito.add(statusComputadorLabel);

        // NOVO: Botões de Salvar e Carregar
        JButton btnSalvar = new JButton("Salvar Jogo");
        btnSalvar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSalvar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Salvar Jogo");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.pokemonsave)", "pokemonsave"));
            int userSelection = fileChooser.showSaveDialog(JanelaPrincipal.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pokemonsave")) {
                    filePath += ".pokemonsave";
                }
                motorJogo.salvarJogo(filePath);
            }
        });
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        painelDireito.add(btnSalvar);

        JButton btnCarregar = new JButton("Carregar Jogo");
        btnCarregar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCarregar.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Carregar Jogo");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Jogo (*.pokemonsave)", "pokemonsave"));
            int userSelection = fileChooser.showOpenDialog(JanelaPrincipal.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                motorJogo.carregarJogo(filePath);
            }
        });
        painelDireito.add(Box.createRigidArea(new Dimension(0, 5))); // Espaçamento
        painelDireito.add(btnCarregar);

        painelDireito.add(Box.createRigidArea(new Dimension(0, 10))); // Espaçamento
        painelDireito.add(new JScrollPane(areaLog));

        add(painelDireito, BorderLayout.EAST);

        pintarRegioes();
    }

    private void pintarRegioes() {
        int meio = MotorJogo.TAMANHO_GRID / 2;
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                if (i < meio && j < meio) {
                    botoesGrid[i][j].setBackground(new Color(173, 216, 230)); // Azul claro (Água)
                } else if (i < meio && j >= meio) {
                    botoesGrid[i][j].setBackground(new Color(144, 238, 144)); // Verde claro (Floresta)
                } else if (i >= meio && j < meio) {
                    botoesGrid[i][j].setBackground(new Color(210, 180, 140)); // Marrom claro (Terra)
                } else {
                    botoesGrid[i][j].setBackground(new Color(255, 255, 204)); // Amarelo claro (Elétrico)
                }
            }
        }
    }

    @Override
    public void atualizar(String evento, Object dados) {
        SwingUtilities.invokeLater(() -> {
            switch (evento) {
                case "STATUS_JOGADOR":
                    statusJogadorLabel.setText((String) dados);
                    break;
                case "STATUS_COMPUTADOR":
                    statusComputadorLabel.setText((String) dados);
                    break;
                case "MENSAGEM":
                    areaLog.append((String) dados + "\n");
                    areaLog.setCaretPosition(areaLog.getDocument().getLength()); // Auto-scroll
                    break;
                case "POKEMON_ENCONTRADO":
                    int[] coords = (int[]) dados;
                    int x = coords[0];
                    int y = coords[1];
                    Pokemon pokemonEncontrado = motorJogo.getTabuleiro()[x][y].getPokemon();
                    JButton botao = botoesGrid[x][y];

                    String nomeIcone = "/resources/" + pokemonEncontrado.getNome().toLowerCase() + ".png";
                    try {
                        ImageIcon icon = new ImageIcon(getClass().getResource(nomeIcone));
                        Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        botao.setIcon(new ImageIcon(img));
                        botao.setEnabled(false); // Desabilita o botão após encontrar o Pokémon
                    } catch (Exception e) {
                        // Fallback para texto se a imagem não for encontrada
                        botao.setText(pokemonEncontrado.getNome().substring(0, 3));
                        System.err.println("Imagem não encontrada para " + pokemonEncontrado.getNome() + ": " + nomeIcone);
                        botao.setEnabled(false);
                    }
                    break;
                case "POKEMON_ESCAPOU_MOVER":
                    int[] oldCoords = (int[]) dados;
                    botoesGrid[oldCoords[0]][oldCoords[1]].setIcon(null); // Limpa o ícone antigo
                    botoesGrid[oldCoords[0]][oldCoords[1]].setText(""); // Limpa o texto antigo
                    botoesGrid[oldCoords[0]][oldCoords[1]].setEnabled(true); // Reabilita o botão
                    break;
                case "FIM_DE_JOGO":
                    String vencedor = (String) dados;
                    areaLog.append("\n--- FIM DE JOGO ---\n" + vencedor + "\n");
                    JOptionPane.showMessageDialog(JanelaPrincipal.this, vencedor, "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                    // Desabilitar todos os botões do grid
                    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                            botoesGrid[i][j].setEnabled(false);
                        }
                    }
                    break;
                case "JOGO_CARREGADO": // NOVO CASE
                    Celula[][] tabuleiroCarregado = (Celula[][]) dados;
                    pintarRegioes(); // Pinta as regiões novamente
                    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                            JButton botaoCarregado = botoesGrid[i][j];
                            botaoCarregado.setIcon(null); // Limpa qualquer ícone anterior
                            botaoCarregado.setText(""); // Limpa qualquer texto anterior
                            botaoCarregado.setEnabled(true); // Reabilita o botão

                            Celula celulaCarregada = tabuleiroCarregado[i][j];
                            if (!celulaCarregada.estaVazia()) {
                                // Se a célula tiver um Pokémon, tenta carregar o ícone
                                nomeIcone = "/resources/" + celulaCarregada.getPokemon().getNome().toLowerCase() + ".png";
                                try {
                                    ImageIcon icon = new ImageIcon(getClass().getResource(nomeIcone));
                                    Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                                    botaoCarregado.setIcon(new ImageIcon(img));
                                    botaoCarregado.setEnabled(false); // Desabilita o botão se já tiver um Pokémon revelado
                                } catch (Exception e) {
                                    botaoCarregado.setText(celulaCarregada.getPokemon().getNome().substring(0, 3));
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
