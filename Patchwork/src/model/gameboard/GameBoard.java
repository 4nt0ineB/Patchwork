package model.gameboard;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import model.Action;
import model.MenuOption;
import model.Patch;
import model.Player;
import model.button.ButtonOwner;
import model.gameboard.event.Event;
import util.xml.XMLElement;
import view.cli.Color;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class GameBoard extends ButtonOwner implements DrawableOnCLI {

  // Number of squares on the board
  private final int spaces;
  // Players indexed by position
  private final LinkedHashSet<Player> players = new LinkedHashSet<>();
  // Patch manager (patches around the board)
  private final PatchManager patchManager;
  //All events in game
  private final Queue<Event> events = new LinkedList<>();
  // Patches stack gathering all patches that must be played by the current player during the turn
  private final Stack<Patch> patchesToPlay = new Stack<>();
  // Event queue to process at the end of the turn
  private final Queue<Event> eventQueue = new LinkedList<>();
  //Current player is always at the top of the stack
  private Player currentPlayer;
  
  // The actions the player can do during the turn
  private final LinkedHashSet<Action> availableActions = new LinkedHashSet<>();

  // List of index of all 5 SPECIAL PATCHES (1 * 1 patch in 
  private HashSet<Integer> specialPatchesIndex = new HashSet<>();
  // Game mode choosen
  private final MenuOption gameMode;
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
  public GameBoard(int spaces, int patchByTurn, int buttons, List<Patch> patches, Set<Player> players, List<Event> events, MenuOption gameMode) {
    super(buttons);
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
    this.gameMode = gameMode;
    if (gameMode.getBind() == 2) {
    	// FULL GAME MODE CHOOSEN
    	addSpecialPatches();
    }
  }
  
  /**
   * Must be called before running a game
   */
  public void init() {
    currentPlayer = latestPlayer();
    updateActions();
  }

  /**
   * Return the list of all availables patches (next 3 patches in front of neutral token)
   * 
   * @return List of patches
   */ 
  // NO RULES THAT SAYS TO ONLY SHOW THE PURCHASABLE PATCHES 
  // YOU MUST SHOW EVEN THE PATCHES YOU CAN'T AFFORD ELSE RUINNING THE GAME
  public List<Patch> availablePatches() {
    return patchManager.availablePatches();
  }

  /**
   * Return the set of all availables actions during the turn
   * 
   * @return Set of Action
   */ 
  public Set<Action> availableActions() {
    return Set.copyOf(availableActions);
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
   * @exception AssertionError - if the given patch does not exists in the available patches
   * @param patch
   */
  public void selectPatch(Patch patch) {
    patchManager.select(patch);
    // also, add the patch to the patches waiting queue
    addPatchToPlay(patch);
  }

  /**
   * Get the selected patch from those available around the board
   * 
   * @return
   */
  public Patch selectedPatch() {
    return patchManager.selected();
  }

  /**
   * Unselect the patch previously selected from those available around the board.
   * Only patches from the around the board can be unselected.
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
    if (patchManager.availablePatches().contains((patch))) {
      // The patch comes from the neutral token
      patch = patchManager.extractSelected();
      // The player played a patch from around the board
      // and can no more execute this actions
      availableActions.clear();
    }
    // The patch comes from somewhere else. But we just extracted it already
    return true;
  }

  public boolean currentPlayerPlayPatch(Patch patch) {
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
    if (currentPlayerMove(newPosition)) {
      pay(currentPlayer(), buttonIncome);
      // The player advance on the board
      // and can no more execute this actions
      availableActions.clear();
    }
  }

  /**
   * Test if a given position is allowed
   * @exception IndexOutOfBoundsException - if the position exceeds boundaries
   * @param position
   */
  private void testPosition(int position) {
    Objects.checkIndex(position, spaces);
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
      newPosition = Math.min(spaces, newPosition);
      if (isFullGameMode()) {
      	var pos = newPosition;
      	var specialPatchesCount = this.specialPatchesIndex.size();
      	// We look if the player will get a special patch on his way 
      	// Only if his new pos his higher and not equal to an special patch index.
      	// And remove the index so that we can't have twice the same special patch.
      	this.specialPatchesIndex.removeIf(e -> e < pos);
      	specialPatchesCount = specialPatchesCount - this.specialPatchesIndex.size();
      	
      	for (var i = 0; i < specialPatchesCount; i++) {
      		this.addPatchToPlay(Patch.getSpecialPatch());
      	}
      }
      // Check if events on path (only when moving forward !)
      var pos = newPosition;
      var positioned = events.stream()
          .filter(event -> event.isPositionedBetween(currentPlayer.position() + 1, pos))
          .toList();
      eventQueue.addAll(positioned);
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
    // add not positioned events before end of turn
    eventQueue.addAll(events.stream().filter(event -> event.runEachTurn()).toList()); 
    if (!availableActions.isEmpty() // Player has things to do
        || !eventQueue.isEmpty()    // Remaining event to run for the player
        || !patchesToPlay.isEmpty() // can't change player, the current has patches to deal with
        ) {
      return false;
    }
    currentPlayer = latestPlayer();
    updateActions();
    if (availableActions.isEmpty()) {
      throw new AssertionError("Unwanted game state. Player is stuck. \n"
          + "Probably bad init settings for the game board, or the game is finished.\n"
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
  private Player nextPlayerFrom(int position) {
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
    if(!patchManager.availablePatches().isEmpty()
       && patchManager.availablePatches()
       .stream().anyMatch(patch -> currentPlayer().canBuy(patch) == true)) {
      availableActions.add(Action.SELECT_PATCH);
    }
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    var builder = ui.builder();
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
  }
  
  /**
   * Add a patch to the waiting 
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
    // In short,
    // the game is finished when the position of the most behind player is the last
    // space.
    return latestPlayer().position() == spaces;
  }

  /**
   * Make new game board from a XMLElement
   * @param element
   * @return
   */
  public static GameBoard fromXML(XMLElement element, MenuOption gameMode) {
    XMLElement.requireNotEmpty(element);
    var spaces = Integer.parseInt(element.getByTagName("spaces").content());
    var patchByTurn = Integer.parseInt(element.getByTagName("patchByTurn").content());
    var buttons = Integer.parseInt(element.getByTagName("buttons").content());
    var players = element.getByTagName("playerList")
        .getAllByTagName("Player").stream().map(Player::fromXML).collect(Collectors.toSet());
    var patches = element.getByTagName("patchList").getAllByTagName("Patch").stream().map(Patch::fromXML).toList();
    var events = element.getByTagName("eventList").getAllByTagName("Event").stream().map(Event::fromXML).toList();
    return new GameBoard(spaces, patchByTurn, buttons, patches, players, events, gameMode);
  }
  
  /**
   * Add 5 distincts random index to the list of special patches index.
   * 
   * @return void
   */
  private void addSpecialPatches() {
  	int i = 0;
  	while (i < 5) {
  		
  		var f = Math.random() / Math.nextDown(1.0);
  		var x = 1 * (1.0 - f) + spaces * f;
    	if (this.specialPatchesIndex.add(((int) x))) {
    		i++;
    	}
  	}
  }
  
  public boolean isFullGameMode() {
  	if (this.gameMode.getBind() == 2) {
  		return true;
  	}
  	return false;
  }
  
}
