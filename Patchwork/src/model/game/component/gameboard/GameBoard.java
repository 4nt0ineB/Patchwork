package model.game.component.gameboard;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import model.game.component.Patch;
import model.game.component.Player;
import model.game.component.button.ButtonBank;
import model.game.component.button.ButtonOwner;
import model.game.component.button.ButtonValued;
import model.game.component.gameboard.event.Event;
import util.xml.XMLElement;
import view.cli.Color;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class GameBoard implements ButtonOwner, DrawableOnCLI {

  // Number of squares on the board
  private final int spaces;
  private final ButtonBank buttonBank;
  // Players indexed by position
  private final LinkedHashSet<Player> players = new LinkedHashSet<>();
  // Patch manager (patches around the board)
  private final PatchManager patchManager;
  //All events in game
  private final Queue<Event> events = new LinkedList<>();
  // Patches stack gathering all patches that must be played by the current player during the turn
  private final Queue<Patch> patchesToPlay = new LinkedList<>();
  // Event queue to process at the end of the turn
  private final Queue<Event> eventQueue = new LinkedList<>();
  //Current player is always at the top of the stack
  private Player currentPlayer;
  private boolean hasPlayedMainAction = false;

  /**
   * GameBoard constructor
   * 
   * @param patchByTurn max number of patches available each turn for the current
   *                    player
   * @param spaces      the number of spaces on the board
   * @param patches     the patches around the board at the beginning
   * @param players     the players
   * @param the         list of events for the board
   */
  public GameBoard(int spaces, int patchByTurn, int buttons, List<Patch> patches, Set<Player> players, List<Event> events) {
    Objects.requireNonNull(patches, "List of patches can't be null");
    Objects.requireNonNull(players, "List of players can't be null");
    if (patches.size() < 1) {
      throw new IllegalArgumentException("There must be at least 1 patches");
    }
    if (players.size() < 2) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    if (patchByTurn < 1) {
      throw new IllegalArgumentException("The number of available patches during a turn can't be lower than 1");
    }
    if (spaces < 1) {
      throw new IllegalArgumentException("The number of spaces on the board can't be lower than 1");
    }
    this.spaces = spaces - 1; // spaces => space no 0 to no spaces - 1
    this.patchManager = new PatchManager(patchByTurn, patches);
    this.players.addAll(players);
    this.events.addAll(events);
    buttonBank = new ButtonBank(buttons);
  }
  
  /**
   * Must be called before running a game
   */
  public void init() {
    currentPlayer = latestPlayer();
  }

  /**
   * Return the list of all availables patches 
   * (next 3 patches in front of neutral token)
   * 
   * @return List of patches
   */ 
  public List<Patch> availablePatches() {
    return patchManager.availablePatches();
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
   * @exception AssertionError If the given patch 
   * does not exists in the available patches
   * @param patch
   */
  public void selectPatch(Patch patch) {
    patchManager.select(patch);
    // also, add the patch to the patches waiting queue
    addPatchToPlay(patch);
  }

  /**
   * Get the selected patch 
   * from those available around the board
   * 
   * @return
   */
  public Patch selectedPatch() {
    return patchManager.selected();
  }

  /**
   * Unselect the patch previously 
   * selected from those available around the board.
   * Only patches from the around 
   * the board can be unselected.
   */
  public void unselectPatch() {
    // Remove the patch from patches waiting queue as well
    var patch = patchManager.selected();
    if (patch != null) {
      patchesToPlay.remove(patch);
      patchManager.unselect();
    }
  }

  /**
   * Get the next patch the current 
   * player must manipulate to place and buy
   * to put it on his quilt
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
    // The patch have been placed on the quilt, 
    // so we extract it from the waiting queue
    patch = patchesToPlay.poll();
    // Where does the patch comes from ?
    if (patchManager.availablePatches().contains((patch))) {
      // The patch comes from the neutral token
      patch = patchManager.extractSelected();
      // has played a patch from the board, this was a main action
      hasPlayedMainAction = true; 
    }
    // The patch comes from somewhere else. But we just extracted it already
    return true;
  }

  private boolean currentPlayerPlayPatch(Patch patch) {
    if (!currentPlayer().placePatch(patch)) {
      return false;
    }
    currentPlayer.payOwnerFor(this, patch);
    // Moves
    currentPlayerMove(currentPlayer.position() + patch.moves());
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
    hasPlayedMainAction = true;
    if (currentPlayerMove(newPosition)) {
      pay(currentPlayer(), buttonIncome);
    }
  }

  /**
   * Move the current player to the given position. 
   * The move will be limited by boundaries [0, spaces]
   * @param newPosition
   */
  private boolean currentPlayerMove(int newPosition) {
    var move = Math.max(0, Math.min(newPosition, spaces));
    if(move == 0) {
      return false;
    }
    // Check if events on path (only when moving forward !)
    if (move - currentPlayer.position() > 0) {
      eventQueue.addAll(events.stream()
          .filter(event -> event.isPositionedBetween(currentPlayer.position() + 1, move) 
              && event.active() 
              && event.run(this))
          .peek(System.out::println)
          .toList());
    }
    // Important to place the player at the end of the list
    // meaning the order of placement on spaces
    players.remove(currentPlayer);
    currentPlayer.move(move);
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
    return !hasPlayedMainAction && currentPlayer.position() != spaces 
        && nextPlayerFrom(currentPlayer.position() + 1) != null;
  }

  /**
   * Test if the current player can choose a patch()
   * 
   * @return
   */
  public boolean currentPlayerCanSelectPatch() {
    return !hasPlayedMainAction &&!patchManager.availablePatches().isEmpty()
        && patchManager.availablePatches()
        .stream().anyMatch(patch -> currentPlayer().canBuy(patch) == true);
  }

  /**
   * Set the next currentPlayer
   * 
   * @exception AssertionError If the player is stuck.
   * @return
   */
  public boolean nextTurn() {
    // add not positioned events before end of turn
    eventQueue.addAll(events.stream()
        .filter(event -> event.runEachTurn() 
            && event.active() 
            && event.run(this))
        .toList()); 
    if (!hasPlayedMainAction || !patchesToPlay.isEmpty()) { // can't change player, the current has patches to deal with
      return false;
    }
    currentPlayer = latestPlayer();
    eventQueue.clear();
    hasPlayedMainAction = false;
//    if (availableActions.isEmpty()) {
//      throw new AssertionError("Unwanted game state. Player is stuck. \n"
//          + "Probably bad init settings for the game board, or the game is finished.\n"
//          + " Also don't forget to init the board.");
//    }
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
  private Player nextPlayerFrom(int position) {
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
  

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    var builder = ui.builder();
    if(!isFinished()) {
      builder.append("[ ---- (Buttons: ")
      .append(buttons())
      .append(") - (Patches: ")
      .append(patchManager.numberOfPatches()).append(") ---- ]\n");
      for (var player : players) {
        if (player.equals(currentPlayer())) {
          builder.append(Color.ANSI_GREEN);
        }
        player.drawOnCLI(ui);
        builder.append(Color.ANSI_RESET).append("\n");
      }
      builder.append("\n");
    }else {
      builder.append(Color.ANSI_ORANGE)
      .append("[ ---- Scores ---- ] \n")
      .append(Color.ANSI_RESET);
      var sortedPlayers = players.stream().sorted(Comparator.reverseOrder()).toList();
      sortedPlayers.forEach(
          p -> builder.append(p.name()).append(" : ")
          .append(p.score()).append("\n"));
      builder.append(Color.ANSI_YELLOW)
      .append(sortedPlayers.get(0).name())
      .append(" Wins !\n")
      .append(Color.ANSI_RESET);
    }
  }
  
  /**
   * Add a patch to the waiting stack
   * The added patch <b>must</b> be played. 
   * @param patch
   */
  public void addPatchToPlay(Patch patch) {
    Objects.requireNonNull(patch, "The patch can't be null");
    patchesToPlay.add(patch);
  }

  /**
   * The game is finished when all the players are on the last space. 
   * 
   * @return true or false
   */
  public boolean isFinished() {
    // In short, the game is finished when the position 
    // of the latest player is the last space
    return latestPlayer().position() == spaces;
  }

  /**
   * Make new game board from a XMLElement
   * @param element
   * @return
   */
  public static GameBoard fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    var spaces = Integer.parseInt(element.getByTagName("spaces").content());
    var patchByTurn = Integer.parseInt(element.getByTagName("patchByTurn").content());
    var buttons = Integer.parseInt(element.getByTagName("buttons").content());
    var players = element.getByTagName("playerList")
        .getAllByTagName("Player").stream().map(Player::fromXML).collect(Collectors.toSet());
    var patches = element.getByTagName("patchList").getAllByTagName("Patch").stream().map(Patch::fromXML).toList();
    var events = element.getByTagName("eventList").getAllByTagName("Event").stream().map(Event::fromXML).toList();
    return new GameBoard(spaces, patchByTurn, buttons, patches, players, events);
  }

  @Override
  public boolean canPay(int amount) {
    return buttonBank.canPay(amount);
  }

  @Override
  public boolean canBuy(ButtonValued thing) {
    return buttonBank.canBuy(thing);
  }

  @Override
  public void pay(ButtonOwner owner, int amount) {
    buttonBank.pay(owner, amount);
  }

  @Override
  public void payOwnerFor(ButtonOwner owner, ButtonValued thing) {
    buttonBank.payOwnerFor(owner, thing);
  }

  @Override
  public int buttons() {
    return buttonBank.buttons();
  }

  @Override
  public ButtonBank buttonBank() {
    return buttonBank;
  }
}
