package controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import model.Action;
import model.Coordinates;
import model.Patch;
import model.gameboard.FullGameBoardFactory;
import model.gameboard.GameBoard;
import model.gameboard.GameBoardFactory;
import view.UserInterface;
import view.cli.PatchworkCLI;

public class PatchworkController {

  private static void patchwork(UserInterface ui, GameBoard board) {
    var action = Action.DEFAULT;
    var patchActions = new LinkedHashSet<Action>();
    patchActions.addAll(List.of(Action.UP, Action.DOWN, Action.RIGHT, Action.LEFT, Action.ROTATE_LEFT,
        Action.ROTATE_RIGHT, Action.QUIT));
    ui.clear();
    // -- Game loop
    do {
      board.nextTurn();
      ui.drawSplashScreen();
      ui.draw(board);
      ui.display();
      action = doActionForTurn(ui, board);
      // The player has patches to place
      while (board.nextPatchToPlay() != null) {
        switch(manipulatePatch(ui, board, patchActions, board.nextPatchToPlay())){
          case QUIT -> { // Abandon this patch
            board.unselectPatch();
          }
          case PLACE -> { // Add the patch to the quilt
            board.playNextPatch();
          }
          default -> {}
        }
      }
      ui.displayEvents(board.eventQueue());
      ui.clear();
    } while (action != Action.QUIT && !board.isFinished());
    ui.close();
  }
  
  private static Action doActionForTurn(UserInterface ui, GameBoard board) {
    var action = Action.DEFAULT;
    var options = new LinkedHashSet<Action>();
    if (board.currentPlayerCanAdvance()) {
      options.add(Action.ADVANCE);
    }
    if (board.currentPlayerCanSelectPatch()) {
      options.add(Action.SELECT_PATCH);
    }
    if(options.isEmpty()) {
      return action;
    }
    options.add(Action.QUIT);
    action = ui.getPlayerActionForTurn(board, options);
    ui.clear();
    ui.drawSplashScreen();
    switch (action) {
      case SELECT_PATCH -> ui.selectPatch(board);
      case ADVANCE -> board.currentPlayerAdvance();
      case QUIT -> { return Action.QUIT; }
      default -> {}
    }
    return action;
  }
  
  private static Action manipulatePatch(UserInterface ui, GameBoard board, Set<Action> actions, Patch patch) {
    // A list of actions
    var action = Action.DEFAULT;
    // We use a dummy quilt to play with the patch
    var quilt = board.currentPlayer().quilt();
    patch.absoluteMoveTo(new Coordinates(quilt.width() / 2, quilt.height() / 2));
    do {
      ui.clear();
      ui.drawSplashScreen();
      ui.drawDummyQuilt(quilt, patch);
      ui.display();
      if (quilt.canAdd(patch) && board.currentPlayer().canBuyPatch(patch)) {
        actions.add(Action.PLACE);
      } else {
        actions.remove(Action.PLACE);
      }
      action = ui.getPlayerActionForTurn(board, actions);
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
    GameBoardFactory gameBoardFactory = new FullGameBoardFactory();
    // GameBoardFactory gameBoardFactory = new FullGameBoardFactory();
    patchwork(new PatchworkCLI(), gameBoardFactory.makeBoard());
  }

}