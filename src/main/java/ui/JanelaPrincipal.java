package ui;

import jogo.MotorJogo;
import modelo.Treinador;
import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame implements Observador {
    private static final int TAMANHO_GRID = 8;
    private final MotorJogo motorJogo;
    private JButton[][] botoesGrid;
    private JTextArea areaLog;
    private JLabel statusJogadorLabel;
    private JLabel statusComputadorLabel;

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
        // painel do Grid
        JPanel painelGrid = new JPanel(new GridLayout(TAMANHO_GRID, TAMANHO_GRID));
        botoesGrid = new JButton[TAMANHO_GRID][TAMANHO_GRID];
        for (int i = 0; i < TAMANHO_GRID; i++) {
            for (int j = 0; j < TAMANHO_GRID; j++) {
                final int x = i;
                final int y = j;
                botoesGrid[i][j] = new JButton();
                botoesGrid[i][j].addActionListener(e -> motorJogo.realizarJogadaJogador(x, y));
                painelGrid.add(botoesGrid[i][j]);
            }
        }
        add(painelGrid, BorderLayout.CENTER);

        // painel de Status e Log
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

    @Override
    public void atualizar(String evento, Object dados) {
        SwingUtilities.invokeLater(() -> {
            switch (evento) {
                case "JOGO_INICIADO":
                    areaLog.setText("Novo jogo iniciado!\n");
                    break;
                case "ATUALIZAR_STATUS":
                    Treinador[] treinadores = (Treinador[]) dados;
                    Treinador jogador = treinadores[0];
                    Treinador computador = treinadores[1];
                    statusJogadorLabel.setText(String.format("Jogador: %d pts | Pokémon: %s (HP: %d)",
                            jogador.getPontuacao(), jogador.getPokemonPrincipal().getNome(), jogador.getPokemonPrincipal().getEnergia()));
                    statusComputadorLabel.setText(String.format("Computador: %d pts | Pokémon: %s (HP: %d)",
                            computador.getPontuacao(), computador.getPokemonPrincipal().getNome(), computador.getPokemonPrincipal().getEnergia()));
                    break;
                case "MENSAGEM":
                    areaLog.append(dados.toString() + "\n");
                    break;
                case "FIM_DE_JOGO":
                    areaLog.append("\n--- JOGO TERMINOU ---\n");
                    areaLog.append(dados.toString() + "\n");
                    JOptionPane.showMessageDialog(this, dados.toString(), "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                    // Desabilitar botões
                    for(int i=0; i<TAMANHO_GRID; i++) for(int j=0; j<TAMANHO_GRID; j++) botoesGrid[i][j].setEnabled(false);
                    break;
            }
        });
    }
}
