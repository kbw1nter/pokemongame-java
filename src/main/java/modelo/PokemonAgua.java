package modelo;

import estrategia.IAtaque;

public class PokemonAgua extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonAgua(String nome, int nivel, int forca, IAtaque estrategia) {
        super(nome, "Água", nivel, forca, estrategia);
    }
}

