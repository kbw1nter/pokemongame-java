package jogo;

import ui.Observador;
import java.util.ArrayList;
import java.util.List;

public class Observado {
    private final List<Observador> observadores = new ArrayList<>();

    public void adicionarObservador(Observador obs) {
        observadores.add(obs);
    }

    public void notificarObservadores(String evento, Object dados) {
        for (Observador obs : observadores) {
            obs.atualizar(evento, dados);
        }
    }
}
