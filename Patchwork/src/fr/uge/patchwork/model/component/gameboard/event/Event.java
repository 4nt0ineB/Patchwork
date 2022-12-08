package fr.uge.patchwork.model.component.gameboard.event;

import fr.uge.patchwork.util.xml.XMLElement;

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

  /**
   * Make a new Event from a XMLElement
   * @param element XMLELement 
   * @exception IllegalStateException If the XMLElement is empty
   * @return
   */
  public static Event fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    var type = EventType.valueOf(element.getByTagName("type").content());
    var position = Integer.parseInt(element.getByTagName("position").content());
    return new Event(type, position);
  }
  
}