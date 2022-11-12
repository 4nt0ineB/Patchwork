package model;

import java.util.List;
import java.util.Objects;

import util.xml.parser.XMLElement;
import util.xml.parser.XMLSerializable;

public record Coordinates(int y, int x) implements XMLSerializable<Coordinates> {
  
  @Override
  public String toString() {
    return "(" + y + "," + x + ")";
  }
  
  public Coordinates add(Coordinates other) {
    return new Coordinates(y + other.y, x + other.x);
  }
  
  public Coordinates mul(Coordinates other) {
    return new Coordinates(y * other.y, x * other.x);
  }
  
  public Coordinates sub(Coordinates other) {
    return new Coordinates(y - other.y, x - other.x);
  }
  
  public double distance(Coordinates other) {
    return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2));
  }
  
  public Coordinates abs() {
    return new Coordinates(Math.abs(y), Math.abs(x));
  }
  
  public Coordinates rotateClockwise() {
    return new Coordinates(-1 * x, 1 * y);
  }
  
  public Coordinates rotateAntiClockwise() {
    return new Coordinates(1 * x(), -1 * y);
  }
  
  
  /**
   * Check if coordinates are in a rectangle formed by two pairs of coordinates.
   * with c1 as upper left corner, c2 as lower right corner
   * 
   * @param c1
   * @param c2
   * @return true or false
   */
  public boolean inRectangle(Coordinates c1, Coordinates c2) {
    return x >= c1.x
        && x <= c2.x
        && y >= c1.y
        && y <= c2.y
        ;
  }
  
  public static Coordinates fromText(String text) {
    Objects.requireNonNull(text, "Can't make new coordinates out of null String");
    var parameters = text.stripIndent().replaceAll("[\\(\\)]", "").split(",");
    return new Coordinates(Integer.parseInt(parameters[0]), 
        Integer.parseInt(parameters[1]));
  }

  @Override
  public XMLElement toXML() {
    var coordinatesElement = new XMLElement("Coordinates");
    coordinatesElement.addAll(List.of(
        new XMLElement("x", x), 
        new XMLElement("y", y)));
    return coordinatesElement;
  }

  
  public Coordinates fromXML(XMLElement element) {
    Objects.requireNonNull(element, "xml can't be null");
    if(element.isEmpty()) {
      throw new IllegalStateException("Empty xml");
    }
    if(element.isEmpty()) {
      throw new IllegalStateException("Empty xml");
    }
    
    return null;
  }
  
}
