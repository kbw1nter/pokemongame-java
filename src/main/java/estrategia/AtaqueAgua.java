package estrategia;

import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;

public class AtaqueAgua implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        
        // Vantagem contra Fogo
        if (defensor.getTipo().equals("Fogo")) {
            danoBase *= 1.5;
        }
        
        // NOVA FUNCIONALIDADE: Redução de dano para Pokémon Água em ambientes adversos
        if (defensor.getTipo().equals("Agua")) {
            // Verifica se está em ambiente adverso (não é região Água)
            String regiaoAtual = obterRegiaoAtual(defensor);
            if (!regiaoAtual.equals("Agua")) {
                // Reduz 25% do dano recebido em ambientes adversos
                danoBase = (int)(danoBase * 0.75);
                // Mensagem pode ser adicionada aqui se necessário
            }
        }
        
        return danoBase;
    }
    
private String obterRegiaoAtual(Pokemon pokemon) {
        // Esta implementação assume que você tem acesso à posição do Pokémon
        // Você pode modificar este método conforme sua arquitetura
        
        // Por enquanto, vamos assumir que a região pode ser obtida do contexto
        // ou você pode passar como parâmetro adicional
        
        // Placeholder - você deve implementar a lógica real baseada na posição
        return "Floresta"; // Exemplo - substitua pela lógica real
    }
}
