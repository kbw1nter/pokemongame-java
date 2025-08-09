package modelo;

import estrategia.AtaqueEletrico;
import estrategia.IAtaque;

public class PokemonEletrico extends Pokemon {
    private static final long serialVersionUID = 1L;

    public PokemonEletrico(String nome, int nivel, int forca, IAtaque estrategia) {
        // chama o construtor da classe pai (pokemon)
        super(nome, "Elétrico", nivel, forca, estrategia);
    }
}
