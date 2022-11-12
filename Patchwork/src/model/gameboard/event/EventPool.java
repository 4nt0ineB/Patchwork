package model.gameboard.event;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

/**
 * 
 * Provides an event pool to manage the event happening on the game board
 *
 */
public class EventPool {

  private final Queue<Event> events = new LinkedList<>();

  /**
   * 
   * @param event
   */
  public void add(Event event) {
    Objects.requireNonNull(event, "The event can't be null");
    events.add(Objects.requireNonNull(event, "the event can't be null"));
  }
  
  /**
   * 
   * @param events
   */
  public void addAll(List<Event> events) {
    Objects.requireNonNull(events, "The list of events can't be null");
    this.events.addAll(events);
  }

  /**
   * Return a list of all events positioned inside a closed interval [n,m]
   * 
   * @param n start (included)
   * @param m end (included)
   * @return a list of positioned eventss
   */
  public List<Event> positionedBetween(int n, int m) {
    return events.stream().filter(event -> event.isPositionedBetween(n, m)).toList();
  }

  /**
   * Return a list of all events each turn;
   * 
   * @return a list of events not positioned
   */
  public List<Event> notPositionedEvents() {
    return events.stream().filter(event -> event.runEachTurn()).toList();
  }

  /**
   * Remove all inactive events
   */
  public void clear() {
    events.removeIf(event -> !event.active());
  }

  /**
   * Remove all events from the pool
   */
  public void flush() {
    events.clear();
  }

  @Override
  public String toString() {
    var builder = new StringBuilder();
    events.stream().forEach(e -> builder.append(e).append("\n"));
    return builder.toString();
  }
}
