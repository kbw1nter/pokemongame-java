package jogo;

import ui.Observador;
import java.util.ArrayList;
import java.util.List;

public class Observado {
    private final List<Observador> observadores = new ArrayList<>();

    public void adicionarObservador(Observador obs) {
        observadores.add(obs);
    }

    // Tornando o método protegido para que subclasses possam chamá-lo
    protected void notificarObservadores(String evento, Object dados) {
        for (Observador obs : observadores) {
            obs.atualizar(evento, dados);
        }
    }
}