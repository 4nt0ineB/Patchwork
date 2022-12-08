package fr.uge.patchwork.model.component.patch;

import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import fr.uge.patchwork.model.component.Coordinates;

public record Form(Set<Coordinates> coordinates) {
    
  public Form {
    Objects.requireNonNull(coordinates);
  }
  

  public Form flip() {
    // @Todo
    return null;
  }
  
  public Form rotateRight() {
    return transform(Coordinates::rotateClockwise);
  }
  
  public Form rotateLeft() {
    return transform(Coordinates::rotateAntiClockwise);
  }
  
  private Form transform(UnaryOperator<Coordinates> operation) {
    Objects.requireNonNull(operation);
    return new Form(coordinates.stream()
        .map(operation)
        .collect(toSet()));
  }
  
  private int countCoordinates() {
    return coordinates.size();
  }
  
  /**
   * Check if cells form a square
   * @param cells
   * @return
   */
  public boolean isSquare() {
    var side = Math.sqrt(coordinates.size());
    if(side * side != coordinates.size()) {
      // number of cells not enough for square
      return false;
    }
    // the vector with which the origin must form the expected square
    var vector = farthestCoordinates()
        .mul(new Coordinates(((int) side) - 1, ((int) side) - 1));
    var origin = new Coordinates(0, 0);
    var c1 = origin;
    var c2 = vector;
    // set proper direction to test presence
    if(vector.x() < origin.x()) {
      c1 = vector;
      c2 = origin;
    }
    // Check if all coordinates are in the expected square
    for(var coord: coordinates) {
      if(!coord.inRectangle(c1, c2)) {
        return false;
      }
    }
    return true;
  }
  
  
  
  /**
   * Return the farthest coordinates from origin (0,0)
   * @param cells
   * @return
   */
  private Coordinates farthestCoordinates() {
    var farthest = new Coordinates(0, 0);
    var rOrigin = new Coordinates(0, 0);
    for(var c: coordinates) {
      if(c.distance(rOrigin) > farthest.distance(rOrigin)){
        farthest = c;
      }
    }
    return farthest;
  }
  
  
}
