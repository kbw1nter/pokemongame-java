package modelo;

import estrategia.AtaqueTerra;

public class PokemonTerra extends Pokemon {
    public PokemonTerra(String nome, int nivel, int forca, AtaqueTerra ataqueTerra) {
        // chama o construtor da classe pai (pokemon)
        super(nome, "Terra", nivel, forca, new AtaqueTerra());
    }
}
