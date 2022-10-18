package model;

import java.util.Objects;

public record Coordinates(int y, int x) {

  @Override
  public int hashCode() {
    return Objects.hash(y, x);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Coordinates o
        && x == o.x
        && y == o.y;
  }

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
  
}
