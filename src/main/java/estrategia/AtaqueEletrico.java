package estrategia;

import modelo.Pokemon;
import java.util.Random;

public class AtaqueEletrico implements IAtaque {
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return danoBase;
    }
}
