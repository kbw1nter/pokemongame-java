package estrategia;

import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;

public class AtaqueTerra implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;
    
    // Contador estático para rastrear turnos globais
    private static int contadorTurno = 1;
    
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        
        // Vantagem contra Elétrico
        if (defensor.getTipo().equals("Elétrico")) {
            danoBase *= 1.5;
        }
        
        // Habilidade especial: força dobrada em turno ímpar
        if (atacante.getTipo().equals("Terra")) {
            if (isTurnoImpar()) {
                danoBase *= 2;
                System.out.println(atacante.getNome() + " aproveitou o turno ímpar e dobrou sua força!");
            }
        }
        
        // Incrementa o contador de turno após o ataque
        incrementarTurno();
        
        return danoBase;
    }
    
    private boolean isTurnoImpar() {
        return contadorTurno % 2 == 1;
    }
    
    private void incrementarTurno() {
        contadorTurno++;
    }
    
    // Método estático para resetar turnos (útil para novos combates)
    public static void resetarTurnos() {
        contadorTurno = 1;
    }
    
    // Método estático para obter turno atual (útil para debug/interface)
    public static int getTurnoAtual() {
        return contadorTurno;
    }
}