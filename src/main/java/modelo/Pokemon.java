package modelo;

import estrategia.IAtaque;
import java.io.Serializable;
import java.util.Random;

public abstract class Pokemon implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String nome;
    protected String tipo;
    protected int nivel;
    protected int forca;
    protected int energia;
    protected int experiencia;
    protected IAtaque estrategiaAtaque;
    protected Treinador treinador;
    protected double taxaFuga;
    protected boolean selvagem;

    public Pokemon(String nome, String tipo, int nivel, int forca, IAtaque estrategia) {
        this.nome = nome;
        this.tipo = tipo;
        this.nivel = nivel;
        this.forca = forca;
        this.energia = 100;
        this.experiencia = 0;
        this.estrategiaAtaque = estrategia;
        this.treinador = null;
        this.selvagem = true;
        this.taxaFuga = 0.3; // Valor padrão
    }

    // Métodos públicos
    public int atacar(Pokemon oponente) {
        return this.estrategiaAtaque.calcularDano(this, oponente);
    }

    public void receberDano(int dano) {
        this.energia -= dano;
        if (this.energia < 0) this.energia = 0;
    }

    public void restaurarEnergia() {
        this.energia = 100;
    }

    // Getters e Setters públicos
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getNivel() { return nivel; }
    public int getForca() { return forca; }
    public int getEnergia() { return energia; }
    public boolean estaNocauteado() { return energia <= 0; }
    public boolean isSelvagem() { return selvagem; }
    public Treinador getTreinador() { return treinador; }
    public double getTaxaFuga() { return taxaFuga; }

    public void setTreinador(Treinador treinador) {
        this.treinador = treinador;
        this.selvagem = (treinador == null);
    }

    public boolean tentarFugir() {
        return isSelvagem() && Math.random() < taxaFuga;
    }
    
    public boolean tentarCapturar() {
    // Computador tem 10% a menos de chance que o jogador
    double chanceBase = 0.5; // 50% base para computador
    double modificadorNivel = 0.05 * this.nivel;
    double modificadorEnergia = 0.01 * (100 - this.energia);
    
    double chanceTotal = Math.max(0.2, Math.min(0.7, chanceBase - modificadorNivel + modificadorEnergia));
    
    return new Random().nextDouble() < chanceTotal;
}
    public int getEnergiaMaxima() {
        return 100; // Ou qualquer valor máximo que você queira definir
    }
    
    
    
    // Adicione também o import se necessário
}