package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GameBoard {
  // max possible moves allowed
  private final int size;
  private int buttons;
  private int neutralTokenPos;
  private Player currentPlayer;
  private final ArrayList<Patch> patches;
  // the players indexed by position
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
    Collections.shuffle(patches);
    this.players = new HashMap<>();
    var playersList = new ArrayList<>(players);
    Collections.shuffle(playersList);
    this.players.put(0, playersList);
    buttons = 162;
    // randomly chosen first player
    currentPlayer = this.players.get(0).get(0);
    this.players.get(0).stream().forEach(p -> p.updateButtons(getButtons(5)));
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
  
  /**
   * Set currentPlayer as the nextPlayer to play
   */
  private void nextPlayer() {
    var nextPlayer = players.get(0);
    for(var kv: players.entrySet()) {
      var mostBehindPlayers = kv.getValue();
      if(mostBehindPlayers != null && mostBehindPlayers.size() > 0) {
        currentPlayer = mostBehindPlayers.get(0);
      }
    }
    throw new IllegalStateException("No player found");
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
      throw new IllegalArgumentException("The new position can't exceed bounderies (0 <= newp: " + newPosition + "<= " + size);
    }
    for(var playersAtPos: players.values()) {
      if(playersAtPos != null && playersAtPos.remove(player)) {
        var playersAtNewPos = players.get(newPosition);
        if(playersAtNewPos == null) {
          players.put(newPosition, new ArrayList<>());
        }
        playersAtNewPos.add(player);
        return true;
      }
    }
    return false;
  }
  
}
