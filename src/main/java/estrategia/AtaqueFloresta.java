package estrategia;

import java.io.Serializable;
import modelo.Pokemon;
import java.util.Random;

public class AtaqueFloresta implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        // habilidade de regeneração, cura parte do dano ao atacar
        int cura = 5;
        atacante.receberDano(-cura); // Curar é receber dano negativo
        System.out.println(atacante.getNome() + " regenerou " + cura + " de vida.");

        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return danoBase;
    }
}
