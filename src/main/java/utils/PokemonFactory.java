package utils;

import modelo.*;

public class PokemonFactory {
    public static Pokemon criarPokemon(String tipo, String nome, int nivel, int forca) {
        return switch (tipo.toLowerCase()) {
            case "água" -> new PokemonAgua(nome, nivel, forca);
            case "floresta" -> new PokemonFloresta(nome, nivel, forca);
            case "terra" -> new PokemonTerra(nome, nivel, forca);
            case "elétrico" -> new PokemonEletrico(nome, nivel, forca);
            default -> throw new IllegalArgumentException("Tipo de Pokémon desconhecido: " + tipo);
        };
    }
}
