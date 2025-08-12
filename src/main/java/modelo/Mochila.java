package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Mochila implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int CAPACIDADE_MAXIMA = 30; // Limite de Pokémons na mochila
    private List<Pokemon> pokemonsCapturados;
    private Treinador proprietario;

    public Mochila(Treinador proprietario) {
        this.proprietario = proprietario;
        this.pokemonsCapturados = new ArrayList<>();
    }

    // Adiciona um Pokémon à mochila
    public boolean adicionarPokemon(Pokemon pokemon) {
        if (pokemonsCapturados.size() >= CAPACIDADE_MAXIMA) {
            System.out.println("Mochila cheia! Não é possível capturar mais Pokémons.");
            return false;
        }
        
        if (pokemon != null) {
            pokemon.setTreinador(proprietario);
            pokemonsCapturados.add(pokemon);
            System.out.println(pokemon.getNome() + " foi adicionado à mochila!");
            return true;
        }
        return false;
    }

    // Remove um Pokémon da mochila por índice
    public Pokemon removerPokemon(int indice) {
        if (indice >= 0 && indice < pokemonsCapturados.size()) {
            Pokemon pokemon = pokemonsCapturados.remove(indice);
            System.out.println(pokemon.getNome() + " foi removido da mochila.");
            return pokemon;
        }
        System.out.println("Índice inválido!");
        return null;
    }

    // Remove um Pokémon específico da mochila
    public boolean removerPokemon(Pokemon pokemon) {
        if (pokemonsCapturados.remove(pokemon)) {
            System.out.println(pokemon.getNome() + " foi removido da mochila.");
            return true;
        }
        System.out.println("Pokémon não encontrado na mochila!");
        return false;
    }

    // Transfere um Pokémon da mochila para o time ativo
    public boolean transferirParaTime(int indiceMochila) {
        if (indiceMochila >= 0 && indiceMochila < pokemonsCapturados.size()) {
            Pokemon pokemon = pokemonsCapturados.get(indiceMochila);
            
            // Verifica se o time já está cheio (máximo 6 Pokémons no time ativo)
            if (proprietario.getTime().size() >= 6) {
                System.out.println("Time cheio! Não é possível adicionar mais Pokémons ao time ativo.");
                return false;
            }
            
            // Remove da mochila e adiciona ao time
            pokemonsCapturados.remove(indiceMochila);
            proprietario.adicionarAoTime(pokemon);
            System.out.println(pokemon.getNome() + " foi transferido para o time ativo!");
            return true;
        }
        System.out.println("Índice inválido!");
        return false;
    }

    // Transfere um Pokémon do time ativo para a mochila
    public boolean transferirDoTime(int indiceTime) {
        List<Pokemon> time = proprietario.getTime();
        if (indiceTime >= 0 && indiceTime < time.size()) {
            if (pokemonsCapturados.size() >= CAPACIDADE_MAXIMA) {
                System.out.println("Mochila cheia! Não é possível guardar o Pokémon.");
                return false;
            }
            
            Pokemon pokemon = time.remove(indiceTime);
            pokemonsCapturados.add(pokemon);
            
            // Se era o Pokémon atual, define um novo ou null
            if (proprietario.getPokemonAtual() == pokemon) {
                if (!time.isEmpty()) {
                    proprietario.setPokemonAtual(time.get(0));
                } else {
                    proprietario.setPokemonAtual(null);
                }
            }
            
            System.out.println(pokemon.getNome() + " foi guardado na mochila!");
            return true;
        }
        System.out.println("Índice inválido!");
        return false;
    }

    // Busca um Pokémon por nome
    public Pokemon buscarPokemon(String nome) {
        for (Pokemon pokemon : pokemonsCapturados) {
            if (pokemon.getNome().equalsIgnoreCase(nome)) {
                return pokemon;
            }
        }
        return null;
    }

    // Lista todos os Pokémons na mochila
    public void listarPokemons() {
        if (pokemonsCapturados.isEmpty()) {
            System.out.println("Mochila vazia!");
            return;
        }
        
        System.out.println("=== POKÉMONS NA MOCHILA ===");
        for (int i = 0; i < pokemonsCapturados.size(); i++) {
            Pokemon p = pokemonsCapturados.get(i);
            System.out.println((i + 1) + ". " + p.getNome() + 
                             " - Tipo: " + p.getTipo() + 
                             " - HP: " + p.getEnergia() + "/" + p.getEnergiaMaxima() +
                             " - Nível: " + p.getNivel());
        }
        System.out.println("Espaços ocupados: " + pokemonsCapturados.size() + "/" + CAPACIDADE_MAXIMA);
    }

    // Organiza os Pokémons por tipo
    public void organizarPorTipo() {
        pokemonsCapturados.sort((p1, p2) -> p1.getTipo().compareTo(p2.getTipo()));
        System.out.println("Pokémons organizados por tipo!");
    }

    // Organiza os Pokémons por nível (maior para menor)
    public void organizarPorNivel() {
        pokemonsCapturados.sort((p1, p2) -> Integer.compare(p2.getNivel(), p1.getNivel()));
        System.out.println("Pokémons organizados por nível!");
    }

    // Organiza os Pokémons por força (maior para menor)
    public void organizarPorForca() {
        pokemonsCapturados.sort((p1, p2) -> Integer.compare(p2.getForca(), p1.getForca()));
        System.out.println("Pokémons organizados por força!");
    }

    // Organiza os Pokémons por nome (ordem alfabética)
    public void organizarPorNome() {
        pokemonsCapturados.sort((p1, p2) -> p1.getNome().compareTo(p2.getNome()));
        System.out.println("Pokémons organizados por nome!");
    }

    // Conta quantos Pokémons de um tipo específico há na mochila
    public int contarPorTipo(String tipo) {
        int contador = 0;
        for (Pokemon pokemon : pokemonsCapturados) {
            if (pokemon.getTipo().equalsIgnoreCase(tipo)) {
                contador++;
            }
        }
        return contador;
    }

    // Verifica se há espaço na mochila
    public boolean temEspaco() {
        return pokemonsCapturados.size() < CAPACIDADE_MAXIMA;
    }

    // Getters
    public List<Pokemon> getPokemonsCapturados() {
        return new ArrayList<>(pokemonsCapturados); // Retorna cópia para evitar modificações externas
    }

    public int getQuantidade() {
        return pokemonsCapturados.size();
    }

    public int getCapacidadeMaxima() {
        return CAPACIDADE_MAXIMA;
    }

    public Treinador getProprietario() {
        return proprietario;
    }

    // Status da mochila
    public String getStatus() {
        return "Mochila: " + pokemonsCapturados.size() + "/" + CAPACIDADE_MAXIMA + " Pokémons";
    }
}