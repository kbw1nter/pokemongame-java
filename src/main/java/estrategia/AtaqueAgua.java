package estrategia;

import modelo.Pokemon;
import java.util.Random;

public class AtaqueAgua implements IAtaque {
    @Override
    public int calcularDano(Pokemon atacante, Pokemon defensor) {
        // Pokémon de água tem dano reduzido contra tipo Floresta
        double fatorTipo = defensor.getTipo().equals("Floresta") ? 0.5 : 1.0;
        int danoBase = (atacante.getForca() + new Random().nextInt(atacante.getNivel() + 1));
        return (int) (danoBase * fatorTipo);
    }
}
