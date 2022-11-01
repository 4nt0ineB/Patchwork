package controller;

import java.util.LinkedHashSet;
import java.util.List;

import model.Coordinates;
import model.GameBoard;
import model.Patch;
import view.UserInterface;
import view.cli.PatchworkCLI;

public class PatchworkController {
  
  private static void patchwork(UserInterface ui, GameBoard gameBoard) {
    var action = Action.DEFAULT;
    ui.clear();
    ui.drawSplashScreen();
    do {
      ui.draw(gameBoard);
      var options = new LinkedHashSet<Action>();
      if(gameBoard.currentPlayerCanAdvance()) {
        options.add(Action.ADVANCE);
      }
      if(gameBoard.currentPlayerCanChosePatch()) {
        options.add(Action.TAKE_PATCH);
      }
      options.add(Action.QUIT);
      action = ui.getPlayerActionForTurn(gameBoard, options);
      ui.clear();
      switch(action) {
        case TAKE_PATCH -> {
          ui.selectPatch(gameBoard);
          patchworkHandlePatch(ui, gameBoard);
        }
        case ADVANCE -> {
          gameBoard.currentPlayerAdvance();
          
         }
        default -> {}
      }
      Patch earnedPatch = null;
      while((earnedPatch = gameBoard.patchEarned()) != null) {
        patchworkHandlePatch(ui, gameBoard);
      }
      gameBoard.nextTurn();
      ui.clear();
    } while(action != Action.QUIT);
    ui.close();
  }
  
  private static void patchworkHandlePatch(UserInterface ui, GameBoard gameBoard) {
    // A list of actions
    var action = Action.DEFAULT;
    var options = new LinkedHashSet<Action>();
    options.clear();
    options.addAll(List.of(
        Action.UP, 
        Action.DOWN,
        Action.RIGHT,
        Action.LEFT,
        Action.ROTATE_LEFT,
        Action.ROTATE_RIGHT,
        Action.QUIT
        ));
    // We use a dummy quilt to play with the patch
    var quilt = gameBoard.currentPlayer().quilt();
    var patch = gameBoard.selectedPatch();
    patch.absoluteMoveTo(new Coordinates(quilt.width()/2, quilt.height()/2));
    do {
      ui.clear();
      ui.drawDummyQuilt(quilt, patch);
      if(quilt.canAdd(patch) && gameBoard.currentPlayer().canBuyPatch(patch)) {
        options.add(Action.PLACE);
      }else {
        options.remove(Action.PLACE);
      }
      action = ui.getPlayerActionForTurn(gameBoard, options);
      switch(action){
        case UP -> patch.moveUp();
        case DOWN -> patch.moveDown();
        case RIGHT -> patch.moveRight();
        case LEFT -> patch.moveLeft();
        case ROTATE_LEFT -> patch.rotateLeft();
        case ROTATE_RIGHT -> patch.rotateRight();
        case PLACE -> {
          gameBoard.playSelectedPatch();
          action = Action.QUIT;
        }
        default -> {}
      }
    }while(action != Action.QUIT);
  }

  public static void main(String[] args) {
    GameBoard.makeBoard();
    //patchwork(new PatchworkCLI(), GameBoard.makeBoard());
  }
  
}