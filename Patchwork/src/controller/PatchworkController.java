package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Coordinates;
import model.GameBoard;
import model.Patch;
import model.Player;
import model.QuiltBoard;
import view.Action;
import view.UserInterface;
import view.cli.PatchworkCLI;

public class PatchworkController {
  
  private static void patchwork(UserInterface ui, GameBoard gameBoard) {
    var action = Action.DEFAULT;
    ui.clear();
    ui.drawSplashScreen();
    // @Todo split turn-menu/place-patch logic
    do {
      ui.draw(gameBoard);
      var options = new ArrayList<Action>();
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
          var quilt = gameBoard.currentPlayer().quilt().clone();
          var patch = gameBoard.selectedPatch();
          patch.absoluteMoveTo(new Coordinates(quilt.width()/2, quilt.height()/2));
//          System.out.println(patch);
          do {
            ui.clear();
            ui.drawDummyQuilt(quilt, patch);
            action = ui.getPlayerActionForTurn(gameBoard, options);
            switch(action){
              case UP -> patch.moveUp();
              case DOWN -> patch.moveDown();
              case RIGHT -> patch.moveRight();
              case LEFT -> patch.moveLeft();
              case ROTATE_LEFT -> patch.rotateLeft();
              case ROTATE_RIGHT -> patch.rotateRight();
              default -> {}
            }
            System.out.println(action);
//            System.out.println(patch);
          }while(action != Action.QUIT);
//          gameBoard.nextTurn();
          gameBoard.unselectPatch();
          action = Action.DEFAULT;
        }
        case ADVANCE -> {
          gameBoard.currentPlayerAdvance();
          gameBoard.nextTurn();
         }
        default -> {}
      }
      ui.clear();
    } while(action != Action.QUIT);
    ui.close();
  }

  public static void main(String[] args) {
    // Basic version
    // turn this into config files
    var patch1 = new Patch(1, 4, 3
        , List.of(
          new Coordinates(0, 0),
          new Coordinates(0, 1),
          new Coordinates(1, 0),
          new Coordinates(1, 1)
          )
        , new Coordinates(0, 0)
        );
    var patch2 = new Patch(0, 2, 2
        , List.of(
          new Coordinates(0, 0),
          new Coordinates(0, 1),
          new Coordinates(1, 0),
          new Coordinates(1, 1)
          )
        , new Coordinates(0, 0)
        );
    var patches = new ArrayList<Patch>();
    patches.addAll(Collections.nCopies(20, patch1));
    patches.addAll(Collections.nCopies(20, patch2));
    var player1 = new Player("Player 1", 0, 0, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 0, 0, new QuiltBoard(9, 9));
    player2.quilt().add(patch2);
    var gameBoard = new GameBoard(53, patches, List.of(player1, player2));
    //
    patchwork(new PatchworkCLI(), gameBoard);
  }
  
}