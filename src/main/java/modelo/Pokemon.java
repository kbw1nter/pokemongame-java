package modelo;

import estrategia.IAtaque;

// classe abstrata que representa um pokemon, metodos e atributos comuns a todos tipo sde pokemon
public abstract class Pokemon {
    protected String nome;
    protected String tipo;
    protected int nivel;
    protected int forca;
    protected int energia;
    protected int experiencia;
    protected boolean selvagem;
    protected IAtaque estrategiaAtaque; // Padrão Strategy

    public Pokemon(String nome, String tipo, int nivel, int forca, IAtaque estrategia) {
        this.nome = nome;
        this.tipo = tipo;
        this.nivel = nivel;
        this.forca = forca;
        this.energia = 100; // Energia inicial
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

    public void ganharExperiencia(int pontos) {
        this.experiencia += pontos;
        // Lógica para avançar de nível pode ser adicionada aqui
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public int getEnergia() { return energia; }
    public boolean estaNocauteado() { return this.energia <= 0; }
}
