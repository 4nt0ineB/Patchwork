package model.gameboard;

import java.util.ArrayList;
import java.util.List;

import model.Coordinates;
import model.Patch;
import model.Player;
import model.QuiltBoard;
import model.event.Event;
import model.event.EventType;
import model.event.PositionedEvent;

public class FullGameBoardFactory implements GameBoardFactory {

  @Override
  public GameBoard makeBoard() {
    // turn this into config files... very ugly
    var patches = List.of(
        new Patch(2, 2, 0, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(0,1))),
        new Patch(2, 3, 0, List.of(new Coordinates(-1,-1),  new Coordinates(-1,0), new Coordinates(-1,1), new Coordinates(0,0), new Coordinates(1,-1),new Coordinates(1,0), new Coordinates(1,1))),
        new Patch(2, 1, 0, List.of(new Coordinates(-1,0),   new Coordinates(0,-1),  new Coordinates(0,0),  new Coordinates(1,0), new Coordinates(1,1), new Coordinates(2,0))),
        new Patch(2, 2, 0, List.of(new Coordinates(-1,-1),  new Coordinates(-1,0), new Coordinates(0,-1), new Coordinates(0,0), new Coordinates(1,0))),
        new Patch(1, 2, 0, List.of(new Coordinates(-1,0),   new Coordinates(-1,1), new Coordinates(0,0),  new Coordinates(1,0), new Coordinates(1,1))),
        new Patch(2, 1, 0, List.of(new Coordinates(0,-1),   new Coordinates(0,0))),
        new Patch(1, 2, 0, List.of(new Coordinates(-1,0),   new Coordinates(-1,1), new Coordinates(0,0),  new Coordinates(1,0),  new Coordinates(2,0), new Coordinates(2,-1))),
        new Patch(4, 2, 0, List.of(new Coordinates(0,-2),   new Coordinates(0,-1), new Coordinates(0,0),  new Coordinates(1,-1), new Coordinates(1,0), new Coordinates(1,1))),
        new Patch(2, 2, 0, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(1,0))),
        new Patch(3, 1, 0, List.of(new Coordinates(0,-1),   new Coordinates(-1,0), new Coordinates(0,0))),
        new Patch(3, 4, 0, List.of(new Coordinates(-2,0),   new Coordinates(-1,0), new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(1,0))),
        new Patch(0, 3, 1, List.of(new Coordinates(0,-2),   new Coordinates(0,-1), new Coordinates(0,0),  new Coordinates(-1,0), new Coordinates(1,0), new Coordinates(0,1))),
        new Patch(1, 4, 1, List.of(new Coordinates(0,-2),   new Coordinates(0,-1), new Coordinates(0,0),  new Coordinates(-1,0), new Coordinates(1,0), new Coordinates(0,1), new Coordinates(0,2))),
        new Patch(3, 2, 1, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(-1,0), new Coordinates(1,-1))),
        new Patch(5, 3, 1, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(-1,0), new Coordinates(0,1),  new Coordinates(1,-1),new Coordinates(1,0), new Coordinates(2,0), new Coordinates(1,1))),
        new Patch(4, 2, 1, List.of(new Coordinates(-1,0),   new Coordinates(0,0),  new Coordinates(1,0),  new Coordinates(1,1))),
        new Patch(1, 5, 1, List.of(new Coordinates(-1,-1),  new Coordinates(0,-1), new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(0,2), new Coordinates(-1,2))),
        new Patch(7, 1, 1, List.of(new Coordinates(0,-2),   new Coordinates(-0,-1), new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(0,2))),
        new Patch(10, 3, 2,List.of(new Coordinates(-2,0),   new Coordinates(-1,0), new Coordinates(0,0),  new Coordinates(1,0),  new Coordinates(1,1))),
        new Patch(7, 2, 2, List.of(new Coordinates(-1,-1),  new Coordinates(0,-1), new Coordinates(1,-1), new Coordinates(0,0),  new Coordinates(0,1), new Coordinates(0,2))),
        new Patch(5, 4, 2, List.of(new Coordinates(0,-1),   new Coordinates(-1,0), new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(1,0))),
        new Patch(2, 3, 1, List.of(new Coordinates(0,0),    new Coordinates(1,-1), new Coordinates(1,0),  new Coordinates(0,0),  new Coordinates(0,1), new Coordinates(0,2))),
        new Patch(5, 5, 2, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(-1,1), new Coordinates(0,1),  new Coordinates(1,1))),
        new Patch(3, 6, 2, List.of(new Coordinates(-1,-1),  new Coordinates(-1,0), new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(1,0), new Coordinates(1,-1))),
        new Patch(10, 5, 3,List.of(new Coordinates(-1,-1),  new Coordinates(-1,0), new Coordinates(0,0),  new Coordinates(0,-1), new Coordinates(1,0), new Coordinates(2,0))),
        new Patch(7, 4, 2, List.of(new Coordinates(-1,0),   new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(1,1),  new Coordinates(1,0), new Coordinates(2,0))),
        new Patch(8, 6, 3, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(1,-1), new Coordinates(1,0),  new Coordinates(0,1), new Coordinates(-1,1))),
        new Patch(6, 5, 2, List.of(new Coordinates(-1,-1),  new Coordinates(-1,0),  new Coordinates(0,0),  new Coordinates(0,-1))),
        new Patch(10, 4, 3,List.of(new Coordinates(0,0),    new Coordinates(1,-1), new Coordinates(1,0),  new Coordinates(0,1),  new Coordinates(-1,1))),
        new Patch(7, 6, 3, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(1,0),  new Coordinates(1,1))),
        new Patch(4, 6, 2, List.of(new Coordinates(0,-1),   new Coordinates(0,0),  new Coordinates(0,1),  new Coordinates(-1,1))));
    var player1 = new Player("Player 1", 5, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 5, new QuiltBoard(9, 9));
    var events = new ArrayList<Event>();
    events.add(new PositionedEvent(EventType.PATCH_INCOME, 2, true, (GameBoard gb) -> {
      gb.patchesToBePlayed.add(new Patch(0, 0, 0, List.of(new Coordinates(0, 0))));
      return true;
    }));
    var gameBoard = new FullGameBoard(3, 54, 152, patches, List.of(player1, player2), events);
    return gameBoard;
  }

}
