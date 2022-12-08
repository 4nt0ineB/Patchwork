package fr.uge.patchwork.model.component.gameboard.event;

import java.util.Objects;
import java.util.function.Predicate;

import fr.uge.patchwork.model.component.gameboard.GameBoard;
import fr.uge.patchwork.model.component.patch.RegularPatch;

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
  public static Predicate<GameBoard> buttonIncome() {
    return (GameBoard board) -> {
      int amount = board.currentPlayer().quilt().buttons();
      if(amount != 0 && board.canPay(amount)) {
        board.pay(board.currentPlayer(), amount);  
        return true;
      }
      return false;
    };
  }

  /**
   * Make a new effect, dropping a patch to the current player
   * @param patch The patch to drop
   * @return
   */
  public static Predicate<GameBoard> patchIncome(RegularPatch patch) {
    Objects.requireNonNull(patch, "Patch can't be null");
    return (GameBoard board) -> {
      board.addPatchToPlay(patch);
      return true;
    };
  }
  
  /**
   * Make a new effect, drop a special tile if a player have filled a plain 7x7 square
   * @param patch The patch to drop
   * @return
   */
  public static Predicate<GameBoard> specialTile() {
    return (GameBoard board) -> {
      if(board.currentPlayer().quilt().hasFilledSquare(7)) {
        board.currentPlayer().earnSpecialTile();
        return true;
      }
      return false;
    };
  }

}