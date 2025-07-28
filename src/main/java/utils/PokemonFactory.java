package utils;

import modelo.*;
import estrategia.*;

public class PokemonFactory {
    public static Pokemon criarPokemon(String tipo, String nome, int nivel, int forca) {
        switch (tipo.toLowerCase()) {
            case "água":
                return new Pokemon("Água", nome, nivel, forca, new AtaqueAgua());
            case "floresta":
                return new Pokemon("Floresta", nome, nivel, forca, new AtaqueFloresta());
            case "terra":
                return new Pokemon("Terra", nome, nivel, forca, new AtaqueTerra());
            case "elétrico":
                return new Pokemon("Elétrico", nome, nivel, forca, new AtaqueEletrico());
            default:
                throw new IllegalArgumentException("Tipo de Pokémon desconhecido: " + tipo);
        }
    }
}
