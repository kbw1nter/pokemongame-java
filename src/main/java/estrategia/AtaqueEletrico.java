package estrategia;

import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class AtaqueEletrico implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;
    
    // Constantes para paralisia
    private static final double CHANCE_PARALISIA = 0.3; // 30% de chance
    private static final int DURACAO_PARALISIA = 1; // 1 rodada
    
    // Map estático para rastrear Pokémon paralisados
    private static Map<Pokemon, Integer> pokemonParalisados = new HashMap<>();
    
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        
        // Vantagem contra Água
        if (defensor.getTipo().equals("Água")) {
            danoBase *= 1.5;
        }
        
        // Habilidade especial: chance de paralisia
        if (atacante.getTipo().equals("Elétrico")) {
            tentarParalisar(defensor);
        }
        
        return danoBase;
    }
    
    private void tentarParalisar(Pokemon defensor) {
        Random random = new Random();
        
        // Verifica se o defensor já está paralisado
        if (estaParalisado(defensor)) {
            return; // Não pode paralisar quem já está paralisado
        }
        
        // Tenta aplicar paralisia
        if (random.nextDouble() < CHANCE_PARALISIA) {
            pokemonParalisados.put(defensor, DURACAO_PARALISIA);
            System.out.println("⚡ " + defensor.getNome() + " foi PARALISADO e não poderá atacar na próxima rodada!");
        }
    }
    
    // Método estático para verificar se um Pokémon está paralisado
    public static boolean estaParalisado(Pokemon pokemon) {
        return pokemonParalisados.containsKey(pokemon) && pokemonParalisados.get(pokemon) > 0;
    }
    
    // Método estático para decrementar paralisia (deve ser chamado a cada turno)
    public static void processarTurno() {
        pokemonParalisados.entrySet().removeIf(entry -> {
            Pokemon pokemon = entry.getKey();
            int duracao = entry.getValue() - 1;
            
            if (duracao <= 0) {
                System.out.println("✨ " + pokemon.getNome() + " se recuperou da paralisia!");
                return true; // Remove da lista
            } else {
                entry.setValue(duracao);
                return false; // Mantém na lista
            }
        });
    }
    
    // Método estático para verificar se um Pokémon pode atacar
    public static boolean podeAtacar(Pokemon pokemon) {
        if (estaParalisado(pokemon)) {
            System.out.println("⚡ " + pokemon.getNome() + " está paralisado e não pode atacar neste turno!");
            return false;
        }
        return true;
    }
    
    // Método estático para limpar paralisia (útil para novos combates)
    public static void limparParalisia() {
        pokemonParalisados.clear();
    }
    
    // Método estático para remover paralisia de um Pokémon específico
    public static void removerParalisia(Pokemon pokemon) {
        pokemonParalisados.remove(pokemon);
    }
}