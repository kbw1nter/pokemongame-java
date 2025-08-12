package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Treinador implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nome;
    private List<Pokemon> time; // Time ativo (máximo 6 Pokémons)
    private Mochila mochila; // Mochila para armazenar Pokémons capturados
    private int pontuacao;
    private Pokemon pokemonAtual;

    public Treinador(String nome) {
        this.nome = nome;
        this.time = new ArrayList<>();
        this.mochila = new Mochila(this);
        this.pontuacao = 0;
        this.pokemonAtual = null;
    }

    // Método principal para capturar Pokémons
    public boolean capturarPokemon(Pokemon pokemon) {
        if (pokemon == null) {
            System.out.println("Pokémon inválido!");
            return false;
        }

        // Se o time ativo tem espaço (menos de 6), adiciona diretamente ao time
        if (time.size() < 6) {
            pokemon.setTreinador(this);
            this.time.add(pokemon);
            
            if (this.pokemonAtual == null) {
                this.pokemonAtual = pokemon;
            }
            
            System.out.println(this.nome + " capturou " + pokemon.getNome() + " e o adicionou ao time ativo!");
            return true;
        }
        
        // Se o time está cheio, adiciona à mochila
        if (mochila.adicionarPokemon(pokemon)) {
            System.out.println(this.nome + " capturou " + pokemon.getNome() + " e o guardou na mochila!");
            return true;
        }
        
        System.out.println("Não foi possível capturar " + pokemon.getNome() + " - time e mochila cheios!");
        return false;
    }

    public Pokemon getPokemonPrincipal() {
        if (time.isEmpty()) {
            return null;
        }
        return time.get(0);
    }

    public void adicionarPontos(int pontos) {
        this.pontuacao += pontos;
    }

    // Método para alternar Pokémon atual do time ativo
    public boolean trocarPokemonAtual(int indice) {
        if (indice >= 0 && indice < time.size()) {
            Pokemon novoPokemon = time.get(indice);
            if (novoPokemon.estaNocauteado()) {
                System.out.println(novoPokemon.getNome() + " está nocauteado e não pode batalhar!");
                return false;
            }
            this.pokemonAtual = novoPokemon;
            System.out.println("Pokémon principal alterado para " + pokemonAtual.getNome() + "!");
            return true;
        }
        System.out.println("Índice inválido para trocar Pokémon!");
        return false;
    }

    // Troca Pokémon principal escolhendo da mochila
    public boolean trocarPokemonPrincipalDaMochila(int indiceMochila) {
        List<Pokemon> pokemonsMochila = mochila.getPokemonsCapturados();
        
        if (indiceMochila < 0 || indiceMochila >= pokemonsMochila.size()) {
            System.out.println("Índice inválido na mochila!");
            return false;
        }
        
        Pokemon pokemonEscolhido = pokemonsMochila.get(indiceMochila);
        
        if (pokemonEscolhido.estaNocauteado()) {
            System.out.println(pokemonEscolhido.getNome() + " está nocauteado e não pode batalhar!");
            return false;
        }
        
        // Se o time estiver cheio, precisa fazer uma troca
        if (time.size() >= 6) {
            System.out.println("Time cheio! Escolha um Pokémon do time para trocar:");
            mostrarTime();
            return false; // Precisará chamar trocarPokemonComMochila() em seguida
        }
        
        // Remove da mochila e adiciona ao time
        mochila.removerPokemon(pokemonEscolhido);
        time.add(pokemonEscolhido);
        this.pokemonAtual = pokemonEscolhido;
        
        System.out.println(pokemonEscolhido.getNome() + " foi movido da mochila para o time e é agora o Pokémon principal!");
        return true;
    }

    // Troca um Pokémon do time com um da mochila e define como principal
    public boolean trocarPokemonComMochila(int indiceTime, int indiceMochila) {
        if (indiceTime < 0 || indiceTime >= time.size()) {
            System.out.println("Índice do time inválido!");
            return false;
        }
        
        List<Pokemon> pokemonsMochila = mochila.getPokemonsCapturados();
        if (indiceMochila < 0 || indiceMochila >= pokemonsMochila.size()) {
            System.out.println("Índice da mochila inválido!");
            return false;
        }
        
        Pokemon pokemonTime = time.get(indiceTime);
        Pokemon pokemonMochila = pokemonsMochila.get(indiceMochila);
        
        if (pokemonMochila.estaNocauteado()) {
            System.out.println(pokemonMochila.getNome() + " está nocauteado e não pode batalhar!");
            return false;
        }
        
        // Realiza a troca
        time.set(indiceTime, pokemonMochila);
        mochila.removerPokemon(pokemonMochila);
        mochila.adicionarPokemon(pokemonTime);
        
        // Define como Pokémon principal
        this.pokemonAtual = pokemonMochila;
        
        // Atualiza referência se era o Pokémon atual
        if (pokemonAtual == pokemonTime) {
            pokemonAtual = pokemonMochila;
        }
        
        System.out.println("Troca realizada! " + pokemonMochila.getNome() + 
                         " (da mochila) trocou de lugar com " + pokemonTime.getNome() + 
                         " (do time) e é agora o Pokémon principal!");
        return true;
    }

    // Lista Pokémons disponíveis para troca (não nocauteados)
    public void listarPokemonsDisponiveis() {
        System.out.println("=== POKÉMONS DISPONÍVEIS PARA BATALHA ===");
        
        System.out.println("\n--- TIME ATIVO ---");
        boolean temDisponivel = false;
        for (int i = 0; i < time.size(); i++) {
            Pokemon p = time.get(i);
            if (!p.estaNocauteado()) {
                String status = (p == pokemonAtual) ? " [PRINCIPAL]" : "";
                System.out.println((i + 1) + ". " + p.getNome() + 
                                 " - HP: " + p.getEnergia() + "/" + p.getEnergiaMaxima() +
                                 " - Nível: " + p.getNivel() + status);
                temDisponivel = true;
            }
        }
        
        System.out.println("\n--- MOCHILA ---");
        List<Pokemon> pokemonsMochila = mochila.getPokemonsCapturados();
        for (int i = 0; i < pokemonsMochila.size(); i++) {
            Pokemon p = pokemonsMochila.get(i);
            if (!p.estaNocauteado()) {
                System.out.println("M" + (i + 1) + ". " + p.getNome() + 
                                 " - HP: " + p.getEnergia() + "/" + p.getEnergiaMaxima() +
                                 " - Nível: " + p.getNivel());
                temDisponivel = true;
            }
        }
        
        if (!temDisponivel) {
            System.out.println("Nenhum Pokémon disponível para batalha!");
        }
    }

    // Encontra automaticamente o melhor Pokémon para ser o principal
    public boolean escolherMelhorPokemonPrincipal() {
        Pokemon melhorPokemon = null;
        int melhorPontuacao = -1;
        
        // Verifica time ativo primeiro
        for (Pokemon p : time) {
            if (!p.estaNocauteado()) {
                int pontuacao = calcularPontuacaoPokemon(p);
                if (pontuacao > melhorPontuacao) {
                    melhorPontuacao = pontuacao;
                    melhorPokemon = p;
                }
            }
        }
        
        // Verifica mochila se não achou no time ou quer uma opção melhor
        for (Pokemon p : mochila.getPokemonsCapturados()) {
            if (!p.estaNocauteado()) {
                int pontuacao = calcularPontuacaoPokemon(p);
                if (pontuacao > melhorPontuacao) {
                    // Se encontrou um melhor na mochila, tenta movê-lo para o time
                    if (time.size() < 6) {
                        mochila.removerPokemon(p);
                        time.add(p);
                        melhorPontuacao = pontuacao;
                        melhorPokemon = p;
                    }
                }
            }
        }
        
        if (melhorPokemon != null) {
            this.pokemonAtual = melhorPokemon;
            System.out.println("Melhor Pokémon escolhido automaticamente: " + melhorPokemon.getNome() +
                             " (Força: " + melhorPokemon.getForca() + ", Nível: " + melhorPokemon.getNivel() + ")");
            return true;
        }
        
        System.out.println("Nenhum Pokémon disponível encontrado!");
        return false;
    }

    // Calcula pontuação de um Pokémon para determinar o melhor
    private int calcularPontuacaoPokemon(Pokemon pokemon) {
        if (pokemon.estaNocauteado()) return -1;
        
        // Pontuação baseada em: nível, força e energia atual
        int pontuacao = pokemon.getNivel() * 10 + 
                       pokemon.getForca() * 2 + 
                       (pokemon.getEnergia() / 10);
        return pontuacao;
    }

    // Verifica se tem Pokémons disponíveis para batalha
    public boolean temPokemonDisponivel() {
        // Verifica time ativo
        for (Pokemon p : time) {
            if (!p.estaNocauteado()) {
                return true;
            }
        }
        
        // Verifica mochila
        for (Pokemon p : mochila.getPokemonsCapturados()) {
            if (!p.estaNocauteado()) {
                return true;
            }
        }
        
        return false;
    }

    // Conta quantos Pokémons estão disponíveis
    public int contarPokemonsDisponiveis() {
        int contador = 0;
        
        for (Pokemon p : time) {
            if (!p.estaNocauteado()) contador++;
        }
        
        for (Pokemon p : mochila.getPokemonsCapturados()) {
            if (!p.estaNocauteado()) contador++;
        }
        
        return contador;
    }

    // Adiciona Pokémon diretamente ao time (se houver espaço)
    public boolean adicionarAoTime(Pokemon pokemon) {
        if (this.time == null) {
            this.time = new ArrayList<>();
        }
        
        if (time.size() >= 6) {
            System.out.println("Time cheio! Use a mochila para armazenar mais Pokémons.");
            return false;
        }
        
        pokemon.setTreinador(this);
        this.time.add(pokemon);
        
        if (pokemonAtual == null) {
            pokemonAtual = pokemon;
        }
        
        System.out.println(this.nome + " adicionou " + pokemon.getNome() + " ao time ativo!");
        return true;
    }

    // Métodos para interagir com a mochila
    public void abrirMochila() {
        mochila.listarPokemons();
    }

    public boolean moverParaMochila(int indiceTime) {
        return mochila.transferirDoTime(indiceTime);
    }

    public boolean moverParaTime(int indiceMochila) {
        return mochila.transferirParaTime(indiceMochila);
    }

    public void organizarMochilaPorTipo() {
        mochila.organizarPorTipo();
    }

    public void organizarMochilaPorNivel() {
        mochila.organizarPorNivel();
    }

    public void organizarMochilaPorForca() {
        mochila.organizarPorForca();
    }

    // Lista o time ativo
    public void mostrarTime() {
        if (time.isEmpty()) {
            System.out.println(nome + " não tem Pokémons no time ativo!");
            return;
        }
        
        System.out.println("=== TIME ATIVO DE " + nome.toUpperCase() + " ===");
        for (int i = 0; i < time.size(); i++) {
            Pokemon p = time.get(i);
            String status = (p == pokemonAtual) ? " [ATUAL]" : "";
            System.out.println((i + 1) + ". " + p.getNome() + 
                             " - Tipo: " + p.getTipo() + 
                             " - HP: " + p.getEnergia() + "/" + p.getEnergiaMaxima() +
                             " - Nível: " + p.getNivel() + status);
        }
    }

    // Status completo do treinador
    public void mostrarStatusCompleto() {
        System.out.println("=== STATUS COMPLETO ===");
        System.out.println("Treinador: " + nome);
        System.out.println("Pontuação: " + pontuacao + " pts");
        System.out.println("Pokémon atual: " + (pokemonAtual != null ? 
            pokemonAtual.getNome() + " (HP: " + pokemonAtual.getEnergia() + ")" : "Nenhum"));
        System.out.println("Time ativo: " + time.size() + "/6");
        System.out.println(mochila.getStatus());
        System.out.println("Total de Pokémons: " + (time.size() + mochila.getQuantidade()));
    }

    // Busca um Pokémon em todo o inventário (time + mochila)
    public Pokemon buscarPokemon(String nome) {
        // Busca no time ativo
        for (Pokemon p : time) {
            if (p.getNome().equalsIgnoreCase(nome)) {
                return p;
            }
        }
        
        // Busca na mochila
        return mochila.buscarPokemon(nome);
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public List<Pokemon> getTime() {
        return time;
    }

    public Mochila getMochila() {
        return mochila;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public Pokemon getPokemonAtual() {
        return pokemonAtual;
    }

    public void setPokemonAtual(Pokemon pokemonAtual) {
        this.pokemonAtual = pokemonAtual;
    }

    // Status resumido do treinador
    public String getStatus() {
        return nome + ": " + pontuacao + " pts | Pokémon: " + 
               (pokemonAtual != null ? pokemonAtual.getNome() + " (HP: " + pokemonAtual.getEnergia() + ")" : "Nenhum") +
               " | Time: " + time.size() + "/6 | Mochila: " + mochila.getQuantidade() + "/" + mochila.getCapacidadeMaxima();
    }
}