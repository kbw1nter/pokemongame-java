package modelo;

import java.util.ArrayList;
import java.util.List;

public class Treinador {
    private final String nome;
    private final List<Pokemon> time;
    private int pontuacao;

    public Treinador(String nome) {
        this.nome = nome;
        this.time = new ArrayList<>();
        this.pontuacao = 0;
    }

    public void capturarPokemon(Pokemon pokemon) {
        pokemon.setSelvagem(false);
        this.time.add(pokemon);
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
}
