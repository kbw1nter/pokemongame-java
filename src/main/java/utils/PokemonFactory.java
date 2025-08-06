package utils;

import estrategia.AtaqueAgua;
import estrategia.AtaqueEletrico;
import estrategia.AtaqueFloresta;
import estrategia.AtaqueTerra;
import modelo.*;

import java.util.Random;
import java.util.random.*;

public class PokemonFactory {
    public static Pokemon criarPokemon(String tipo, String nome, int nivel, int forca) {
        return switch (tipo.toLowerCase()) {
            case "água" -> new PokemonAgua(nome, nivel, forca, new AtaqueAgua());
            case "floresta" -> new PokemonFloresta(nome, nivel, forca, new AtaqueFloresta());
            case "terra" -> new PokemonTerra(nome, nivel, forca, new AtaqueTerra());
            case "elétrico" -> new PokemonEletrico(nome, nivel, forca, new AtaqueEletrico());
            default -> throw new IllegalArgumentException("Tipo de Pokémon desconhecido: " + tipo);
        };
    }

    public static Pokemon criarPokemonSelvagem(String tipo, int nivel, int forca) {
        Random random = new Random();
        String nomePokemon;
        switch (tipo.toLowerCase()) {
            case "água":
                String[] nomesAgua = {"Squirtle", "Psyduck", "Magikarp"};
                nomePokemon = nomesAgua[random.nextInt(nomesAgua.length)];
                return criarPokemon(tipo, nomePokemon, nivel, forca);
            case "floresta":
                String[] nomesFloresta = {"Bulbasaur", "Oddish", "Bellsprout"};
                nomePokemon = nomesFloresta[random.nextInt(nomesFloresta.length)];
                return criarPokemon(tipo, nomePokemon, nivel, forca);
            case "terra":
                String[] nomesTerra = {"Sandshrew", "Diglett", "Geodude"};
                nomePokemon = nomesTerra[random.nextInt(nomesTerra.length)];
                return criarPokemon(tipo, nomePokemon, nivel, forca);
            case "elétrico":
                String[] nomesEletrico = {"Pikachu", "Jolteon", "Magnemite"};
                nomePokemon = nomesEletrico[random.nextInt(nomesEletrico.length)];
                return criarPokemon(tipo, nomePokemon, nivel, forca);
            default:
                throw new IllegalArgumentException("Tipo de Pokémon selvagem desconhecido: " + tipo);
        }
    }
}
