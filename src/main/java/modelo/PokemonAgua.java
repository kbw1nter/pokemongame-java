package modelo;

import estrategia.AtaqueAgua;

public class PokemonAgua extends Pokemon {
    public PokemonAgua(String nome, int nivel, int forca) {
        super(nome, "Água", nivel, forca, new AtaqueAgua());
    }
}
