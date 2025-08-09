package modelo;

import estrategia.IAtaque;

public class PokemonEletrico extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonEletrico(String nome, int nivel, int forca, IAtaque estrategia) {
        super(nome, "Elétrico", nivel, forca, estrategia);
    }
}
