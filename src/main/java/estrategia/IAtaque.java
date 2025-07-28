package estrategia;

import modelo.Pokemon;

//interface pra definir a estrategia de ataque
public interface IAtaque {
    int calcularDano(Pokemon atacante, Pokemon defensor);
}
