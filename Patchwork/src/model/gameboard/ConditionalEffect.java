package model.gameboard;

import java.util.Objects;

import model.Patch;

@FunctionalInterface
public interface ConditionalEffect {
  /**
   * run the effect on a GameBoard
   * 
   * @param gb
   * @return true if effect have been applied, else false
   */
  boolean run(GameBoard gb);
  
  public static ConditionalEffect makeButtonIncomeEffect() {
    return (GameBoard gb) -> {
      gb.pay(gb.currentPlayer(), gb.currentPlayer().quilt().buttons());
      return true;
    };
  }

  public static ConditionalEffect makePatchIncomeEffect(Patch patch) {
    Objects.requireNonNull(patch, "Patch can't be null");
    return (GameBoard gb) -> {
      gb.addPatchToPlay(patch);
      return true;
    };
  }
}