package modelo;

import estrategia.AtaqueTerra;
import estrategia.IAtaque;

public class PokemonTerra extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonTerra(String nome, int nivel, int forca, IAtaque estrategia) {
        // chama o construtor da classe pai (pokemon)
        super(nome, "Terra", nivel, forca, estrategia);
    }
}
