package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import view.cli.CLIDisplayable;
import view.cli.Color;

public class GameBoard implements CLIDisplayable {
  // Max number of patches the player can choose from
  private final static int NEXTPATCHES = 3;
  // Number of squares on the board
  private final int size;
  // The bank
  private int buttons;
  private int neutralTokenPos;
  private final ArrayList<Patch> patches;
  private Player currentPlayer;
  private int selectedPatch = -1;
  // all the players 
  private final List<Player> unindexedPlayers;
  //the players indexed by position
  private final HashMap<Integer, ArrayList<Player>> players;
  
  /**
   * GameBoard constructor
   * @param size the number of squares on the board
   * @param patches the patches around the board at the beginning
   * @param players the players
   */
  public GameBoard(int size, List<Patch> patches, List<Player> players) {
    if(size < 1) {
      throw new IllegalArgumentException("There must be at least one square on the board");
    }
    Objects.requireNonNull(patches, "List of patches can't be null");
    Objects.requireNonNull(patches, "List of players can't be null");
    if(patches.size() < 1) {
      throw new IllegalArgumentException("There must be at least 1 patches");
    }
    if(players.size() < 2) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    this.size = size;
    this.patches = new ArrayList<>(patches);
    Collections.shuffle(this.patches);
    this.players = new HashMap<>();
    var playersList = List.copyOf(players);
    this.players.put(0, new ArrayList<>(playersList));
    Collections.shuffle(this.players.get(0));
    buttons = 162;
    // randomly chosen first player
    currentPlayer = this.players.get(0).get(0);
    this.players.get(0).stream().forEach(p -> p.updateButtons(getButtons(5)));
    unindexedPlayers = playersList;
    neutralTokenPos = minPatch(this.patches);
    // @Todo check min square size to fit each patch
  }
  
  /**
   * Substract amount of buttons from the game board buttons.
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
    changePlayerPosition(currentPlayer, currentPlayer.position() + 1);
  }
  
  /**
   * The player can advance if he not reached the end
   * and if a player is ahead of him
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return currentPlayer.position() != size && nextPlayer(currentPlayer.position() + 1) != null;
  }
  
  public boolean currentPlayerCanChosePatch() {
    return nextPatches().stream().allMatch(patch -> currentPlayer.canBuyPatch(patch) == true);
  }
  
  /**
   * Set the next currentPlayer 
   * @return
   */
  public void nextTurn() {
    currentPlayer = nextPlayer(0); // the most behind player
    if(!currentPlayerCanAdvance() 
        && !currentPlayerCanChosePatch()
        ){
      throw new IllegalStateException("Unwanted game state. Player is stucked. "
          + "Should be bad init settings for the game board");
    }
  }
  
  /**
   * Return the first player to play from a given position
   * 
   * @param pos;
   * @return Player or null if no player found
   */
  public Player nextPlayer(int pos) {
    if(pos < 0 || pos > size) {
      throw new IllegalArgumentException("Invalid position. 0 < [" + pos + "] <= +");
    }
    var nextPlayer = players.get(0);
    for(var kv: players.entrySet()) {
      if(kv.getKey() < pos) {
        continue;
      }
      var mostBehindPlayers = kv.getValue();
      if(mostBehindPlayers != null && mostBehindPlayers.size() > 0) {
        // the last added player (as indicated in the rules (the one on top))
        return mostBehindPlayers.get(mostBehindPlayers.size() - 1);
      }
    }
    return null;
  }
  
  /**
   * Change the current position of a players
   * @param player
   * @param newPosition
   * @return
   */
  private boolean changePlayerPosition(Player player, int newPosition) {
    Objects.requireNonNull(player, "Player can't be null");
    if(newPosition < 0 || newPosition > size) {
      throw new IllegalArgumentException("The new position can't exceed boundaries (0 <= newp: " + newPosition + "<= " + size);
    }
    players.get(player.position()).remove(player);
    var playersAtNewPos = players.get(newPosition);
    if(playersAtNewPos == null) {
      players.put(newPosition, new ArrayList<>());
    }
    players.get(newPosition).add(player);
    // @Todo check if newposition of player exceed size
    player.move(newPosition - player.position());
    return true;
  }
  
  public void selectPatch(int index) {
    if(index < 0 || index >= patches.size()) {
      throw new IllegalArgumentException("Wrong index");
    }
    selectedPatch = neutralTokenPos + index;
  }
  
  public Patch selectedPatch() {
    return patches.get(selectedPatch);
  }
  
  public void unselectPatch() {
    selectedPatch = -1;
  }
  
  /**
   * @Todo probably not do that
   * @return
   */
  public List<Player> players(){
    return unindexedPlayers;
  }
  
  public Player currentPlayer() {
    return currentPlayer;
  }
  
  /**
   * Return a list of the next available patch
   * @return
   */
  public List<Patch> nextPatches(){
    var nexp = new ArrayList<Patch>();
    for(var i = 0; i < NEXTPATCHES; i++) {
      nexp.add(nextPatch(neutralTokenPos + i));
    }
    return List.copyOf(nexp);
  }
  
  /**
   * Return the next patch after index 
   * by using modulo on the number of patches
   * (loop trough patches)
   * @param index
   * @return the patch or null
   */
  public Patch nextPatch(int index) {
    if(patches.size() == 0) {
      return null;
    }
    return patches.get(index % patches.size());
  }
  
  /**
   * Return index of the smallest patch in a list
   * @param patches
   * @return index or -1
   */
  public int minPatch(List<Patch> patches) {
    Objects.requireNonNull(patches, "Can't find smallest in null obj");
    if(patches.size() == 0) {
      return -1;
    }
    var smallest = 0;
    for(var i = 1; i < patches.size(); i++) {
      if(patches.get(i).countCells() < patches.get(0).countCells()) {
        smallest = i;
      }
    }
    return smallest;
  }

  @Override
  public void drawOnCLI() {
    for(var player: players()) {
      if(player == currentPlayer) {
        System.out.println(Color.ANSI_GREEN);
      }
      player.drawOnCLI();
      System.out.println(Color.ANSI_RESET);
    }
  }
  
}
