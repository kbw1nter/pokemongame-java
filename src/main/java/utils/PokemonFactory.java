package utils;

import modelo.*;
import estrategia.*;
import java.util.Random;

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
                break;
            case "floresta":
                String[] nomesFloresta = {"Bulbasaur", "Oddish", "Bellsprout"};
                nomePokemon = nomesFloresta[random.nextInt(nomesFloresta.length)];
                break;
            case "terra":
                String[] nomesTerra = {"Sandshrew", "Diglett", "Geodude"};
                nomePokemon = nomesTerra[random.nextInt(nomesTerra.length)];
                break;
            case "elétrico":
                String[] nomesEletrico = {"Pikachu", "Jolteon", "Magnemite"};
                nomePokemon = nomesEletrico[random.nextInt(nomesEletrico.length)];
                break;
            default:
                throw new IllegalArgumentException("Tipo de Pokémon selvagem desconhecido: " + tipo);
        }
        return criarPokemon(tipo, nomePokemon, nivel, forca);
    }
}
