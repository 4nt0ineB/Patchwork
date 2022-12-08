package fr.uge.patchwork.model.component.gameboard.event;
/**
 * 
 * Implement an event on the game board
 * 
 * <p>
 * The event are triggered depending of their position 
 *
 */
public record Event(EventType type, int position) {

  /**
   * Test if the event is positioned inside a given interval
   * 
   * @param n start (included)
   * @param m end (included)
   * @return
   */
  public Boolean isPositionedBetween(int n, int m) {
    return !runEachTurn() && position >= n && position <= m;
  }
  
  public int position() {
  	return position;
  }
  
  public EventType type() {
    return type;
  }

  public Boolean runEachTurn() {
    return position < 0;
  }

  @Override
  public String toString() {
    return  "{" 
        + (position > -1 ? "Position: " + position + ", " : ", " ) 
        + "}";
  }
}