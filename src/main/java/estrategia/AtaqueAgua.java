package estrategia;

import modelo.Pokemon;
import java.io.Serializable;
import java.util.Random;

public class AtaqueAgua implements IAtaque, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        // pokmon de água tem dano reduzido contra tipo Floresta
        double fatorTipo = defensor.getTipo().equals("Floresta") ? 0.5 : 1.0;
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return (int) (danoBase * fatorTipo);
    }
}
