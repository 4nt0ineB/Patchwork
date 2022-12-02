package fr.uge.patchwork.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import fr.uge.patchwork.model.component.gameboard.GameBoard;
import fr.uge.patchwork.util.xml.XMLParser;

public record Game(GameMode gameMode, GameBoard gameBoard) {
  
  public Game {
    Objects.requireNonNull(gameMode, "The game mode can't be null");
    Objects.requireNonNull(gameBoard, "The game board can't be null");
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
