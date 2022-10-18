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
    do {
      ui.draw(gameBoard);
      action = ui.getPlayerActionForTurn();
      switch(action) {
        case PICK_PATCH -> {
         System.out.println("Pick a patch !");
        }
        default -> {}
      }
      ui.clear();
    }while(action != Action.QUIT);
    ui.close();
  }

  public static void main(String[] args) {
    // Basic version
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
    var gameBoard = new GameBoard(53, patches, List.of(player1, player2));
    //
    patchwork(new PatchworkCLI(), gameBoard);
  }

}





//var pCoordinates = List.of(
//new Coordinates(-1, 0),
//new Coordinates(0, 0),
//new Coordinates(-1, 1),
//new Coordinates(1, 0)
//);
//var patch = new Patch(2, 6, 4, pCoordinates, new Coordinates(0,0));
//var p2Coordinates = List.of(
//new Coordinates(0, -1),
//new Coordinates(0, 0),
//new Coordinates(0, 1),
//new Coordinates(1, 1)
//);
//var patch2 = new Patch(2, 6, 4, p2Coordinates, new Coordinates(0, 0));
//System.out.println(patch);
//System.out.println(patch2);
//System.out.println(patch.equals(patch2));