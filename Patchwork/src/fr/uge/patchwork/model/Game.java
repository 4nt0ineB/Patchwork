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

/**
 * The data of a patchwork game
 */
public record Game(GameMode gameMode, TrackBoard trackBoard, 
    PatchManager patchManager) {
  
  public Game {
    Objects.requireNonNull(gameMode, "The game mode can't be null");
    Objects.requireNonNull(trackBoard, "The track board can't be null");
    Objects.requireNonNull(patchManager, "The patche manager can't be null");
  }
  
  // public void save() ?
  // public void loadFromSave() ?
  
  /**
   * Create a 1vs1 patchwork game with basic set of patch
   * with no events nor special tile
   * @return the game environnement
   * @throws IOException if an error occur while paring file setting
   */
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
  
  /**
   * Create a 1vs1 patchwork game with the full set of patch
   * and events
   * @return the game environnement
   * @throws IOException if an error occur while paring file setting
   */
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
  
  /**
   * Create a 1vs1 patchwork game the with full set of patch and events 
   * and with a player as Automa
   * @param difficulty
   * @param deckType
   * @return the game environnement
   * @throws IOException if an error occur while paring file setting
   */
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
