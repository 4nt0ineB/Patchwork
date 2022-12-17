package fr.uge.patchwork.model.component.gameboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.player.Player;

/**
 * 
 * Implementation of the game board
 * 
 *  <p>
 * Provides API to run a patchwork game.
 * It handle players management, events, buttons circulation.
 * 
 *
 */
public class TrackBoard {

  // Number of squares on the board
  private final int spaces;
  // Players indexed by position
  private final LinkedHashSet<Player> players = new LinkedHashSet<>();
  //All events in game
  private final Queue<Event> events = new LinkedList<>();

  /**
   * GameBoard constructor
   * 
   * @param patchByTurn max number of patches available each turn for the current player
   * @param spaces      the number of spaces on the board
   * @param patches     the patches around the board at the beginning
   * @param players     the players
   * @param the         list of events for the board
   */
  public TrackBoard(int spaces, Set<Player> players, List<Event> events) {
    Objects.requireNonNull(players, "List of players can't be null");
    if (players.size() < 1) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    if (spaces < 1) {
      throw new IllegalArgumentException("The number of spaces on the board can't be lower than 1");
    }
    this.spaces = spaces - 1; // 54 spaces => [0;53]
    this.players.addAll(players);
    this.events.addAll(events);
  }

  public int spaces() {
    return spaces;
  }

  /**
   * Move the current player to the given position. 
   * The move will be limited by boundaries [0, spaces]
   * @param newPosition
   */
  public List<Event> movePlayer(Player player, int moves) {
    var newpos = Math.max(0, Math.min(player.position() + moves, spaces));
    var triggeredEvents = new ArrayList<Event>();
    // Check if events on path (only when moving forward !)
    if (newpos - player.position() > 0) {
      triggeredEvents.addAll(triggerEvents(player.position() + 1, newpos));
    }
    // Important to place the player at the end of the list
    // meaning the order of placement on spaces
    players.remove(player);
    player.move(newpos);
    players.add(player);
    return triggeredEvents;
  }
  
  private List<Event> triggerEvents(int from, int to){
    return events.stream()
        .filter(e -> e.isPositionedBetween(from, to))
        .toList();
  }
  
  /**
   * Test if the current player can advance on the board The player can advance if
   * he has not reached the end and if a player is ahead of him
   * 
   * @return true if the player can advance on the board, otherwise false 
   */
  public boolean playerCanAdvance(Player player) {
  	/* In the first turn of the game the player can 
  	 * advance even if he can afford a patch because the
  	 * other player is in front of him 
  	 * (on the same place but below him so in front of him) */
    return player.position() != spaces 
        && (nextPlayerFrom(player.position() + 1) != null
        || countPlayersAt(player.position()) > 1);
  }

  public void removeEvent(Event event) {
    Objects.requireNonNull(event);
    events.remove(event);
  }

  /**
   * Search the first next player who can play.
   * The search starts at a given position.
   * 
   * @param position
   * @return the player, or null
   */
  public Player nextPlayerFrom(int position) {
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

  private long countPlayersAt(int position) {
    return players.stream().filter(p -> p.position() == position).count();
  }
  
  /**
   * Get the furthest behind player on the board
   * @return
   */
  public Player latestPlayer() {
    return nextPlayerFrom(0);
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

  public List<Event> events() {
    return List.copyOf(events);
  }

  public List<Player> players() {
    return List.copyOf(players);
  }  
}
