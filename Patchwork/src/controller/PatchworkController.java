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
    var action = Action.QUIT;
    ui.init();
    ui.drawSplashScreen();
    // @Todo split turn-menu/place-patch logic
    do {
      ui.draw(gameBoard);
      action = ui.getPlayerActionForTurn(gameBoard);
      switch(action) {
        case TAKE_PATCH -> {
          var patches = gameBoard.nextPatches();
          var patchIndex = ui.letPlayerSelectPatch(patches);
          ui.tryAndBuyPatch(gameBoard);
          // chose patch
//        ui.letPlayerTryPatch(gameBoard);
          gameBoard.nextTurn();
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