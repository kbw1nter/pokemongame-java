package modelo;

import java.io.Serializable;

public class Celula implements Serializable {
    private Pokemon pokemon;
    private final int x;
    private final int y;
    private boolean visitada; // Adicionado para controlar se a célula foi visitada

    public Celula(int x, int y) {
        this.x = x;
        this.y = y;
        this.pokemon = null; // começa vazia
        this.visitada = false; // Inicialmente não visitada
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

    // método para marcar a célula como visitada
    public void setVisitada(boolean visitada) {
        this.visitada = visitada;
    }

    // método para verificar se a célula foi visitada
    public boolean foiVisitada() {
        return visitada;
    }
}