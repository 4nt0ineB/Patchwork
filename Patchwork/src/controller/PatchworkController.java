package controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import model.Coordinates;
import model.GameBoard;
import model.Patch;
import view.UserInterface;
import view.cli.PatchworkCLI;

public class PatchworkController {

  private static void patchwork(UserInterface ui, GameBoard board) {
    var action = Action.DEFAULT;
    var patchActions = new LinkedHashSet<Action>();
    patchActions.addAll(List.of(Action.UP, Action.DOWN, Action.RIGHT, Action.LEFT, Action.ROTATE_LEFT,
        Action.ROTATE_RIGHT, Action.QUIT));
    ui.clear();
    ui.drawSplashScreen();
    // -- Game loop
    do {
      ui.draw(board);
      var options = new LinkedHashSet<Action>();
      if (board.currentPlayerCanAdvance()) {
        options.add(Action.ADVANCE);
      }
      if (board.currentPlayerCanChosePatch()) {
        options.add(Action.TAKE_PATCH);
      }
      options.add(Action.QUIT);
      action = ui.getPlayerActionForTurn(board, options);
      ui.clear();
      switch (action) {
        case TAKE_PATCH -> {
          ui.selectPatch(board);
          patchActions.add(Action.QUIT); // The player can stop dealing with this patch
          if (manipulatePatch(ui, board, patchActions, board.selectedPatch()).equals(Action.PLACE)) {
            if (board.playSelectedPatch()) {
              board.nextTurn();
            }
          }
        }
        case ADVANCE -> {
          board.currentPlayerAdvance();
          board.nextTurn();
        }
      default -> {
      }
      }
      // -- Earned Patches
      Patch earnedPatch = null;
      patchActions.remove(Action.QUIT); // The player can't avoid placing given patches
      while ((earnedPatch = board.patchEarned()) != null) {
        manipulatePatch(ui, board, patchActions, earnedPatch);
        board.placeEarnedPatch();
      }
      board.nextTurn();
      ui.clear();
    } while (action != Action.QUIT);
    ui.close();
  }

  private static Action manipulatePatch(UserInterface ui, GameBoard gameBoard, Set<Action> actions, Patch patch) {
    // A list of actions
    var action = Action.DEFAULT;
    // We use a dummy quilt to play with the patch
    var quilt = gameBoard.currentPlayer().quilt();
    patch.absoluteMoveTo(new Coordinates(quilt.width() / 2, quilt.height() / 2));
    do {
      ui.clear();
      ui.drawDummyQuilt(quilt, patch);
      if (quilt.canAdd(patch) && gameBoard.currentPlayer().canBuyPatch(patch)) {
        actions.add(Action.PLACE);
      } else {
        actions.remove(Action.PLACE);
      }
      action = ui.getPlayerActionForTurn(gameBoard, actions);
      switch (action) {
        case UP -> patch.moveUp();
        case DOWN -> patch.moveDown();
        case RIGHT -> patch.moveRight();
        case LEFT -> patch.moveLeft();
        case ROTATE_LEFT -> patch.rotateLeft();
        case ROTATE_RIGHT -> patch.rotateRight();
        case PLACE -> {
          return Action.PLACE;
        }
        default -> {}
      }
    } while (action != Action.QUIT);
    return action;
  }

  public static void main(String[] args) {
    patchwork(new PatchworkCLI(), GameBoard.makeBoard());
  }

}