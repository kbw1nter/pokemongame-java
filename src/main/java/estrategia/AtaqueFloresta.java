package estrategia;
import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;

public class AtaqueFloresta implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;
    
    // Constantes para regeneração
    private static final double TAXA_REGENERACAO = 0.25; // 25% do dano causado
    private static final int REGENERACAO_MINIMA = 5; // Mínimo de 5 pontos de cura
    
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        Random random = new Random();
        int danoBase = atacante.getForca() + random.nextInt(atacante.getNivel() + 1);
        
        // Vantagem contra Água
        if (defensor.getTipo().equals("Água")) {
            danoBase = (int)(danoBase * 1.5);
        }
        
        // Habilidade de Regeneração - cura parte do dano causado
        if (atacante.getTipo().equals("Floresta")) {
            aplicarRegeneracao(atacante, danoBase);
        }
        
        return danoBase;
    }
    
    private void aplicarRegeneracao(Pokemon atacante, int danoCausado) {
        // Calcula a quantidade de cura baseada no dano causado
        int cura = Math.max(REGENERACAO_MINIMA, (int)(danoCausado * TAXA_REGENERACAO));
        
        // Aplica a cura sem exceder a energia máxima
        int energiaAtual = atacante.getEnergia();
        int energiaMaxima = atacante.getEnergiaMaxima();
        
        // Só regenera se não estiver com energia máxima
        if (energiaAtual < energiaMaxima) {
            int novaEnergia = Math.min(energiaMaxima, energiaAtual + cura);
            
            // MÉTODO MAIS SIMPLES: Define a nova energia diretamente
            // Assuming Pokemon class has a method to set energy directly
            // If not, you can modify the Pokemon class to add this method
            atacante.curarEnergia(cura); // Novo método que precisará ser adicionado ao Pokemon
            
            System.out.println(atacante.getNome() + " se regenerou e recuperou " + 
                             (novaEnergia - energiaAtual) + " pontos de energia!");
        }
    }
}