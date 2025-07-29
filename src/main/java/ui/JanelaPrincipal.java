package ui;

import jogo.MotorJogo;
import modelo.Celula;
import modelo.Treinador;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame implements Observador {
    private final MotorJogo motorJogo;
    private JButton[][] botoesGrid;
    private JTextArea areaLog;
    private JLabel statusJogadorLabel;
    private JLabel statusComputadorLabel;

    private final Color COR_AGUA = new Color(173, 216, 230);
    private final Color COR_FLORESTA = new Color(144, 238, 144);
    private final Color COR_TERRA = new Color(210, 180, 140);
    private final Color COR_ELETRICO = new Color(236, 159, 239);

    public JanelaPrincipal() {
        this.motorJogo = new MotorJogo();
        this.motorJogo.adicionarObservador(this);

        setTitle("Pokémon - Jogo de Tabuleiro");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        criarComponentes();

        motorJogo.iniciarNovoJogo();
    }

    private void criarComponentes() {
        JPanel painelGrid = new JPanel(new GridLayout(MotorJogo.TAMANHO_GRID, MotorJogo.TAMANHO_GRID));
        botoesGrid = new JButton[MotorJogo.TAMANHO_GRID][MotorJogo.TAMANHO_GRID];
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                final int x = i;
                final int y = j;
                botoesGrid[i][j] = new JButton();
                botoesGrid[i][j].addActionListener(e -> motorJogo.realizarJogadaJogador(x, y));
                painelGrid.add(botoesGrid[i][j]);
            }
        }
        add(painelGrid, BorderLayout.CENTER);

        JPanel painelDireito = new JPanel();
        painelDireito.setLayout(new BoxLayout(painelDireito, BoxLayout.Y_AXIS));

        statusJogadorLabel = new JLabel("Jogador: ...");
        statusComputadorLabel = new JLabel("Computador: ...");
        areaLog = new JTextArea(10, 25);
        areaLog.setEditable(false);

        painelDireito.add(new JLabel("--- STATUS ---"));
        painelDireito.add(statusJogadorLabel);
        painelDireito.add(statusComputadorLabel);
        painelDireito.add(new JScrollPane(areaLog));

        add(painelDireito, BorderLayout.EAST);
    }

    private void pintarRegioes() {
        int meio = MotorJogo.TAMANHO_GRID / 2;
        for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
            for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                botoesGrid[i][j].setIcon(null);
                botoesGrid[i][j].setEnabled(true);

                if (i < meio && j < meio) botoesGrid[i][j].setBackground(COR_AGUA);
                else if (i < meio && j >= meio) botoesGrid[i][j].setBackground(COR_FLORESTA);
                else if (i >= meio && j < meio) botoesGrid[i][j].setBackground(COR_TERRA);
                else botoesGrid[i][j].setBackground(COR_ELETRICO);
            }
        }
    }

    @Override
    public void atualizar(String evento, Object dados) {
        SwingUtilities.invokeLater(() -> {
            switch (evento) {
                case "JOGO_INICIADO":
                    areaLog.setText("Novo jogo iniciado!\n");
                    pintarRegioes();
                    break;

                case "CELULA_REVELADA":
                    Celula celula = (Celula) dados;
                    JButton botao = botoesGrid[celula.getX()][celula.getY()];
                    botao.setEnabled(false);

                    if (!celula.estaVazia()) {
                        String nomeIcone = "/resources/" + celula.getPokemon().getNome().toLowerCase() + ".png";
                        try {
                            ImageIcon icon = new ImageIcon(getClass().getResource(nomeIcone));
                            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                            botao.setIcon(new ImageIcon(img));
                        } catch (Exception e) {
                            botao.setText(celula.getPokemon().getNome().substring(0, 3));
                            System.err.println("Imagem não encontrada: " + nomeIcone);
                        }
                    }
                    break;

                case "ATUALIZAR_STATUS":
                    Treinador[] treinadores = (Treinador[]) dados;
                    Treinador jogador = treinadores[0];
                    Treinador computador = treinadores[1];

                    String statusJogador = "Jogador: " + jogador.getPontuacao() + " pts";
                    if (jogador.getPokemonPrincipal() != null) {
                        statusJogador += String.format(" | Pokémon: %s (HP: %d)",
                                jogador.getPokemonPrincipal().getNome(), jogador.getPokemonPrincipal().getEnergia());
                    }
                    statusJogadorLabel.setText(statusJogador);

                    String statusComputador = "Computador: " + computador.getPontuacao() + " pts";
                    if (computador.getPokemonPrincipal() != null) {
                        statusComputador += String.format(" | Pokémon: %s (HP: %d)",
                                computador.getPokemonPrincipal().getNome(), computador.getPokemonPrincipal().getEnergia());
                    }
                    statusComputadorLabel.setText(statusComputador);
                    break;

                case "MENSAGEM":
                    areaLog.append(dados.toString() + "\n");
                    areaLog.setCaretPosition(areaLog.getDocument().getLength());
                    break;

                case "FIM_DE_JOGO":
                    areaLog.append("\n--- JOGO TERMINOU ---\n");
                    areaLog.append(dados.toString() + "\n");
                    JOptionPane.showMessageDialog(this, dados.toString(), "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                            botoesGrid[i][j].setEnabled(false);
                        }
                    }
                    break;
            }
        });
    }
}
