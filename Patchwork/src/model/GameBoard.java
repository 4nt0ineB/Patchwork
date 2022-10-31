package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import view.cli.Color;
import view.cli.DisplayableOnCLI;

public class GameBoard implements DisplayableOnCLI {
  // Max number of patches the player can choose from
  private final int nextPatches = 3;
  // Number of squares on the board
  private final int size = 53;
  // The bank
  private int buttons;
  private int neutralTokenPos;
  private final ArrayList<Patch> patches;
  
  // Current player is always at the top of the stack
  private int currentPlayerPos;
  private int selectedPatch;
  // Players indexed by position
  private final SortedMap<Integer, Stack<Player>> players;
  
  
  /**
   * GameBoard constructor
   * @param size the number of squares on the board
   * @param patches the patches around the board at the beginning
   * @param players the players
   */
  public GameBoard(List<Patch> patches, List<Player> players) {
    Objects.requireNonNull(patches, "List of patches can't be null");
    Objects.requireNonNull(players, "List of players can't be null");
    if(patches.size() < 1) {
      throw new IllegalArgumentException("There must be at least 1 patches");
    }
    if(players.size() < 2) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    buttons = 162;
    this.patches = new ArrayList<>(patches);
    Collections.shuffle(this.patches);
    this.players = new TreeMap<>();
    this.players.put(0, new Stack<Player>());
    this.players.get(0).addAll(players);
    Collections.shuffle(this.players.get(0));
    currentPlayerPos = 0;
    // delegate
    this.players.get(0).stream().forEach(p -> p.buttonIncome(getButtons(5)));
    neutralTokenPos = Patch.minPatch(this.patches);
    // @Todo check min square size to fit each patch ?
  }
  
  public Player currentPlayer() {
    return players.get(currentPlayerPos).peek();
  }
  
  /**
   * Subtract amount of buttons from the game board buttons.
   * if it goes below 0, return the maximum.
   * @param 
   * @return the maximum amount possible for the amount asked
   */
  private int getButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("Can't get " + amount  + " buttons");
    }
    var max = buttons - amount < 0 ? buttons : amount;
    buttons -= max;
    return max;
  }
  
  public void currentPlayerAdvance() {
    if(!currentPlayerCanAdvance()) {
      return;
    }
    int newPosition = nextPositionWithPlayers(currentPlayerPos + 1) + 1;
    int buttonIncome = newPosition - currentPlayerPos;
    currentPlayerMove(newPosition);
    currentPlayer().buttonIncome(buttonIncome);
  }
  
  /**
   * Move the current player at the given position
   * @param newPosition
   */
  private void currentPlayerMove(int newPosition) {
    if(newPosition < 0 || newPosition > size) {
      throw new IllegalArgumentException("The new position can't exceed boundaries (0 <= newp: " + newPosition + "<= " + size);
    }
    var player = players.get(currentPlayerPos).pop();
    var playersAtPos = players.get(newPosition);
    if(playersAtPos == null) {
      players.put(newPosition, new Stack<>());
    }
    players.get(newPosition).add(player);
    currentPlayerPos = newPosition;
  }
  
  /**
   * The player can advance if he has not reached the end
   * and if a player is ahead of him
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return currentPlayerPos != size && nextPositionWithPlayers(currentPlayerPos + 1) != -1;
  }
  
  public boolean currentPlayerCanChosePatch() {
    var nextPatches = nextPatches();
    if(nextPatches.isEmpty()) {
      return false;
    }
    return nextPatches.stream().anyMatch(patch -> currentPlayer().canBuyPatch(patch) == true);
  }
  
  /**
   * Set the next currentPlayer 
   * @return
   */
  public void nextTurn() {
    currentPlayerPos = nextPositionWithPlayers(0); // the most behind player
    unselectPatch();
    if(!currentPlayerCanAdvance() 
        && !currentPlayerCanChosePatch()
        ){
      throw new AssertionError("Unwanted game state. Player is stucked. "
          + "Probably bad init settings for the game board");
    }
  }
  
  /**
   * Find the first next position where there are players
   * from a given position
   * 
   * 
   * @param pos Start position for searching 
   * @return the position, or -1 if not players found
   */
  private int nextPositionWithPlayers(int pos) {
    if(pos < 0 || pos > size) {
      throw new IllegalArgumentException("Invalid position. 0 < [" + pos + "] <= " + size);
    }
    for(var i = pos; i < size; i++) {
      var playersAtPos = players.get(i);
      if(playersAtPos != null && !playersAtPos.isEmpty()) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Return a list of the next available patches
   * for this turn
   * @return
   */
  public List<Patch> nextPatches(){
    var nextPatchesForTurn = new ArrayList<Patch>();
    // NEXTPATCHES *after* the token 
    for(var i = 1; i <= nextPatches; i++) {
      var patch = nextPatch(neutralTokenPos + i);
      nextPatchesForTurn.add(patch);
    }
    return nextPatchesForTurn;
  }
  
  /**
   * Select a patch among the next available
   * @param patch
   */
  public void selectPatch(int i) {
//    Objects.requireNonNull(patch, "The path can't be null");
    var nextPatches = nextPatches();
    if(nextPatches.isEmpty()) {
      return;
    }
    if(i < 0 || i >= nextPatches.size()) {
      throw new IllegalArgumentException("Given index does'nt exists for the nextPatches");
    }
    selectedPatch = neutralTokenPos + 1 + i;
    //    for(var i = 0; i < nextPatches; i++) {
//      // We want to compare pointers !
//      if(nextPatch(neutralTokenPos + 1 + i) == patch) {
//        selectedPatch = neutralTokenPos + 1 + i;
//      }
//    }
  }
  
  public Patch selectedPatch() {
    if(selectedPatch == -1) {
      throw new AssertionError("A patch should have been selected first");
    }
    return patches.get(selectedPatch);
  }
  
  public void unselectPatch() {
    selectedPatch = -1;
  }
  
  public boolean playSelectedPatch() {
    if(!currentPlayer().buyAndPlacePatch(selectedPatch())){
      return false;
    }
    // Moves
    var newPosition = Math.min(size, currentPlayerPos + selectedPatch().moves());
    currentPlayerMove(newPosition);
    // Buttons
    var patch = patches.remove(selectedPatch);
    buttons += patch.price();
    //
    neutralTokenPos = selectedPatch % patches.size();
    unselectPatch();
    return true;
  }
  
  /**
   * Return the next patch after index 
   * by using modulo on the number of patches
   * (loop trough patches)
   * @param index
   * @return the patch or null
   */
  private Patch nextPatch(int index) {
    if(patches.size() == 0) {
      return null;
    }
    return patches.get(index % patches.size());
  }
  
  @Override
  public void drawOnCLI() {
    for(var position: players.entrySet()) {
      for(var player: position.getValue()) {
        System.out.print("Position " + position.getKey() + "| ");
        if(player == currentPlayer()) {
          System.out.print(Color.ANSI_GREEN);
        }
        player.drawOnCLI();
        System.out.print(Color.ANSI_RESET);
      }
    }
    System.out.println();
  }
  
}
