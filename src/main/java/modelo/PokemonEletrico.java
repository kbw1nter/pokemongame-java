package modelo;

import estrategia.AtaqueEletrico;

public class PokemonEletrico extends Pokemon {
    public PokemonEletrico(String nome, int nivel, int forca, AtaqueEletrico ataqueEletrico) {
        // chama o construtor da classe pai (pokemon)
        super(nome, "Elétrico", nivel, forca, new AtaqueEletrico());
    }
}
