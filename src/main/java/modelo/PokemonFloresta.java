package modelo;

import estrategia.AtaqueFloresta;

public class PokemonFloresta extends Pokemon {
    public PokemonFloresta(String nome, int nivel, int forca) {
        super(nome, "Floresta", nivel, forca, new AtaqueFloresta());
    }
}
