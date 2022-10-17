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
  
}
