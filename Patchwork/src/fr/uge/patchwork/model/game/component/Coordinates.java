package fr.uge.patchwork.model.game.component;

import java.util.List;

import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.util.xml.XMLSerializable;

public record Coordinates(int y, int x) implements XMLSerializable {
  
  @Override
  public String toString() {
    return "(" + y + "," + x + ")";
  }
  
  /**
   * Add the Coordinates with the corresponding coordinates from an other instance
   * @param other Coordinates
   * @return
   */
  public Coordinates add(Coordinates other) {
    return new Coordinates(y + other.y, x + other.x);
  }
  
  /**
   * Multiply the Coordinates with the corresponding coordinates from an other instance
   * @param other Coordinates
   * @return
   */
  public Coordinates mul(Coordinates other) {
    return new Coordinates(y * other.y, x * other.x);
  }
  
  /**
   * Substract the Coordinates with the corresponding coordinates from an other instance
   * @param other Coordinates
   * @return
   */
  public Coordinates sub(Coordinates other) {
    return new Coordinates(y - other.y, x - other.x);
  }
  
  /**
   * Return the distance between the instance and other Coordinates
   * @param other Coordinates
   * @return
   */
  public double distance(Coordinates other) {
    return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
  }
  
  /**
   * Return the distance in X axis between the instance and other Coordinates
   * @param other Coordinates
   * @return
   */
  public int distanceInX(Coordinates other) {
    return this.x() - other.x();
  }
  
  
  /**
   * Return the distance in Y axis between the instance and other Coordinates
   * @param other Coordinates
   * @return
   */
  public int distanceInY(Coordinates other) {
  	return this.y() - other.y();
  }
  
  
  /**
   * Apply {@link Math#abs} on the each coordinate
   * @return new Coordinates
   */
  public Coordinates abs() {
    return new Coordinates(Math.abs(y), Math.abs(x));
  }
  
  /**
   * Transform the coordinates by a clockwise rotation
   * @return
   */
  public Coordinates rotateClockwise() {
    return new Coordinates(-1 * x, 1 * y);
  }
  
  /**
   * Transform the coordinates by an anti-clockwise rotation
   * @return
   */
  public Coordinates rotateAntiClockwise() {
    return new Coordinates(1 * x(), -1 * y);
  }
  
  
  /**
   * Check if coordinates are in a rectangle formed by two pairs of coordinates.
   * with c1 as upper left corner, c2 as lower right corner
   * 
   * @param c1 Coordinates
   * @param c2 Coordinates
   * @return true or false
   */
  public boolean inRectangle(Coordinates c1, Coordinates c2) {
    return x >= c1.x
        && x <= c2.x
        && y >= c1.y
        && y <= c2.y
        ;
  }
  
  @Override
  public XMLElement toXML() {
    var coordinatesElement = new XMLElement("Coordinates");
    coordinatesElement.addAll(List.of(
        new XMLElement("x", x), 
        new XMLElement("y", y)));
    return coordinatesElement;
  }

  /**
   * Make new Coordinates from a XMLElement
   * @param element XMLElement
   * @exception IllegalStateException If the XMLElement is empty
   * @return
   */
  public static Coordinates fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    return new Coordinates(
        Integer.parseInt(element.getByTagName("x").content()),
        Integer.parseInt(element.getByTagName("y").content()));
  }
 
}
