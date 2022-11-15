package model.gameboard.event;

import java.util.Objects;

import model.Patch;
import model.gameboard.GameBoard;

/**
 * 
 * Provides effect to be triggered by and conditioned event in game
 * 
 */
@FunctionalInterface
public interface Effect {
  
  /**
   * run the effect on a GameBoard
   * 
   * @param gb
   * @return true if effect have been applied, else false
   */
  boolean run(GameBoard gb);
  
  /*
   * Make a new effect, paying the current 
   * player according to the amount of buttons on his quilt
   */
  public static Effect makeButtonIncomeEffect() {
    return (GameBoard board) -> {
      int amount = board.currentPlayer().quilt().buttons();
      if(board.canPay(amount)) {
        board.pay(board.currentPlayer(), amount);  
      }
      return true;
    };
  }

  /**
   * Make a new effect, dropping a patch to the current player
   * @param patch The patch to drop
   * @return
   */
  public static Effect makePatchIncomeEffect(Patch patch) {
    Objects.requireNonNull(patch, "Patch can't be null");
    return (GameBoard gb) -> {
      gb.addPatchToPlay(patch);
      return true;
    };
  }

}