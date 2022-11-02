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
    // turn this into config files ?
    var patches = new ArrayList<Patch>();
    var squaredShape = List.of(new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 0),
        new Coordinates(1, 1));
    for (var i = 0; i < 20; i++) {
      patches.add(new Patch(1, 4, 3, squaredShape));
      patches.add(new Patch(0, 2, 2, squaredShape));
    }
    var player1 = new Player("Player 1", 5, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 5, new QuiltBoard(9, 9));

    var events = new ArrayList<Event>();
    events.add(new PositionedEvent(EventType.PATCH_INCOME, 2, true, (GameBoard gb) -> {
      gb.patchesToBePlayed.add(new Patch(0, 0, 0, List.of(new Coordinates(0, 0))));
      return true;
    }));
    var gameBoard = new FullGameBoard(3, 53, 152, patches, List.of(player1, player2), events);
    return gameBoard;
  }

}
