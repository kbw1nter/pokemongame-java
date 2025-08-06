package jogo;

import estrategia.AtaqueAgua;
import estrategia.AtaqueFloresta;
import modelo.Pokemon;
import modelo.PokemonAgua;
import modelo.PokemonFloresta;
import modelo.Treinador;

public class Jogo {
    public static void main(String[] args) {
        // exemplo de como as classes podem interagir
        Treinador jogador = new Treinador("Ash");
        Treinador computador = new Treinador("Gary");

        // criação de pokemons
        Pokemon p1 = new PokemonAgua("Squirtle", 5, 12, new AtaqueAgua());
        Pokemon p2 = new PokemonFloresta("Bulbasaur", 5, 11, new AtaqueFloresta());

        jogador.capturarPokemon(p1);
        computador.capturarPokemon(p2);

        System.out.println("--- Início da Batalha ---");

        Pokemon pokemonJogador = jogador.getPokemonPrincipal();
        Pokemon pokemonComputador = computador.getPokemonPrincipal();

        // simulação de uma batalha em turnos
        while (!pokemonJogador.estaNocauteado() && !pokemonComputador.estaNocauteado()) {
            // turno do jogador
            pokemonJogador.atacar(pokemonComputador);
            if (pokemonComputador.estaNocauteado()) break;

            // turno do computador
            pokemonComputador.atacar(pokemonJogador);
        }

        System.out.println("--- Fim da Batalha ---");

        if (pokemonJogador.estaNocauteado()) {
            System.out.println(pokemonComputador.getNome() + " venceu!");
            computador.adicionarPontos(100);
        } else {
            System.out.println(pokemonJogador.getNome() + " venceu!");
            jogador.adicionarPontos(100);
        }

        System.out.println("Pontuação final: " + jogador.getNome() + " - " + jogador.getPontuacao());
        System.out.println("Pontuação final: " + computador.getNome() + " - " + computador.getPontuacao());
    }
}
