package model.gameboard;

@FunctionalInterface
public interface Effect {
  /**
   * run the effect on a GameBoard
   * 
   * @param gb
   * @return true if effect have been applied, else false
   */
  boolean run(GameBoard gb);
}