package model.gameboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

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
  private int buttons;
  // Current player is always at the top of the stack
  private Player currentPlayer;
  // Players indexed by position
  private final LinkedHashSet<Player> players = new LinkedHashSet<>();
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
   * @param patches     the patches around the board at the beginning
   * @param players     the players
   * @param the         list of events for the board
   */
  public GameBoard(int spaces, int nextPatches, int buttons, List<Patch> patches, Set<Player> players, List<Event> events) {
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
    this.spaces = spaces - 1;
    this.buttons = buttons;
    this.neutralToken = new NeutralToken(nextPatches, patches);
    this.players.addAll(players);
    this.eventPool.addAll(events);
  }

  public void init() {
    currentPlayer = latestPlayer();
    updateActions();
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
    return currentPlayer;
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
    if (patch != null) {
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
    if (patchesToPlay.isEmpty()) {
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
    if(currentPlayerMove(currentPlayer.position() + patch.moves())) {
      buttons += patch.price();
    }
    return true;
  }

  /**
   * Advance the current player to the space in front of the next player. This
   * action lead to button income proportional of number of crossed spaces.
   */
  public void currentPlayerAdvance() {
    if (!currentPlayerCanAdvance()) {
      return;
    }
    int newPosition = nextPlayerFrom(currentPlayer.position() + 1).position() + 1;
    int buttonIncome = newPosition - currentPlayer.position();
    if (currentPlayerMove(newPosition)) {
      currentPlayer().buttonIncome(getButtons(buttonIncome));
      // The player advance on the board
      // and can no more execute this actions
      availableActions.clear();
    }
  }


  private int getButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("The amount of button requested can't be negative");
    }
    if(buttons - amount < 0) {
      throw new AssertionError("The amount of button requested exceed the amount of button on the board. The game is broken. Check your init settings");
    }
    buttons -= amount;
    return amount;
  }

  /**
   * Test if a given position is allowed
   * @exception IllegalArgumentException - if the position exceeds boundaries
   * @param position
   */
  private void testPosition(int position) {
    if (position < 0 || position > spaces) {
      throw new IllegalArgumentException("The new position can't exceed boundaries 0 <= " + position + " <= " + spaces);
    }
  }

  /**
   * Move the current player to the given position. The move will be limited by
   * boundaries [0, spaces]
   * @exception IllegalArgumentException - if the position exceeds boundaries
   * @param newPosition
   */
  private boolean currentPlayerMove(int newPosition) {
    testPosition(newPosition);
    var move = newPosition - currentPlayer.position();
    if (move > 0) {
      System.out.println("HEY\n moves from " + (currentPlayer.position() + 1) + " to " + newPosition);
      newPosition = Math.min(spaces, newPosition);
      // Check if events on path (only when moving forward !)
      eventQueue.addAll(eventPool.positionedBetween(currentPlayer.position() + 1, newPosition));
      System.out.println(eventQueue);
    } else if (move < 0) {
      newPosition = Math.min(0, newPosition);
    } else {
      return false;
    }
    // Important to place the player at the end of the list
    // meaning the order of placement on spaces
    players.remove(currentPlayer);
    currentPlayer.move(newPosition);
    players.add(currentPlayer);
    return true;
  }

  /**
   * Test if the current player can advance on the board The player can advance if
   * he has not reached the end and if a player is ahead of him
   * 
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return availableActions.contains(Action.ADVANCE);
  }

  /**
   * Test if the current player can choose a patch()
   * 
   * @return
   */
  public boolean currentPlayerCanSelectPatch() {
    return availableActions.contains(Action.SELECT_PATCH);
  }

  /**
   * Set the next currentPlayer
   * 
   * @exception AssertionError if the player is stuck.
   * @return
   */
  public boolean nextTurn() {
    eventQueue.addAll(eventPool.onTurn()); // add on turn events before end of turn
    if (!availableActions.isEmpty() // Player has things to do
        || !eventQueue.isEmpty()    // Remaining event to treat for the player
        || !patchesToPlay.isEmpty() // can't change player, the current has patches to deal with
        ) {
      return false;
    }
    currentPlayer = latestPlayer();
    updateActions();
    if (availableActions.isEmpty()) {
      throw new AssertionError("Unwanted game state. Player is stuck. "
          + "Probably bad init settings for the game board or the game is finished."
          + " Also don't forget to init the board.");
    }
    return true;
  }

  public List<Event> eventQueue() {
    return eventQueue.stream().toList();
  }

  /**
   * Search the first next player who can play.
   * The search starts at a given position.s
   * 
   * @param position
   * @return the player, or null
   */
  public Player nextPlayerFrom(int position) {
    testPosition(position);
    Player nextPlayer = null;
    var iterator = players.iterator();
    while (iterator.hasNext()) {
      var player = iterator.next();
        if (nextPlayer != null) {
          if (nextPlayer.position() == player.position()) {
            nextPlayer = player;
          } else {
            break;
          }
        } else if(player.position() >= position) {
          nextPlayer = player;
        }
    }
    return nextPlayer;
  }

  public Player latestPlayer() {
    return nextPlayerFrom(0);
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
  private void updateActions() {
    availableActions.clear();
    if(currentPlayer.position() != spaces 
        && nextPlayerFrom(currentPlayer.position() + 1) != null) {
      availableActions.add(Action.ADVANCE);    
    }
    if(!neutralToken.availablePatches().isEmpty()
       && neutralToken.availablePatches().stream().anyMatch(patch -> currentPlayer().canBuyPatch(patch) == true)) {
      availableActions.add(Action.SELECT_PATCH);
    }
  }

  @Override
  public void drawOnCLI(PatchworkCLI ui) {
    var builder = ui.builder();
    builder.append("[ ---- (Buttons: ")
    .append(buttons)
    .append(") - (Patches: ")
    .append(neutralToken.numberOfPatches()).append(") ---- ]\n");
    for (var player : players) {
      if (player.equals(currentPlayer())) {
        builder.append(Color.ANSI_GREEN);
      }
      player.drawOnCLI(ui);
      builder.append(Color.ANSI_RESET).append("\n");
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
    return latestPlayer().position() == spaces;
  }
  
  /**
   * Make a initialized new basic game board 
   * @return a basic game board
   */
  public static GameBoard basicBoard() {
    // turn this into config files ?
    var patches = new ArrayList<Patch>();
    var squaredShape = List.of(new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 0),
        new Coordinates(1, 1));
    for (var i = 0; i < 20; i++) {
      patches.add(new Patch(3, 4, 1, squaredShape));
      patches.add(new Patch(2, 2, 0, squaredShape));
    }
    var player1 = new Player("Player 1", 5, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 5, new QuiltBoard(9, 9));
    var gameBoard = new GameBoard(53, 3, 152, patches, Set.<Player>of(player1, player2), List.<Event>of());
    gameBoard.init();
    return gameBoard;
  }

  /**
   * Make a initialized new standard game board 
   * @return a basic game board
   */
  public static GameBoard fullBoard() {
    // turn this into config files... very ugly
    var patches = List.of(
        new Patch(2, 2, 0, List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(0, 1))),
        new Patch(2, 3, 0,
            List.of(new Coordinates(-1, -1), new Coordinates(-1, 0), new Coordinates(-1, 1), new Coordinates(0, 0),
                new Coordinates(1, -1), new Coordinates(1, 0), new Coordinates(1, 1))),
        new Patch(2, 1, 0,
            List.of(new Coordinates(-1, 0), new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(1, 0),
                new Coordinates(1, 1), new Coordinates(2, 0))),
        new Patch(2, 2, 0,
            List.of(new Coordinates(-1, -1), new Coordinates(-1, 0), new Coordinates(0, -1), new Coordinates(0, 0),
                new Coordinates(1, 0))),
        new Patch(1, 2, 0,
            List.of(new Coordinates(-1, 0), new Coordinates(-1, 1), new Coordinates(0, 0), new Coordinates(1, 0),
                new Coordinates(1, 1))),
        new Patch(2, 1, 0, List.of(new Coordinates(0, -1), new Coordinates(0, 0))),
        new Patch(1, 2, 0,
            List.of(new Coordinates(-1, 0), new Coordinates(-1, 1), new Coordinates(0, 0), new Coordinates(1, 0),
                new Coordinates(2, 0), new Coordinates(2, -1))),
        new Patch(4, 2, 0,
            List.of(new Coordinates(0, -2), new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(1, -1),
                new Coordinates(1, 0), new Coordinates(1, 1))),
        new Patch(2, 2, 0,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 0))),
        new Patch(3, 1, 0, List.of(new Coordinates(0, -1), new Coordinates(-1, 0), new Coordinates(0, 0))),
        new Patch(3, 4, 0,
            List.of(new Coordinates(-2, 0), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, 1),
                new Coordinates(1, 0))),
        new Patch(0, 3, 1,
            List.of(new Coordinates(0, -2), new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(-1, 0),
                new Coordinates(1, 0), new Coordinates(0, 1))),
        new Patch(1, 4, 1,
            List.of(new Coordinates(0, -2), new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(-1, 0),
                new Coordinates(1, 0), new Coordinates(0, 1), new Coordinates(0, 2))),
        new Patch(3, 2, 1,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(-1, 0), new Coordinates(1, -1))),
        new Patch(5, 3, 1,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(-1, 0), new Coordinates(0, 1),
                new Coordinates(1, -1), new Coordinates(1, 0), new Coordinates(2, 0), new Coordinates(1, 1))),
        new Patch(4, 2, 1,
            List.of(new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(1, 0), new Coordinates(1, 1))),
        new Patch(1, 5, 1,
            List.of(new Coordinates(-1, -1), new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(0, 1),
                new Coordinates(0, 2), new Coordinates(-1, 2))),
        new Patch(7, 1, 1,
            List.of(new Coordinates(0, -2), new Coordinates(-0, -1), new Coordinates(0, 0), new Coordinates(0, 1),
                new Coordinates(0, 2))),
        new Patch(10, 3, 2,
            List.of(new Coordinates(-2, 0), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(1, 0),
                new Coordinates(1, 1))),
        new Patch(7, 2, 2,
            List.of(new Coordinates(-1, -1), new Coordinates(0, -1), new Coordinates(1, -1), new Coordinates(0, 0),
                new Coordinates(0, 1), new Coordinates(0, 2))),
        new Patch(5, 4, 2,
            List.of(new Coordinates(0, -1), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, 1),
                new Coordinates(1, 0))),
        new Patch(2, 3, 1,
            List.of(new Coordinates(0, 0), new Coordinates(1, -1), new Coordinates(1, 0), new Coordinates(0, 0),
                new Coordinates(0, 1), new Coordinates(0, 2))),
        new Patch(5, 5, 2,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(-1, 1), new Coordinates(0, 1),
                new Coordinates(1, 1))),
        new Patch(3, 6, 2,
            List.of(new Coordinates(-1, -1), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, 1),
                new Coordinates(1, 0), new Coordinates(1, -1))),
        new Patch(10, 5, 3,
            List.of(new Coordinates(-1, -1), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, -1),
                new Coordinates(1, 0), new Coordinates(2, 0))),
        new Patch(7, 4, 2,
            List.of(new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 1),
                new Coordinates(1, 0), new Coordinates(2, 0))),
        new Patch(8, 6, 3,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(1, -1), new Coordinates(1, 0),
                new Coordinates(0, 1), new Coordinates(-1, 1))),
        new Patch(6, 5, 2,
            List.of(new Coordinates(-1, -1), new Coordinates(-1, 0), new Coordinates(0, 0), new Coordinates(0, -1))),
        new Patch(10, 4, 3,
            List.of(new Coordinates(0, 0), new Coordinates(1, -1), new Coordinates(1, 0), new Coordinates(0, 1),
                new Coordinates(-1, 1))),
        new Patch(7, 6, 3,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(1, 0), new Coordinates(1, 1))),
        new Patch(4, 6, 2,
            List.of(new Coordinates(0, -1), new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(-1, 1))));
    var player1 = new Player("Player 1", 5, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 5, new QuiltBoard(9, 9));
    var events = new ArrayList<Event>();
    for(var position: List.of(5, 11, 17, 23, 29, 35, 41, 47)) {
      events.add(new PositionedEvent(EventType.BUTTON_INCOME, position, false, makeButtonIncomeEffect()));
    }
    for(var position: List.of(20, 26, 32, 44, 50)) {
      events.add(new PositionedEvent(EventType.PATCH_INCOME, position, false, makePatchIncomeEffect(new Patch(0, 0, 0, List.of(new Coordinates(0,0))))));
    }
    // @Todo make special tile event !
    var gameBoard = new GameBoard(53, 3, 152, patches, Set.<Player>of(player1, player2), events);
    gameBoard.init();
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
