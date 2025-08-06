package estrategia;

import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;

public class AtaqueTerra implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return danoBase;
    }
}
