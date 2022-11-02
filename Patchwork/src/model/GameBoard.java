package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import model.event.Event;
import model.event.EventPool;
import model.event.PositionedEvent;
import view.cli.Color;
import view.cli.DisplayableOnCLI;

public class GameBoard implements DisplayableOnCLI {

  // Max number of patches the player can choose from
  private final int nextPatches;
  // Number of squares on the board
  private final int spaces;
  // The bank
  private int buttons;
  // The patches
  private final Queue<Patch> earnedPatches = new LinkedList<>();
  // Current player is always at the top of the stack
  private int currentPlayerPos;
  // Players indexed by position
  private final SortedMap<Integer, Stack<Player>> players = new TreeMap<>();
  //
  private final EventPool eventPool = new EventPool();
  private final NeutralToken neutralToken;

  /**
   * GameBoard constructor
   * 
   * @param nextPatches max number of patches available each turn for the current player
   * @param spaces the number of spaces on the board
   * @param buttons the number of buttons in the bank
   * @param patches the patches around the board at the beginning
   * @param players the players
   * @param the list of events for the board
   */
  private GameBoard(int nextPatches, int spaces, int buttons, List<Patch> patches, List<Player> players,
      ArrayList<Event> events) {
    Objects.requireNonNull(patches, "List of patches can't be null");
    Objects.requireNonNull(players, "List of players can't be null");
    if (patches.size() < 1) {
      throw new IllegalArgumentException("There must be at least 1 patches");
    }
    if (players.size() < 2) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    if (nextPatches < 1) {
      throw new IllegalArgumentException("The number of available patches during a turn can't be lower than 1");
    }
    if (spaces < 1) {
      throw new IllegalArgumentException("The number of spaces on the board can't be lower than 1");
    }
    this.buttons = buttons;
    this.players.put(0, new Stack<Player>());
    this.players.get(0).addAll(players);
    this.nextPatches = nextPatches;
    this.spaces = spaces - 1;
    this.eventPool.addAll(events);
    this.neutralToken = new NeutralToken(nextPatches, patches);
  }

  private void init() {
    Collections.shuffle(this.players.get(0));
    currentPlayerPos = 0;
    // delegate
    this.players.get(0).stream().forEach(p -> p.buttonIncome(getButtons(5)));
    // @Todo check min square size to fit each patch ?
  }

  /**
   * Make and initialize a new basic Patchwork game board
   * 
   * @return
   */
  public static GameBoard makeBoard() {
    // Basic version
    // turn this into config files ?
    var patches = new ArrayList<Patch>();
    var squaredShape = List.of(
        new Coordinates(0, 0), 
        new Coordinates(0, 1), 
        new Coordinates(1, 0), 
        new Coordinates(1, 1));
    for (var i = 0; i < 20; i++) {
      patches.add(new Patch(1, 4, 3, squaredShape));
      patches.add(new Patch(0, 2, 2, squaredShape));
    }
    var player1 = new Player("Player 1", 0, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 0, new QuiltBoard(9, 9));

    var events = new ArrayList<Event>();
//    events.add(new PositionedEvent(2, true, (GameBoard gb) -> {
//      gb.earnedPatches.add(new Patch(0, 0, 0, List.of(new Coordinates(0, 0))));
//      return true;
//    }));
    var gameBoard = new GameBoard(3, 53, 162, patches, List.of(player1, player2), events);
    gameBoard.init();
    return gameBoard;
  }

  /**
   * Return the first earned patch from the list of earned patches
   * 
   * @return
   */
  public Patch patchEarned() {
    return earnedPatches.peek();
  }

  public void placeEarnedPatch() {
    currentPlayerPlayPatch(earnedPatches.poll());
  }

  public Player currentPlayer() {
    return players.get(currentPlayerPos).peek();
  }
  
  /**
   * Return a list of only purchasable patches by the current player
   * @return
   */
  public List<Patch> availablePatches() {
    return neutralToken.availablePatches().stream().filter(patch -> currentPlayer().canBuyPatch(patch)).toList();
  }
  
  /**
   * Select a patch among the next available
   * 
   * @param patch
   */
  public void selectPatch(Patch patch) {
    neutralToken.select(patch);
  }

  public Patch selectedPatch() {
    return neutralToken.selected();
  }

  public void unselectPatch() {
    neutralToken.unselect();
  }

  public boolean playSelectedPatch() {
    if (!currentPlayerPlayPatch(selectedPatch())) {
      return false;
    }
    neutralToken.extractSelected();
    return true;
  }

  public boolean currentPlayerPlayPatch(Patch patch) {
    if (!currentPlayer().buyAndPlacePatch(patch)) {
      return false;
    }
    // Moves
    currentPlayerMove(currentPlayerPos + patch.moves());
    // Buttons
    buttons += patch.price();
    return true;
  }

  /**
   * Subtract amount of buttons from the game board buttons. if it goes below 0,
   * return the maximum.
   * 
   * @param
   * @return the maximum amount possible for the amount asked
   */
  private int getButtons(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Can't get " + amount + " buttons");
    }
    var max = buttons - amount < 0 ? buttons : amount;
    buttons -= max;
    return max;
  }

  /**
   * Advance the current player
   * to the space in front of the next player.
   * This action lead to button income proportional of number of crossed spaces.
   */
  public void currentPlayerAdvance() {
    if (!currentPlayerCanAdvance()) {
      return;
    }
    int newPosition = nextPositionWithPlayers(currentPlayerPos + 1) + 1;
    int buttonIncome = newPosition - currentPlayerPos;
    currentPlayerMove(newPosition);
    currentPlayer().buttonIncome(buttonIncome);
  }

  /**
   * Move the current player to the given position. The move will be limited by
   * boundaries [0, spaces]
   * 
   * @param newPosition
   */
  private void currentPlayerMove(int newPosition) {
    if (newPosition - currentPlayerPos > 0) {
      newPosition = Math.min(spaces, newPosition);
    } else {
      newPosition = Math.min(0, newPosition);
    }
    var player = players.get(currentPlayerPos).pop();
    var playersAtPos = players.get(newPosition);
    if (playersAtPos == null) {
      players.put(newPosition, new Stack<>());
    }
    players.get(newPosition).add(player);
    // Check if events on path, only when moves forward !
    if (newPosition - currentPlayerPos > 0) {
      for (var event : eventPool.positionedBetween(currentPlayerPos + 1, newPosition)) {
        event.run(this);
      }
    }
    currentPlayerPos = newPosition;
  }

  /**
   * Test if the current player can advance on the board The player can advance if
   * he has not reached the end and if a player is ahead of him
   * 
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return currentPlayerPos != spaces && nextPositionWithPlayers(currentPlayerPos + 1) != -1;
  }

  /**
   * Test if the current player can choose a patch()
   * 
   * @return
   */
  public boolean currentPlayerCanChosePatch() {
    if (neutralToken.availablePatches().isEmpty()) {
      return false;
    }
    return neutralToken.availablePatches().stream().anyMatch(patch -> currentPlayer().canBuyPatch(patch) == true);
  }

  /**
   * Set the next currentPlayer
   * @exception AssertionError if the player is stuck.
   * @return
   */
  public void nextTurn() {
    if (!earnedPatches.isEmpty()) {
      return;
    }
    currentPlayerPos = nextPositionWithPlayers(0); // the most behind player
    earnedPatches.clear();
    if (!currentPlayerCanAdvance() && !currentPlayerCanChosePatch()) {
      throw new AssertionError(
          "Unwanted game state. Player is stuck. " + "Probably bad init settings for the game board");
    }
  }
  
  /**
   * Run events that are not triggered by moves (positioned events)
   */
  public void endOfTurnEvents() {
    for (var event : eventPool.onTurn()) {
      event.run(this);
    }
  }

  /**
   * Find the first next position where there are players from a given position
   * 
   * 
   * @param pos Start position for searching
   * @return the position, or -1 if not players found
   */
  private int nextPositionWithPlayers(int pos) {
    if (pos < 0 || pos > spaces) {
      throw new IllegalArgumentException("Invalid position. 0 < [" + pos + "] <= " + spaces);
    }
    for (var i = pos; i < spaces; i++) {
      var playersAtPos = players.get(i);
      if (playersAtPos != null && !playersAtPos.isEmpty()) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void drawOnCLI() {
    System.out.println(earnedPatches);
    System.out.println("Effects " + eventPool);
    for (var position : players.entrySet()) {
      for (var player : position.getValue()) {
        System.out.print("Position " + position.getKey() + "| ");
        if (player.equals(currentPlayer())) {
          System.out.print(Color.ANSI_GREEN);
        }
        player.drawOnCLI();
        System.out.print(Color.ANSI_RESET);
      }
    }
    System.out.println();
  }

}
