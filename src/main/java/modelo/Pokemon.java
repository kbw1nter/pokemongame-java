package modelo;

import estrategia.IAtaque;
import java.io.Serializable;

public abstract class Pokemon implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String nome;
    protected String tipo;
    protected int nivel;
    protected int forca;
    protected int energia;
    protected int experiencia;
    protected boolean selvagem;
    protected IAtaque estrategiaAtaque;

    public Pokemon(String nome, String tipo, int nivel, int forca, IAtaque estrategia) {
        this.nome = nome;
        this.tipo = tipo;
        this.nivel = nivel;
        this.forca = forca;
        this.energia = 100;
        this.experiencia = 0;
        this.selvagem = true;
        this.estrategiaAtaque = estrategia;
    }

    public void atacar(Pokemon oponente) {
        int dano = this.estrategiaAtaque.calcularDano(this, oponente);
        oponente.receberDano(dano);
        System.out.println(this.nome + " atacou " + oponente.getNome() + " causando " + dano + " de dano.");
    }

    public void receberDano(int dano) {
        this.energia -= dano;
        if (this.energia < 0) {
            this.energia = 0;
        }
    }

    public void restaurarEnergia() {
        this.energia = 100;
    }

    public void ganharExperiencia(int pontos) {
        this.experiencia += pontos;
    }

    public void setSelvagem(boolean selvagem) {
        this.selvagem = selvagem;
    }

    // getters
    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public int getEnergia() {
        return energia;
    }

    public boolean estaNocauteado() {
        return this.energia <= 0;
    }

    public int getForca() {
        return forca;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean isSelvagem() {
        return selvagem;
    }
}
