package estrategia;

import java.io.Serializable;
import modelo.Pokemon;
import java.util.Random;

public class AtaqueTerra implements IAtaque {
    private final long serialVersionUID = 1L;

    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        // por enquanto ataque padrão
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return danoBase;
    }
}
