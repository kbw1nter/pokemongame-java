package modelo;

import java.io.Serializable;

public class Celula implements Serializable {
    private Pokemon pokemon;
    private final int x;
    private final int y;

    public Celula(int x, int y) {
        this.x = x;
        this.y = y;
        this.pokemon = null; // começa vazia
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    public boolean estaVazia() {
        return this.pokemon == null;
    }

    public void esvaziar() {
        this.pokemon = null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}



