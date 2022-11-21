package model.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import model.game.component.gameboard.GameBoard;
import util.xml.XMLParser;

public class Game {
  
  private final GameMode gameMode;
  private final GameBoard gameBoard;
  
  private Game(GameMode gameMode, GameBoard gameBoard) {
    this.gameMode = Objects.requireNonNull(gameMode, "The game mode can't be null");
    this.gameBoard = Objects.requireNonNull(gameBoard, "The game board can't be null");
  }
  
  public GameMode gameMode() {
    return gameMode;
  }
  
  public GameBoard gameBoard() {
    return gameBoard;
  }
  
  // public void save() ?
  // public void loadFromSave() ?
  
  public static Game fromGameMode(GameMode gameMode) throws IOException {
    Objects.requireNonNull(gameMode, "the game mode can't be null");
    var path = switch(gameMode) {
        case PATCHWORK_BASIC -> Path.of("resources/settings/patchwork_basic.xml");
        case PATCHWORK_FULL -> Path.of("resources/settings/patchwork_full.xml");
        default -> throw new AssertionError("Can't be here");
    };
    var xmlParser = new XMLParser();
    var xmlElement = xmlParser.parse(path);
    var board = GameBoard.fromXML(xmlElement);
    return new Game(gameMode, board);
  }
  
}
