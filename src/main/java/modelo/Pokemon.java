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
    protected int energiaMaxima;
    
    // Constantes para sistema de experiência
    private static final int EXP_BASE_VITORIA = 25;
    private static final int EXP_POR_NIVEL = 50;

    public Pokemon(String nome, String tipo, int nivel, int forca, IAtaque estrategia) {
    this.nome = nome;
    this.tipo = tipo;
    this.nivel = nivel;
    this.forca = forca;
    this.energiaMaxima = 100 + (nivel * 10); // HP aumenta com o nível
    this.energia = this.energiaMaxima; // Começa com HP máximo
    this.experiencia = 0;
    this.estrategiaAtaque = estrategia;
    this.treinador = null;
    this.selvagem = true;
    this.taxaFuga = 0.3;
}


    // Métodos públicos
    public int atacar(Pokemon oponente) {
        return this.estrategiaAtaque.calcularDano(this, oponente);
    }

    public boolean receberDano(int dano) {
    this.energia = Math.max(0, this.energia - dano);
    return estaNocauteado();
}

    public void restaurarEnergia() {
    this.energia = this.energiaMaxima;
}
    
    private void subirNivel() {
    this.nivel++;
    this.forca += 5;
    int aumentoHP = 10; // Aumenta HP máximo ao subir de nível
    this.energiaMaxima += aumentoHP;
    this.energia += aumentoHP; // Recupera o aumento
    
    System.out.println(this.nome + " subiu para o nível " + this.nivel + "!");
    System.out.println("Força aumentou para " + this.forca + "!");
    System.out.println("HP máximo aumentou para " + this.energiaMaxima + "!");
}
    

    // Métodos de experiência
    public void ganharExperiencia(int pontos) {
        this.experiencia += pontos;
        verificarSubidaNivel();
    }
    
    public int ganharExperienciaPorVitoria(Pokemon oponenteDerrotado) {
        int expGanha = calcularExperienciaVitoria(oponenteDerrotado);
        ganharExperiencia(expGanha);
        return expGanha;
    }

    private int calcularExperienciaVitoria(Pokemon oponente) {
        // Experiência fixa de 25 pontos por vitória
        return EXP_BASE_VITORIA;
    }

    private void verificarSubidaNivel() {
        int expNecessaria = calcularExpNecessaria();
        
        while (this.experiencia >= expNecessaria) {
            subirNivel();
            expNecessaria = calcularExpNecessaria();
        }
    }

    private int calcularExpNecessaria() {
        // Fórmula: 50 * nível atual
        return EXP_POR_NIVEL * this.nivel;
    }

    public int getExpParaProximoNivel() {
        return calcularExpNecessaria() - this.experiencia;
    }

    public double getProgressoNivel() {
        int expNecessaria = calcularExpNecessaria();
        return (double) this.experiencia / expNecessaria * 100;
    }

    // Getters e Setters públicos
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getNivel() { return nivel; }
    public int getForca() { return forca; }
    public int getEnergia() { return energia; }
    public int getExperiencia() { return experiencia; }
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
    return this.energiaMaxima;
}
    
    public int curarEnergia(int quantidade) {
    if (quantidade <= 0) return 0;
    
    int energiaAnterior = this.energia;
    int energiaMaxima = this.energiaMaxima;
    
    // Calcula nova energia sem exceder o máximo
    int novaEnergia = Math.min(energiaMaxima, energiaAnterior + quantidade);
    
    // Atualiza a energia
    this.energia = novaEnergia;
    
    // Retorna a quantidade real curada
    return novaEnergia - energiaAnterior;
}
    
    
}