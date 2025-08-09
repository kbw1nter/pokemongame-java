package modelo;

import estrategia.IAtaque;

public class PokemonFloresta extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonFloresta(String nome, int nivel, int forca, IAtaque estrategia) {
        super(nome, "Floresta", nivel, forca, estrategia);
    }
}

