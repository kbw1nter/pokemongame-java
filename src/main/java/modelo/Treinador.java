package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Treinador implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final List<Pokemon> time;
    private int pontuacao;
    private Pokemon pokemonAtual; // Adicionado para rastrear o Pokémon principal em batalha

    public Treinador(String nome) {
        this.nome = nome;
        this.time = new ArrayList<>();
        this.pontuacao = 0;
        this.pokemonAtual = null; // Inicialmente sem Pokémon atual
    }

    public void capturarPokemon(Pokemon pokemon) {
        pokemon.setSelvagem(false);
        this.time.add(pokemon);
        if (this.pokemonAtual == null) {
            this.pokemonAtual = pokemon; // Define o primeiro Pokémon capturado como o atual
        }
        System.out.println(this.nome + " capturou " + pokemon.getNome() + "!");
    }

    public Pokemon getPokemonPrincipal() {
        if (time.isEmpty()) {
            return null;
        }
        return time.getFirst(); // retorna o primeiro Pokémon do time
    }

    public void adicionarPontos(int pontos) {
        this.pontuacao += pontos;
    }

    // getters
    public String getNome() {
        return nome;
    }
    public List<Pokemon> getTime() {
        return time;
    }
    public int getPontuacao() {
        return pontuacao;
    }

    // método para obter o status do treinador (nome e pontuação)
    public String getStatus() {
        return nome + ": " + pontuacao + " pts | Pokémon: " + (pokemonAtual != null ? pokemonAtual.getNome() + " (HP: " + pokemonAtual.getPontosVida() + ")" : "Nenhum");
    }

    public Pokemon getPokemonAtual() {
        return pokemonAtual;
    }

    public void setPokemonAtual(Pokemon pokemonAtual) {
        this.pokemonAtual = pokemonAtual;
    }
}