package model.gameboard;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import model.Coordinates;
import model.Patch;
import model.Player;
import model.QuiltBoard;
import model.event.Event;
import model.event.EventPool;
import view.cli.PatchworkCLI;

public class FullGameBoard extends GameBoard {

  // Event manager
  protected final EventPool eventPool = new EventPool();

  protected FullGameBoard(int nextPatches, int spaces, int buttons, List<Patch> patches, List<Player> players,
      ArrayList<Event> events) {
    super(nextPatches, spaces, buttons, patches, players);
    this.eventPool.addAll(events);
  }

  @Override
  protected void currentPlayerMove(int newPosition) {
    // Check if events on path, only when moves forward !
    if (newPosition - currentPlayerPos > 0) {
      for (var event : eventPool.positionedBetween(currentPlayerPos + 1, newPosition)) {
        eventQueue.add(event);
      }
    }
    super.currentPlayerMove(newPosition);
  }

  /**
   * Run events that are not triggered by moves (positioned events)
   */
  public void endOfTurnEvents() {
    for (var event : eventPool.onTurn()) {
      eventQueue.add(event);
    }
  }
  
  @Override
  public boolean nextTurn() {
    endOfTurnEvents(); // add on turn events before end of turn
    return super.nextTurn();
  }

}
