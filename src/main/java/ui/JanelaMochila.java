package ui;

import modelo.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class JanelaMochila extends JDialog {
    private final Mochila mochila;
    private final Treinador treinador;
    private JPanel painelPokemons;
    private JLabel lblStatus;
    private JComboBox<String> comboOrdenacao;
    private JComboBox<String> comboFiltroTipo;
    
    public JanelaMochila(JFrame parent, Treinador treinador) {
        super(parent, "Mochila de Pokémons", true);
        this.treinador = treinador;
        this.mochila = treinador.getMochila();
        
        initComponents();
        atualizarInterface();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Painel superior com controles
        JPanel painelControles = new JPanel(new FlowLayout());
        painelControles.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        // ComboBox para ordenação
        painelControles.add(new JLabel("Ordenar por:"));
        comboOrdenacao = new JComboBox<>(new String[]{
            "Nome", "Nível", "Força", "Tipo"
        });
        comboOrdenacao.addActionListener(e -> ordenarPokemons());
        painelControles.add(comboOrdenacao);
        
        painelControles.add(Box.createHorizontalStrut(20));
        
        // ComboBox para filtro por tipo
        painelControles.add(new JLabel("Filtrar por tipo:"));
        comboFiltroTipo = new JComboBox<>(new String[]{
            "Todos", "Água", "Fogo", "Planta", "Elétrico", "Terra", "Voador", "Pedra", "Normal"
        });
        comboFiltroTipo.addActionListener(e -> atualizarInterface());
        painelControles.add(comboFiltroTipo);
        
        add(painelControles, BorderLayout.NORTH);
        
        // Painel central com scroll para os Pokémons
        painelPokemons = new JPanel();
        painelPokemons.setLayout(new BoxLayout(painelPokemons, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(painelPokemons);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Painel inferior com status e botões
        JPanel painelInferior = new JPanel(new BorderLayout());
        
        lblStatus = new JLabel();
        painelInferior.add(lblStatus, BorderLayout.WEST);
        
        JPanel painelBotoes = new JPanel(new FlowLayout());
        
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dispose());
        painelBotoes.add(btnFechar);
        
        JButton btnTransferirTodos = new JButton("Organizar Automaticamente");
        btnTransferirTodos.addActionListener(e -> organizarAutomaticamente());
        btnTransferirTodos.setToolTipText("Move os melhores Pokémons para o time ativo");
        painelBotoes.add(btnTransferirTodos);
        
        painelInferior.add(painelBotoes, BorderLayout.EAST);
        add(painelInferior, BorderLayout.SOUTH);
    }
    
    private void atualizarInterface() {
        painelPokemons.removeAll();
        
        String tipoFiltro = (String) comboFiltroTipo.getSelectedItem();
        
        for (int i = 0; i < mochila.getPokemonsCapturados().size(); i++) {
            Pokemon pokemon = mochila.getPokemonsCapturados().get(i);
            
            // Aplicar filtro por tipo
            if (!tipoFiltro.equals("Todos") && !pokemon.getTipo().equals(tipoFiltro)) {
                continue;
            }
            
            JPanel painelPokemon = criarPainelPokemon(pokemon, i);
            painelPokemons.add(painelPokemon);
            painelPokemons.add(Box.createVerticalStrut(5));
        }
        
        // Atualizar status
        lblStatus.setText(mochila.getStatus());
        
        // Se não há pokémons (ou nenhum passou pelo filtro)
        if (painelPokemons.getComponentCount() == 0) {
            JLabel lblVazio = new JLabel("Nenhum Pokémon encontrado", JLabel.CENTER);
            lblVazio.setFont(new Font("Arial", Font.ITALIC, 16));
            lblVazio.setForeground(Color.GRAY);
            painelPokemons.add(lblVazio);
        }
        
        painelPokemons.revalidate();
        painelPokemons.repaint();
    }
    
    private JPanel criarPainelPokemon(Pokemon pokemon, int indice) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        painel.setBackground(Color.WHITE);
        
        // Painel da imagem
        JPanel painelImagem = new JPanel(new BorderLayout());
        painelImagem.setPreferredSize(new Dimension(100, 100));
        painelImagem.setBackground(Color.WHITE);
        
        try {
            String nomeIcone = "/pokemons/" + pokemon.getNome().toLowerCase() + ".png";
            InputStream is = getClass().getResourceAsStream(nomeIcone);
            
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                JLabel lblImagem = new JLabel(new ImageIcon(scaledImg));
                lblImagem.setHorizontalAlignment(JLabel.CENTER);
                painelImagem.add(lblImagem, BorderLayout.CENTER);
            } else {
                JLabel lblNome = new JLabel(pokemon.getNome(), JLabel.CENTER);
                lblNome.setFont(new Font("Arial", Font.BOLD, 10));
                painelImagem.add(lblNome, BorderLayout.CENTER);
            }
        } catch (IOException e) {
            JLabel lblNome = new JLabel(pokemon.getNome(), JLabel.CENTER);
            lblNome.setFont(new Font("Arial", Font.BOLD, 10));
            painelImagem.add(lblNome, BorderLayout.CENTER);
        }
        
        painel.add(painelImagem, BorderLayout.WEST);
        
        // Painel das informações
        JPanel painelInfo = new JPanel(new GridLayout(4, 2, 5, 2));
        painelInfo.setBackground(Color.WHITE);
        
        painelInfo.add(new JLabel("Nome:"));
        painelInfo.add(new JLabel(pokemon.getNome()));
        
        painelInfo.add(new JLabel("Tipo:"));
        JLabel lblTipo = new JLabel(pokemon.getTipo());
        lblTipo.setForeground(getCorPorTipo(pokemon.getTipo()));
        painelInfo.add(lblTipo);
        
        painelInfo.add(new JLabel("Nível:"));
        painelInfo.add(new JLabel(String.valueOf(pokemon.getNivel())));
        
        painelInfo.add(new JLabel("Força:"));
        painelInfo.add(new JLabel(String.valueOf(pokemon.getForca())));
        
        painel.add(painelInfo, BorderLayout.CENTER);
        
        // Painel de energia
        JPanel painelEnergia = new JPanel(new BorderLayout());
        painelEnergia.setBackground(Color.WHITE);
        
        JProgressBar barraEnergia = new JProgressBar(0, pokemon.getEnergiaMaxima());
        barraEnergia.setValue(pokemon.getEnergia());
        barraEnergia.setString(pokemon.getEnergia() + "/" + pokemon.getEnergiaMaxima());
        barraEnergia.setStringPainted(true);
        
        // Cor da barra baseada na porcentagem de energia
        double porcentagem = (double) pokemon.getEnergia() / pokemon.getEnergiaMaxima();
        if (porcentagem > 0.7) {
            barraEnergia.setForeground(Color.GREEN);
        } else if (porcentagem > 0.3) {
            barraEnergia.setForeground(Color.ORANGE);
        } else {
            barraEnergia.setForeground(Color.RED);
        }
        
        painelEnergia.add(new JLabel("HP:"), BorderLayout.WEST);
        painelEnergia.add(barraEnergia, BorderLayout.CENTER);
        
        painel.add(painelEnergia, BorderLayout.NORTH);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout());
        painelBotoes.setBackground(Color.WHITE);
        
        JButton btnTransferir = new JButton("→ Time");
        btnTransferir.setToolTipText("Transferir para o time ativo");
        btnTransferir.addActionListener(e -> transferirParaTime(indice));
        btnTransferir.setEnabled(treinador.getTime().size() < 6); // Só permite se o time não estiver cheio
        painelBotoes.add(btnTransferir);
        
        JButton btnLiberar = new JButton("Liberar");
        btnLiberar.setToolTipText("Liberar este Pokémon");
        btnLiberar.setForeground(Color.RED);
        btnLiberar.addActionListener(e -> liberarPokemon(indice, pokemon.getNome()));
        painelBotoes.add(btnLiberar);
        
        painel.add(painelBotoes, BorderLayout.SOUTH);
        
        return painel;
    }
    
    private Color getCorPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "água": return new Color(100, 150, 255);
            case "fogo": return new Color(255, 100, 100);
            case "planta": return new Color(100, 200, 100);
            case "elétrico": return new Color(255, 255, 100);
            case "terra": return new Color(150, 100, 50);
            case "voador": return new Color(150, 150, 255);
            case "pedra": return new Color(150, 150, 100);
            default: return Color.BLACK;
        }
    }
    
    private void transferirParaTime(int indice) {
        if (mochila.transferirParaTime(indice)) {
            JOptionPane.showMessageDialog(this, "Pokémon transferido para o time ativo!");
            atualizarInterface();
        } else {
            JOptionPane.showMessageDialog(this, "Não foi possível transferir o Pokémon!", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void liberarPokemon(int indice, String nome) {
        int resposta = JOptionPane.showConfirmDialog(
            this,
            "Tem certeza que deseja liberar " + nome + "?\nEsta ação não pode ser desfeita!",
            "Confirmar Liberação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (resposta == JOptionPane.YES_OPTION) {
            mochila.removerPokemon(indice);
            JOptionPane.showMessageDialog(this, nome + " foi liberado!");
            atualizarInterface();
        }
    }
    
    private void ordenarPokemons() {
        String criterio = (String) comboOrdenacao.getSelectedItem();
        
        switch (criterio) {
            case "Nome":
                mochila.organizarPorNome();
                break;
            case "Nível":
                mochila.organizarPorNivel();
                break;
            case "Força":
                mochila.organizarPorForca();
                break;
            case "Tipo":
                mochila.organizarPorTipo();
                break;
        }
        
        atualizarInterface();
    }
    
    private void organizarAutomaticamente() {
        // Move os melhores pokémons (por força) para o time se houver espaço
        int espacosDisponiveis = 6 - treinador.getTime().size();
        
        if (espacosDisponiveis <= 0) {
            JOptionPane.showMessageDialog(this, "Time já está completo!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Ordena por força primeiro
        mochila.organizarPorForca();
        
        int transferidos = 0;
        for (int i = 0; i < Math.min(espacosDisponiveis, mochila.getQuantidade()); i++) {
            if (mochila.transferirParaTime(0)) { // Sempre transfere o primeiro (mais forte)
                transferidos++;
            }
        }
        
        if (transferidos > 0) {
            JOptionPane.showMessageDialog(this, 
                "Transferidos " + transferidos + " Pokémon(s) para o time ativo!", 
                "Organização Automática", 
                JOptionPane.INFORMATION_MESSAGE);
            atualizarInterface();
        }
    }
}