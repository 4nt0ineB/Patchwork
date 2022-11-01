package model;

@FunctionalInterface
public interface Effect {
    void run(GameBoard gb);
}