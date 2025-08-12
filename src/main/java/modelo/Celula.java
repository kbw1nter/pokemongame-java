package modelo;

import java.io.Serializable;

public class Celula implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Pokemon pokemon;
    private final int x;
    private final int y;
    private boolean visitada;

    public Celula(int x, int y) {
        this.x = x;
        this.y = y;
        this.pokemon = null;
        this.visitada = false;
    }

    // Métodos getters/setters
    public Pokemon getPokemon() { return pokemon; }
    public void setPokemon(Pokemon pokemon) { this.pokemon = pokemon; }
    public boolean estaVazia() { return pokemon == null; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean foiVisitada() { return visitada; }
    public void setVisitada(boolean visitada) { this.visitada = visitada; }
}