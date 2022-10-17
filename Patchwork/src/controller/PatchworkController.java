package controller;

import java.util.List;

import model.Coordinates;
import model.Patch;
import model.QuiltBoard;
import view.Action;
import view.UserInterface;

public class PatchworkController {
  
  private static void patchwork(UserInterface ui) {
    var action = Action.QUIT;
    do {
      ui.draw();
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
    // patchwork(new PatchworkCLI());
    var pCoordinates = List.of(
        new Coordinates(-1, 0),
        new Coordinates(0, 0),
        new Coordinates(-1, 1),
        new Coordinates(1, 0)
    );
    var patch = new Patch(2, 6, 4, pCoordinates, new Coordinates(1,1));
    var quilt = new QuiltBoard(6, 7);
    System.out.println(patch.absolutePositions());
    System.out.println(quilt.addPatch(patch));
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