package model.gameboard;

import java.util.ArrayList;
import java.util.List;

import model.Coordinates;
import model.Patch;
import model.Player;
import model.QuiltBoard;

public class BasicGameBoardFactory implements GameBoardFactory {
  @Override
  public GameBoard makeBoard() {
    // turn this into config files ?
    var patches = new ArrayList<Patch>();
    var squaredShape = List.of(
        new Coordinates(0, 0), 
        new Coordinates(0, 1), 
        new Coordinates(1, 0), 
        new Coordinates(1, 1));
    for (var i = 0; i < 20; i++) {
      patches.add(new Patch(3, 4, 1, squaredShape));
      patches.add(new Patch(2, 2, 0, squaredShape));
    }
    var player1 = new Player("Player 1", 5, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 5, new QuiltBoard(9, 9));
    var gameBoard = new GameBoard(3, 53, 152, patches, List.of(player1, player2));
    return gameBoard;
  }
}
