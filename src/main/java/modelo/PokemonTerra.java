package modelo;

import estrategia.IAtaque;

public class PokemonTerra extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonTerra(String nome, int nivel, int forca, IAtaque estrategia) {
        super(nome, "Terra", nivel, forca, estrategia);
    }
}