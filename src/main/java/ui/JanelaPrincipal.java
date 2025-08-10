package ui;

import jogo.MotorJogo;
import modelo.Celula;
import modelo.Pokemon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

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
                // Aumentando o tamanho preferencial do botão para acomodar imagens maiores
                botao.setPreferredSize(new Dimension(100, 100));
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

        // Botões de Salvar e Carregar
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
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10)));
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
        painelDireito.add(Box.createRigidArea(new Dimension(0, 5)));
        painelDireito.add(btnCarregar);

        areaLog = new JTextArea(15, 30);
        areaLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaLog);
        painelDireito.add(Box.createRigidArea(new Dimension(0, 10)));
        painelDireito.add(new JLabel("--- LOG ---"));
        painelDireito.add(scrollPane);

        add(painelDireito, BorderLayout.EAST);

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
                    areaLog.setCaretPosition(areaLog.getDocument().getLength());
                    break;
                case "POKEMON_ENCONTRADO":
                    int[] coords = (int[]) dados;
                    int x = coords[0];
                    int y = coords[1];
                    JButton botao = botoesGrid[x][y];

                    Pokemon pokemonEncontrado = motorJogo.getTabuleiro()[x][y].getPokemon();

                    if (pokemonEncontrado != null) {
                        String nomeIcone = pokemonEncontrado.getNome().toLowerCase() + ".png";
                        System.out.println("POKEMON_ENCONTRADO: Tentando carregar imagem: " + nomeIcone);
                        try (InputStream is = ClassLoader.getSystemResourceAsStream(nomeIcone)) {
                            if (is != null) {
                                BufferedImage originalImage = ImageIO.read(is);
                                BufferedImage imageWithoutAlpha = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                Graphics2D g = imageWithoutAlpha.createGraphics();
                                g.drawImage(originalImage, 0, 0, null);
                                g.dispose();
                                // tamanho da imagem
                                Image img = imageWithoutAlpha.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                                botao.setIcon(new ImageIcon(img));
                                botao.setEnabled(false);
                                System.out.println("POKEMON_ENCONTRADO: Imagem carregada com sucesso: " + nomeIcone);
                            } else {
                                System.err.println("POKEMON_ENCONTRADO: Imagem não encontrada para " + pokemonEncontrado.getNome() + ": " + nomeIcone + " (InputStream nulo)");
                                // Fallback para caminho absoluto (com barra inicial) se o relativo falhar
                                nomeIcone = "/" + nomeIcone;
                                System.out.println("POKEMON_ENCONTRADO: Tentando carregar imagem com caminho absoluto: " + nomeIcone);
                                try (InputStream isFallback = getClass().getResourceAsStream(nomeIcone)) {
                                    if (isFallback != null) {
                                        BufferedImage originalImage = ImageIO.read(isFallback);
                                        BufferedImage imageWithoutAlpha = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                        Graphics2D g = imageWithoutAlpha.createGraphics();
                                        g.drawImage(originalImage, 0, 0, null);
                                        g.dispose();

                                        Image img = imageWithoutAlpha.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                                        botao.setIcon(new ImageIcon(img));
                                        botao.setEnabled(false);
                                        System.out.println("POKEMON_ENCONTRADO: Imagem carregada com sucesso com caminho absoluto: " + nomeIcone);
                                    } else {
                                        botao.setText(pokemonEncontrado.getNome().substring(0, Math.min(pokemonEncontrado.getNome().length(), 3)));
                                        System.err.println("POKEMON_ENCONTRADO: Imagem não encontrada para " + pokemonEncontrado.getNome() + ": " + nomeIcone + " (InputStream nulo no fallback)");
                                        botao.setEnabled(false);
                                    }
                                } catch (IOException e) {
                                    botao.setText(pokemonEncontrado.getNome().substring(0, Math.min(pokemonEncontrado.getNome().length(), 3)));
                                    System.err.println("POKEMON_ENCONTRADO: Erro ao carregar imagem para " + pokemonEncontrado.getNome() + ": " + nomeIcone + ". Erro: " + e.getMessage());
                                    botao.setEnabled(false);
                                }
                            }
                        } catch (IOException e) {
                            botao.setText(pokemonEncontrado.getNome().substring(0, Math.min(pokemonEncontrado.getNome().length(), 3)));
                            System.err.println("POKEMON_ENCONTRADO: Erro ao carregar imagem para " + pokemonEncontrado.getNome() + ": " + nomeIcone + ". Erro: " + e.getMessage());
                            botao.setEnabled(false);
                        }
                    } else {
                        botao.setText("");
                        botao.setIcon(null);
                        botao.setEnabled(false);
                    }
                    break;
                case "JOGO_CARREGADO":
                    Celula[][] tabuleiroCarregado = (Celula[][]) dados;
                    for (int i = 0; i < MotorJogo.TAMANHO_GRID; i++) {
                        for (int j = 0; j < MotorJogo.TAMANHO_GRID; j++) {
                            JButton botaoCarregado = botoesGrid[i][j];
                            botaoCarregado.setIcon(null);
                            botaoCarregado.setText("");
                            botaoCarregado.setEnabled(true);

                            Celula celulaCarregada = tabuleiroCarregado[i][j];
                            if (!celulaCarregada.estaVazia()) {
                                Pokemon pokemonCarregado = celulaCarregada.getPokemon();
                                if (pokemonCarregado != null) {
                                    String nomeIcone = pokemonCarregado.getNome().toLowerCase() + ".png";
                                    System.out.println("JOGO_CARREGADO: Tentando carregar imagem: " + nomeIcone);
                                    try (InputStream is = ClassLoader.getSystemResourceAsStream(nomeIcone)) {
                                        if (is != null) {
                                            BufferedImage originalImage = ImageIO.read(is);
                                            BufferedImage imageWithoutAlpha = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                            Graphics2D g = imageWithoutAlpha.createGraphics();
                                            g.drawImage(originalImage, 0, 0, null);
                                            g.dispose();

                                            Image img = imageWithoutAlpha.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                                            botaoCarregado.setIcon(new ImageIcon(img));
                                            botaoCarregado.setEnabled(false);
                                            System.out.println("JOGO_CARREGADO: Imagem carregada com sucesso: " + nomeIcone);
                                        } else {
                                            System.err.println("JOGO_CARREGADO: Imagem não encontrada para " + pokemonCarregado.getNome() + ": " + nomeIcone + " (InputStream nulo)");
                                            nomeIcone = "/" + nomeIcone;
                                            System.out.println("JOGO_CARREGADO: Tentando carregar imagem com caminho absoluto: " + nomeIcone);
                                            try (InputStream isFallback = getClass().getResourceAsStream(nomeIcone)) {
                                                if (isFallback != null) {
                                                    BufferedImage originalImage = ImageIO.read(isFallback);
                                                    BufferedImage imageWithoutAlpha = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                                                    Graphics2D g = imageWithoutAlpha.createGraphics();
                                                    g.drawImage(originalImage, 0, 0, null);
                                                    g.dispose();

                                                    Image img = imageWithoutAlpha.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                                                    botaoCarregado.setIcon(new ImageIcon(img));
                                                    botaoCarregado.setEnabled(false);
                                                    System.out.println("JOGO_CARREGADO: Imagem carregada com sucesso com caminho absoluto: " + nomeIcone);
                                                } else {
                                                    botaoCarregado.setText(pokemonCarregado.getNome().substring(0, Math.min(pokemonCarregado.getNome().length(), 3)));
                                                    System.err.println("JOGO_CARREGADO: Imagem não encontrada para " + pokemonCarregado.getNome() + ": " + nomeIcone + " (InputStream nulo no fallback)");
                                                    botaoCarregado.setEnabled(false);
                                                }
                                            } catch (IOException e) {
                                                botaoCarregado.setText(pokemonCarregado.getNome().substring(0, Math.min(pokemonCarregado.getNome().length(), 3)));
                                                System.err.println("JOGO_CARREGADO: Erro ao carregar imagem para " + pokemonCarregado.getNome() + ": " + nomeIcone + ". Erro: " + e.getMessage());
                                                botaoCarregado.setEnabled(false);
                                            }
                                        }
                                    } catch (IOException e) {
                                        botaoCarregado.setText(pokemonCarregado.getNome().substring(0, Math.min(pokemonCarregado.getNome().length(), 3)));
                                        System.err.println("JOGO_CARREGADO: Erro ao carregar imagem para " + pokemonCarregado.getNome() + ": " + nomeIcone + ". Erro: " + e.getMessage());
                                        botaoCarregado.setEnabled(false);
                                    }
                                } else {
                                    botaoCarregado.setText("");
                                    botaoCarregado.setIcon(null);
                                    botaoCarregado.setEnabled(true);
                                }
                            } else {
                                botaoCarregado.setText("");
                                botaoCarregado.setIcon(null);
                                botaoCarregado.setEnabled(true);
                            }
                        }
                    }
                    areaLog.append("Tabuleiro carregado e atualizado.\n");
                    break;
            }
        });
    }
}
