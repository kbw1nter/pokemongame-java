package utils;

import modelo.*;
import estrategia.*;
import java.util.Random;

public class PokemonFactory {

    private static final String[] NOMES_AGUA = {"squirtle", "magikarp", "psyduck", "vaporeon"};
    private static final String[] NOMES_FLORESTA = {"bulbasaur", "bellsprout", "chikorita", "oddish"};
    private static final String[] NOMES_TERRA = {"cubone", "diglett", "geodude", "sandshrew"};
    private static final String[] NOMES_ELETRICO = {"pikachu", "jolteon", "magnemite", "voltorb"};

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
        String nome;

        switch (tipo.toLowerCase()) {
            case "água":
                nome = NOMES_AGUA[random.nextInt(NOMES_AGUA.length)];
                return new PokemonAgua(nome, nivel, forca, new AtaqueAgua());
            case "floresta":
                nome = NOMES_FLORESTA[random.nextInt(NOMES_FLORESTA.length)];
                return new PokemonFloresta(nome, nivel, forca, new AtaqueFloresta());
            case "terra":
                nome = NOMES_TERRA[random.nextInt(NOMES_TERRA.length)];
                return new PokemonTerra(nome, nivel, forca, new AtaqueTerra());
            case "elétrico":
                nome = NOMES_ELETRICO[random.nextInt(NOMES_ELETRICO.length)];
                return new PokemonEletrico(nome, nivel, forca, new AtaqueEletrico());
            default:
                throw new IllegalArgumentException("Tipo de Pokémon selvagem desconhecido: " + tipo);
        }
    }

    public static Pokemon criarPokemonAleatorio() {
        Random random = new Random();
        String[] tipos = {"água", "floresta", "terra", "elétrico"};
        String tipo = tipos[random.nextInt(tipos.length)];
        int nivel = random.nextInt(10) + 1; // Nível entre 1 e 10
        int forca = random.nextInt(15) + 5; // Força entre 5 e 19

        return criarPokemonSelvagem(tipo, nivel, forca);
    }
}
