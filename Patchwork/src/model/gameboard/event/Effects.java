package model.gameboard.event;

import java.util.Objects;

import java.util.function.Function;

import model.Patch;
import model.gameboard.GameBoard;

/**
 * 
 * Provides effects to be triggered by an event in game
 * 
 */

public final class Effects {
  
  /*
   * Make a new effect, paying the current 
   * player according to the amount of buttons on his quilt
   */
  public static Function<GameBoard, Boolean> buttonIncome() {
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
  public static Function<GameBoard, Boolean> patchIncome(Patch patch) {
    Objects.requireNonNull(patch, "Patch can't be null");
    return (GameBoard gb) -> {
      gb.addPatchToPlay(patch);
      return true;
    };
  }

}