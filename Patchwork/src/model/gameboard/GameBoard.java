package model.gameboard;



import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import model.Action;
import model.Coordinates;
import model.Patch;
import model.Player;
import model.QuiltBoard;
import model.gameboard.event.Event;
import model.gameboard.event.EventPool;
import model.gameboard.event.EventType;
import model.gameboard.event.PositionedEvent;
import view.cli.Color;
import view.cli.DisplayableOnCLI;
import view.cli.PatchworkCLI;

public class GameBoard implements DisplayableOnCLI {

  // Number of squares on the board
  private final int spaces;
  // The bank
  private int buttons;
  // Current player is always at the top of the stack
  private int currentPlayerPos;
  // Players indexed by position
  private final SortedMap<Integer, Stack<Player>> players = new TreeMap<>();
  // Patch manager (around the board patches)
  private final NeutralToken neutralToken;
  // Patches stack that must be played by the current player
  private final Stack<Patch> patchesToPlay = new Stack<>();
  // Event queue to process at the end of the turn
  private final Queue<Event> eventQueue = new LinkedList<>();
  // All events in game
  private final EventPool eventPool = new EventPool();
  // The actions the player can do during this turn
  private final LinkedHashSet<Action> availableActions = new LinkedHashSet<>();
  
  /**
   * GameBoard constructor
   * 
   * @param nextPatches max number of patches available each turn for the current
   *                    player
   * @param spaces      the number of spaces on the board
   * @param buttons     the number of buttons in the bank
   * @param patches     the patches around the board at the beginning
   * @param players     the players
   * @param the         list of events for the board
   */
  public GameBoard(int nextPatches, int spaces, int buttons, List<Patch> patches, List<Player> players,
      List<Event> events) {
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
    Collections.shuffle(this.players.get(0));
    this.spaces = spaces - 1;
    this.neutralToken = new NeutralToken(nextPatches, patches);
    this.eventPool.addAll(events);
    resetActions();
  }

  /**
   * Return a list of only purchasable patches by the current player
   * 
   * @return
   */
  public List<Patch> availablePatches() {
    return neutralToken.availablePatches().stream().filter(patch -> currentPlayer().canBuyPatch(patch)).toList();
  }

  /**
   * Get the current player of the turn
   * 
   * @return
   */
  public Player currentPlayer() {
    return players.get(currentPlayerPos).peek();
  }

  /**
   * Select a patch among the next available from those around the board
   * 
   * @param patch
   */
  public void selectPatch(Patch patch) {
    neutralToken.select(patch);
    // also, add the patch to the Patch waiting queue
    patchesToPlay.add(patch);
  }

  /**
   * Get the selected patch from those available, around the board
   * 
   * @return
   */
  public Patch selectedPatch() {
    return neutralToken.selected();
  }

  /**
   * Unselect the patch previously selected from those available around the board.
   * Only patches from the around the board can be unselected.
   */
  public void unselectPatch() {
    // Remove the patch from patches waiting queue as well
    var patch = neutralToken.selected();
    if(patch != null) {
      patchesToPlay.remove(patch);
      neutralToken.unselect();
    }
  }

  /**
   * Get the next patch that the current player must manipulate to place and buy
   * to put on his quilt
   * 
   * @return
   */
  public Patch nextPatchToPlay() {
    if(patchesToPlay.isEmpty()) {
      return null;
    }
    return patchesToPlay.peek();
  }

  /**
   * Play the next patch from the patches waiting queue
   * 
   * @return
   */
  public boolean playNextPatch() {
    var patch = patchesToPlay.peek();
    if (patch == null) {
      return false;
    }
    // or else the patch comes from somewhere else
    if (!currentPlayerPlayPatch(patch)) {
      return false;
    }
    // The patch have been place on the quilt, we extract it from the waiting queue
    patch = patchesToPlay.pop();
    // Where does the patch comes from ?
    if (neutralToken.availablePatches().contains((patch))) { 
      // The patch comes from the neutral token
      patch = neutralToken.extractSelected();
      // The player played a patch from around the board
      // and can no more execute this actions
      availableActions.clear();
    }
    // The patch comes from somewhere else. But we just extracted it already
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
  protected int getButtons(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Can't get " + amount + " buttons");
    }
    var max = buttons - amount < 0 ? buttons : amount;
    buttons -= max;
    return max;
  }

  /**
   * Advance the current player to the space in front of the next player. This
   * action lead to button income proportional of number of crossed spaces.
   */
  public void currentPlayerAdvance() {
    if (!currentPlayerCanAdvance()) {
      return;
    }
    int newPosition = nextPositionWithPlayers(currentPlayerPos + 1) + 1;
    int buttonIncome = newPosition - currentPlayerPos;
    currentPlayerMove(newPosition);
    currentPlayer().buttonIncome(buttonIncome);
    // The player advance on the board
    // and can no more execute this actions
    availableActions.clear();
  }

  /**
   * Move the current player to the given position. The move will be limited by
   * boundaries [0, spaces]
   * 
   * @param newPosition
   */
  private void currentPlayerMove(int newPosition) {

    var move = newPosition - currentPlayerPos;
    if (move > 0) {
      newPosition = Math.min(spaces, newPosition);
      // Check if events on path (only when moving forward !)
      for (var event : eventPool.positionedBetween(currentPlayerPos + 1, newPosition)) {
        eventQueue.add(event);
      }
    } else if (move < 0) {
      newPosition = Math.min(0, newPosition);
    }
    
    var player = players.get(currentPlayerPos).pop();
    var playersAtPos = players.get(newPosition);
    if (playersAtPos == null) {
      players.put(newPosition, new Stack<>());
    }
    players.get(newPosition).add(player);
    currentPlayerPos = newPosition;
  }

  /**
   * Test if the current player can advance on the board The player can advance if
   * he has not reached the end and if a player is ahead of him
   * 
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return currentPlayerPos != spaces 
        && nextPositionWithPlayers(currentPlayerPos + 1) != -1
        && availableActions.contains(Action.ADVANCE);
  }

  /**
   * Test if the current player can choose a patch()
   * 
   * @return
   */
  public boolean currentPlayerCanSelectPatch() {
    if (neutralToken.availablePatches().isEmpty() 
        || currentPlayerPos == spaces // the player reached the end of the board
        || !availableActions.contains(Action.SELECT_PATCH)) {
      return false;
    }
    return neutralToken.availablePatches().stream()
        .anyMatch(patch -> currentPlayer().canBuyPatch(patch) == true);
  }

  /**
   * Set the next currentPlayer
   * 
   * @exception AssertionError if the player is stuck.
   * @return
   */
  public boolean nextTurn() {
    endOfTurnEvents(); // add on turn events before end of turn
    if(!availableActions.isEmpty()) {
      return false;
    }
    if(!eventQueue.isEmpty()) {
      return false;
    }
    if (!patchesToPlay.isEmpty()) {
      return false; // can't change player, the current has patches to deal with
    }
    currentPlayerPos = nextPositionWithPlayers(0); // the most behind player
    resetActions();
    if (!currentPlayerCanAdvance() && !currentPlayerCanSelectPatch()) {
      throw new AssertionError("Unwanted game state. Player is stuck. "
          + "Probably bad init settings for the game board or the game is finished");
    }
    return true;
  }

  public List<Event> eventQueue() {
    return eventQueue.stream().toList();
  }

  /**
   * Find the first next position where there are players from a given position
   * 
   * @param pos Start position for searching
   * @return the position, or -1 if not players found
   */
  protected int nextPositionWithPlayers(int pos) {
    if (pos < 0 || pos > spaces) {
      // throw new IllegalArgumentException("Invalid position. 0 < [" + pos + "] <= "
      // + spaces);
      return -1;
    }
    for (var i = pos; i <= spaces; i++) {
      var playersAtPos = players.get(i);
      if (playersAtPos != null && !playersAtPos.isEmpty()) {
        return i;
      }
    }
    return -1;
  }
  
  public void endOfTurnEvents() {
    for (var event : eventPool.onTurn()) {
      eventQueue.add(event);
    }
  }

  /**
   * Run all events then clear the queue
   */
  public void runWaitingEvents() {
    eventQueue.stream().forEachOrdered(event -> event.run(this));
    eventQueue.clear();
  }
  
  /**
   * Reset the available actions for the current player
   */
  private void resetActions() {
    availableActions.clear();
    availableActions.add(Action.ADVANCE);
    availableActions.add(Action.SELECT_PATCH);
  }

  @Override
  public void drawOnCLI(PatchworkCLI ui) {
    var builder = ui.builder();
    builder.append("Position |\n");
    for (var position : players.entrySet()) {
      for (var player : position.getValue()) {
        builder.append(String.format("%9d|", position.getKey()));
        if (player.equals(currentPlayer())) {
          builder.append(Color.ANSI_GREEN);
        }
        builder
        .append(" ");
        player.drawOnCLI(ui);
        builder
        .append(Color.ANSI_RESET)
        .append("\n");
      }
    }
    builder.append("\n");
  }

  /**
   * The game is finished when all the players are on the last space. In short,
   * the game is finished when the position of the most behind player is the last
   * space.
   * 
   * @return
   */
  public boolean isFinished() {
    return nextPositionWithPlayers(0) == spaces;
  }

  public static GameBoard basicBoard() {
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
    var gameBoard = new GameBoard(3, 53, 152, patches, List.of(player1, player2), List.<Event>of());
    return gameBoard;
  }
  
  public static GameBoard fullBoard() {
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
      gb.patchesToPlay.add(new Patch(0, 0, 0, List.of(new Coordinates(0, 0))));
      return true;
    }));
    var gameBoard = new GameBoard(3, 54, 152, patches, List.of(player1, player2), events);
    return gameBoard;
  }
 

  private static Effect makeButtonIncomeEffect() {
    return (GameBoard gb) -> { 
      gb.currentPlayer().buttonIncome(gb.getButtons(gb.currentPlayer().quilt().buttons())); 
      return true; 
      };
  }
  
  private static Effect makePatchIncomeEffect(Patch patch) {
    return (GameBoard gb) -> { 
      gb.patchesToPlay.add(patch);
      return true; 
      };
  }
}
