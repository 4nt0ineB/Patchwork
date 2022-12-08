package fr.uge.patchwork.model.component.patch;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

public record Form(Set<Coordinates> coordinates) {
    
  public Form {
    Objects.requireNonNull(coordinates);
    if(coordinates.isEmpty()) {
      throw new IllegalArgumentException("At least the relative "
          + "origin coordinates must be provided (0,0)");
    }
  }
  
  public Form flip() {
    return transform(Coordinates::swap);
  }
  
  public Form rotateRight() {
    if(isSquare()) {
      return new Form(coordinates);
    }
    return transform(Coordinates::rotateClockwise);
  }
  
  public Form rotateLeft() {
    if(isSquare()) {
      return new Form(coordinates);
    }
    return transform(Coordinates::rotateAntiClockwise);
  }
  
  private Form transform(UnaryOperator<Coordinates> operation) {
    Objects.requireNonNull(operation);
    return new Form(coordinates.stream()
        .map(operation)
        .collect(toSet()));
  }
  
  public int countCoordinates() {
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
  
  
  public static Form fromText(String txt) {
     Objects.requireNonNull(txt, "Text representation can't be null");
     if(!txt.contains("o")) {
       throw new IllegalArgumentException("No relative origin defined");
     }
     var coordinates = new HashSet<Coordinates>();
     Coordinates origin = null;
     var currentCoord = new Coordinates(0,0);
     for(var i = 0; i < txt.length(); i++) {
       var c = txt.charAt(i);
       currentCoord = switch(c) {
         case '\n' -> new Coordinates(currentCoord.y() + 1, 0);
         case ' '  -> currentCoord.add(new Coordinates(0, 1));
         default   -> {
           currentCoord = currentCoord.add(new Coordinates(0, 1));
           coordinates.add(currentCoord);
           yield currentCoord;
         }
       };
       if(c == 'o') { // its the origin, the coordinates
         origin = currentCoord;
       }
     }
     var relativeOrigin = origin;
     // translate back all coordinates with the origin
     return new Form(coordinates.stream()
         .map(c -> c.sub(relativeOrigin))
         .collect(toSet()));
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
