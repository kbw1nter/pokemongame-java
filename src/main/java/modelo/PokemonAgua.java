package modelo;

import estrategia.AtaqueAgua;

public class PokemonAgua extends Pokemon {
    public PokemonAgua(String nome, int nivel, int forca, AtaqueAgua ataqueAgua) {
        super(nome, "Água", nivel, forca, new AtaqueAgua());
    }
}
