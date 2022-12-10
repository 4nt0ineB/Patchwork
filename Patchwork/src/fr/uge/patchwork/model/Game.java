package fr.uge.patchwork.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.gameboard.event.EventType;
import fr.uge.patchwork.model.component.patch.RegularPatch;

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
    var events = new ArrayList<Event>();
    var players = new HashSet<Player>(List.of(
        new Player("Player 1", 5, new QuiltBoard(9,9)),
        new Player("Player 2", 5, new QuiltBoard(9,9))
        ));

    Path patchesPath = null;
    switch(gameMode) {
        case PATCHWORK_BASIC -> {
          patchesPath = Path.of("resources/patchwork/settings/basic/patchwork_basic.txt");
        }
        case PATCHWORK_FULL -> {
          patchesPath = Path.of("resources/patchwork/settings/full/patchwork_full.txt");
          for(var pos: List.of(5, 11, 17, 23, 29, 35, 41, 47)) {
            events.add(new Event(EventType.BUTTON_INCOME, pos));
          }
          for(var pos: List.of(20, 26, 32, 44, 50)) {
            events.add(new Event(EventType.PATCH_INCOME, pos));
          }
        }
        default -> throw new AssertionError("Can't be here");
    };
    
    var trackBoard = new TrackBoard(54, players, events);
    var patchManager = new PatchManager(RegularPatch.fromFile(patchesPath));
    return new Game(gameMode, trackBoard, patchManager);
  }
  
}
