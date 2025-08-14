package utils;

import modelo.*;
import estrategia.*;
import java.util.Random;

public class PokemonFactory {
    private static final Random random = new Random();
    
    public static Pokemon criarPokemonAleatorio() {
        int tipo = random.nextInt(4); // 0-3 para os 4 tipos
        
        switch(tipo) {
            case 0: return criarPokemonAgua();
            case 1: return criarPokemonFloresta();
            case 2: return criarPokemonEletrico();
            case 3: return criarPokemonTerra();
            default: return criarPokemonFloresta();
        }
    }
    
    public static Pokemon criarPokemonAgua() {
        String[] nomes = {"Squirtle", "Totodile", "Mudkip"};
        String nome = nomes[random.nextInt(nomes.length)];
        int nivel = random.nextInt(5) + 1;
        int forca = 30 + (nivel * 10);
        return new PokemonAgua(nome, nivel, forca, new AtaqueAgua());
    }
    
    public static Pokemon criarPokemonFloresta() {
        String[] nomes = {"Bulbasaur", "Chikorita", "Treecko"};
        String nome = nomes[random.nextInt(nomes.length)];
        int nivel = random.nextInt(5) + 1;
        int forca = 35 + (nivel * 8);
        return new PokemonFloresta(nome, nivel, forca, new AtaqueFloresta());
    }
    
    public static Pokemon criarPokemonEletrico() {
        String[] nomes = {"Pikachu", "Elekid", "Shinx"};
        String nome = nomes[random.nextInt(nomes.length)];
        int nivel = random.nextInt(5) + 1;
        int forca = 25 + (nivel * 11);
        return new PokemonEletrico(nome, nivel, forca, new AtaqueEletrico());
    }
    
    public static Pokemon criarPokemonTerra() {
        String[] nomes = {"Sandshrew", "Diglett", "Trapinch"};
        String nome = nomes[random.nextInt(nomes.length)];
        int nivel = random.nextInt(5) + 1;
        int forca = 40 + (nivel * 7);
        return new PokemonTerra(nome, nivel, forca, new AtaqueTerra());
    }
    public static Pokemon criarPokemonInicialJogador() {
        int nivelInicial = 5;
        int forcaInicial = 10;
        
        Pokemon[] iniciais = {
            new PokemonEletrico("Pikachu", nivelInicial, forcaInicial, new AtaqueEletrico()),
            new PokemonAgua("Squirtle", nivelInicial, forcaInicial, new AtaqueAgua()),
            new PokemonFloresta("Bulbasaur", nivelInicial, forcaInicial, new AtaqueTerra())
        };
        return iniciais[new Random().nextInt(iniciais.length)];
    }
    
    public static Pokemon criarPokemonInicialComputador() {
        int nivelInicial = 5;
        int forcaInicial = 10;
        
        Pokemon[] iniciais = {
            new PokemonEletrico("Shinx", nivelInicial, forcaInicial, new AtaqueEletrico()),
            new PokemonAgua("Mudkip", nivelInicial, forcaInicial, new AtaqueAgua()),
            new PokemonFloresta("Chikorita", nivelInicial, forcaInicial, new AtaqueFloresta())
        };
        return iniciais[new Random().nextInt(iniciais.length)];
    }

}