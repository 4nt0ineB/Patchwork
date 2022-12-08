package fr.uge.patchwork.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.util.xml.XMLParser;

public record Game(GameMode gameMode, TrackBoard trackBoard, 
    PatchManager patchManager) {
  
  public Game {
    Objects.requireNonNull(gameMode, "The game mode can't be null");
    Objects.requireNonNull(trackBoard, "The track board can't be null");
    Objects.requireNonNull(patchManager, "The patche manager can't be null");
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
    var board = TrackBoard.fromXML(xmlElement);
    return new Game(gameMode, board, null);
  }
  
}
