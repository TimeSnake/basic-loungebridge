package de.timesnake.basic.loungebridge.util.game;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GameElementManager {

    private final Set<GameElement> elements = new HashSet<>();

    public GameElementManager() {

    }

    public void start() {
        this.elements.forEach(GameElement::start);
    }

    public void stop() {
        this.elements.forEach(GameElement::stop);
    }

    public void addGameElement(GameElement element) {
        this.elements.add(element);
    }

    public void addGameElement(Collection<GameElement> elements) {
        this.elements.addAll(elements);
    }

    public void removeGameElement(Collection<GameElement> elements) {
        this.elements.removeAll(elements);
    }
}
