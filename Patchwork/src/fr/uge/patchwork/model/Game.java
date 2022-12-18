package fr.uge.patchwork.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.gameboard.event.EventType;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.model.component.player.HumanPlayer;
import fr.uge.patchwork.model.component.player.Player;
import fr.uge.patchwork.model.component.player.automa.Automa;
import fr.uge.patchwork.model.component.player.automa.AutomaDifficulty;
import fr.uge.patchwork.model.component.player.automa.DeckType;

public record Game(GameMode gameMode, TrackBoard trackBoard, 
    PatchManager patchManager) {
  
  public Game {
    Objects.requireNonNull(gameMode, "The game mode can't be null");
    Objects.requireNonNull(trackBoard, "The track board can't be null");
    Objects.requireNonNull(patchManager, "The patche manager can't be null");
  }
  
  // public void save() ?
  // public void loadFromSave() ?
  
  
  public static Game basic() throws IOException {
    var events = new ArrayList<Event>();
    var players = new HashSet<Player>(List.of(
        new HumanPlayer("Player 1", 5, new QuiltBoard(9,9)),
        new HumanPlayer("Player 2", 5, new QuiltBoard(9,9))));
    var patchesPath = Path.of("resources/settings/basic/patchwork_basic.txt");
    var trackBoard = new TrackBoard(54, players, events);
    var patchManager = new PatchManager(RegularPatch.fromFile(patchesPath));
    return new Game(GameMode.PATCHWORK_BASIC, trackBoard, patchManager);
  }
  
  public static Game full() throws IOException {
    var events = new ArrayList<Event>();
    var players = new HashSet<Player>(List.of(
        new HumanPlayer("Player 1", 5, new QuiltBoard(9,9)),
        new HumanPlayer("Player 2", 5, new QuiltBoard(9,9))));
    var patchesPath = Path.of("resources/settings/full/patchwork_full.txt");
    for(var pos: List.of(5, 11, 17, 23, 29, 35, 41, 47)) {
      events.add(new Event(EventType.BUTTON_INCOME, pos));
    }
    for(var pos: List.of(20, 26, 32, 44, 50)) {
      events.add(new Event(EventType.PATCH_INCOME, pos));
    }
    var trackBoard = new TrackBoard(54, players, events);
    var patchManager = new PatchManager(RegularPatch.fromFile(patchesPath));
    return new Game(GameMode.PATCHWORK_FULL, trackBoard, patchManager);    
  }
  
  public static Game automa(AutomaDifficulty difficulty, DeckType deckType) throws IOException {
    var events = new ArrayList<Event>();
    var players = new HashSet<Player>(List.of(
        new HumanPlayer("Player 1", 5, new QuiltBoard(9,9)),
        new Automa(difficulty, DeckType.fromType(deckType))));
    var patchesPath = Path.of("resources/settings/full/patchwork_full.txt");
    for(var pos: List.of(5, 11, 17, 23, 29, 35, 41, 47)) {
      events.add(new Event(EventType.BUTTON_INCOME, pos));
    }
    for(var pos: List.of(20, 26, 32, 44, 50)) {
      events.add(new Event(EventType.PATCH_INCOME, pos));
    }
    var trackBoard = new TrackBoard(54, players, events);
    var patchManager = new PatchManager(RegularPatch.fromFile(patchesPath));
    return new Game(GameMode.PATCHWORK_AUTOMA, trackBoard, patchManager);
  }
  
}
