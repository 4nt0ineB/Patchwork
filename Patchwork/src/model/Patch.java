package model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Patch {
  
  private static final Coordinates[] RLMATRIX = { new Coordinates(0, -1), new Coordinates(1, 0) };
  private static final Coordinates[] RRMATRIX = { new Coordinates(0, 1), new Coordinates(-1, 0) };
  
  // buttons in case of income
  private final int buttons; 
  // number of move to execute for the player if the patch is placed on his quilt
  private final int move; 
  // the price of the patch
  private final int price;
  // Absolute origin on the QuiltBoard associated to the relative origin of the patch (0,0)
  private Coordinates absoluteOrigin;
  // Index of the current state of rotation
  private int currentRotation;
  // All unique possible rotations for the patch
  private final List<Set<Coordinates>> rotations;
  
  /**
   * Patch constructor
   * @param buttons number of buttons associated with the patch, for potential button income
   * @param move the number of move a player can do after placing the path on his quilt
   * @param price the price of the patch
   * @param Point list of cells (Point) of the patch. 
   * The (y=0,x=0) coordinate is relative origin of the patch
   * For example, this patch :
   * <pre>
   *  --- ---
   * |   | x |
   *  --- ---
   *     |   |
   *      ---
   * </pre>
   * might be given as [(0, -1), (0, 0), (1, 0)].
   */
  public Patch(int buttons, int move, int price, List<Coordinates> coordinates, Coordinates absoluteOrigin) {
    if(buttons < 0) {
      throw new IllegalArgumentException("Buttons can't be negative");
    }
    if(move < 0) {
      throw new IllegalArgumentException("Move can't be negative");
    }
    if(price < 0) {
      throw new IllegalArgumentException("The price can't be negative");
    }
     Objects.requireNonNull(coordinates, "Must provide a list of coordinates");
    if(coordinates.size() == 0) {
      throw new IllegalArgumentException("The patch must be at least one pair of coordinates");
    }
    this.buttons = buttons;
    this.move = move;
    this.price = price;
    this.absoluteOrigin = absoluteOrigin;
    currentRotation = 0;
    this.rotations = allRotations(Set.copyOf(coordinates));
  }
  
  /**
   * 90° left rotation
   */
  public Patch Left() {
    currentRotation = (currentRotation - 1) % rotations.size();
    rotations.get(currentRotation);
    return this;
  }
  
  /**
   * 90° right rotation
   */
  public Patch rotateRight() {
    currentRotation = (currentRotation + 1) % rotations.size();
    rotations.get(currentRotation);
    return this;
  }
  
  /**
   * Decrement by one the absolute coordinates along y axis
   */
  public void moveUp() {
    absoluteOrigin = absoluteOrigin.sub(new Coordinates(1, 0));
  }
  
  /**
   * Increment by one the absolute coordinates along y axis
   */
  public void moveDown() {
    absoluteOrigin = absoluteOrigin.add(new Coordinates(1, 0));
  }
  
  /**
   * Decrement by one the absolute coordinates along x axis
   */
  public void moveLeft() {
    absoluteOrigin = absoluteOrigin.sub(new Coordinates(0, 1));
  }
  
  /**
   * Increment by one the absolute coordinates along x axis
   */
  public void moveRight() {
    absoluteOrigin = absoluteOrigin.add(new Coordinates(0, 1));
  }
  
  public int countCells() {
    return rotations.get(currentRotation).size();
  }
  
  /**
   * Return a set of the absolute positions of the patch cells
   * @return
   */
  public Set<Coordinates> absolutePositions(){
    return new HashSet<>(rotations.get(currentRotation).stream().map(c -> c.add(absoluteOrigin)).toList());
  }
  
  /**
   * Return true if patch overlap an other patch
   * @param patch
   * @return true if overlap, else false 
   */
  public boolean overlap(Patch patch) {
    Objects.requireNonNull(patch, "Can't test overlapping on null");
    var patchCells = patch.absolutePositions();
    for(var cell: absolutePositions()) {
      for(var ocell: patchCells) {
        if(cell.equals(ocell)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * check if the absolute coordinates of the 
   * patch fits in a defined rectangle<br>
   * 
   * [0;width[<br>
   * [0;height[
   * 
   * @param width
   * @param height
   * @return false if its not fitting, else true
   */
  public boolean fits(int width, int height) {
    if(width < 1 || height < 1) {
      throw new IllegalArgumentException("The rectangle dimensions must be at least of 1x1");
    }
    var topleft = new Coordinates(0, 0);
    var lowerright = new Coordinates(height, width);
    for(var cell: rotations.get(currentRotation)) {
      var absPos = cell.add(absoluteOrigin);
      if(!absPos.inRectangle(topleft, lowerright)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Check if cells form a square
   * @param cells
   * @return
   */
  private boolean isSquare(Set<Coordinates> cells) {
    var side = Math.sqrt(cells.size());
    if(side * side != cells.size()) {
      // number of cells could not form a square
      return false;
    }
    // the vector with which the square must expand from origin to form expected square
    var vector = farthestCoordinates(cells).mul(new Coordinates(((int) side) - 1, ((int) side) - 1));
    var rOrigin = new Coordinates(0, 0);
    // Check if all coordinates are in the expected square
    for(var c: cells) {
      if(!c.inRectangle(vector, rOrigin)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Return the farthest coordinates to origin (0,0)
   * @param cells
   * @return
   */
  private Coordinates farthestCoordinates(Set<Coordinates> cells) {
    var farthest = new Coordinates(0, 0);
    var rOrigin = new Coordinates(0, 0);
    for(var c: cells) {
      if(c.distance(rOrigin) > farthest.distance(rOrigin)){
        farthest = c;
      }
    }
    return farthest;
  }
  
  /**
   * Return a list of set of coordinates as all
   * possible rotations of a given set of coordinates (the cells of the patch)
   * @param cells a rotation state of a patch
   * @return
   */
  private List<Set<Coordinates>> allRotations(Set<Coordinates> cells) {
    var rotationsList = new ArrayList<Set<Coordinates>>();
    rotationsList.add(cells);
    // if square, no rotations
    if(isSquare(cells)) {
      return rotationsList;
    }
    for(var i = 1; i < 4; i++) {
      var r = rotate(rotationsList.get(i - 1), RRMATRIX);
      if(!rotationsList.contains(r)) {
        rotationsList.add(r);
      }
    }
    return rotationsList;
  }
  
  /**
   * Affine transformation of a set of coordinates (the patch cells)
   *  by a list of Coordinates
   * @param cells
   * @param vector
   * @return
   */
  private Set<Coordinates> rotate(Set<Coordinates> cells, Coordinates rotMatrix[]) {
    var newCells =  cells.stream().map(cell -> {
      var newc = new Coordinates(
          rotMatrix[0].y() * cell.y() + rotMatrix[0].x() * cell.x(), 
          rotMatrix[1].y() * cell.y() + rotMatrix[1].x() * cell.x()
          );
      return newc;
    }).toList();
    return new HashSet<>(newCells);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buttons, move, price, rotations);
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Patch o
        && buttons == o.buttons
        && move == o.move
        && price == o.price
        && rotations.contains(o.rotations.get(o.currentRotation))
        ;
  }

  @Override
  public String toString() {
    return rotations.get(currentRotation) 
        + " (p: "+ price 
        + ", m: " + move 
        + ", b: " + buttons + ") "
        + "AbsOrigin: " + absoluteOrigin
        ;
  }

  public int price() {
    return price;
  }
  
  
}