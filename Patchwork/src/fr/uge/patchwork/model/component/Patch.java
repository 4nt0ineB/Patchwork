package fr.uge.patchwork.model.component;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import fr.uge.patchwork.model.component.button.ButtonValued;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

/**
 * 
 * Provides an implementation of a patch
 *
 */
public class Patch implements ButtonValued, DrawableOnCLI {
  
  // buttons in case of income
  private final int buttons; 
  // number of move to execute for the player if the patch is placed on his quilt
  private final int moves; 
  // the price of the patch (in buttons)
  private final int price; 
  // Absolute origin on the QuiltBoard associated to the relative origin of the patch (0,0)
  private Coordinates absoluteOrigin = new Coordinates(0, 0);
  // Index of the current  of rotation
  private int currentRotation;
  // All unique possible rotations for the patch
  private final List<Set<Coordinates>> rotations;
  
  /**
   * Patch constructor
   * @param buttons number of buttons associated with the patch, for potential button income
   * @param moves the number of move a player can do after placing the path on his quilt
   * @param price the price of the patch
   * @param coordinates list of cells (Point) of the patch. 
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
  public Patch(int price, int moves, int buttons, List<Coordinates> coordinates) {
    if(buttons < 0) {
      throw new IllegalArgumentException("Buttons can't be negative");
    }
    if(moves < 0) {
      throw new IllegalArgumentException("Moves can't be negative");
    }
    if(price < 0) {
      throw new IllegalArgumentException("The price can't be negative");
    }
     Objects.requireNonNull(coordinates, "Must provide a list of coordinates");
    if(coordinates.size() == 0) {
      throw new IllegalArgumentException("The patch must be at least one pair of coordinates");
    }
    this.buttons = buttons;
    this.moves = moves;
    this.price = price;
    currentRotation = 0;
    this.rotations = allRotations(new HashSet<>(coordinates));
  }
  
  /**
   * @return the number of moves granted
   */
  public int moves() {
    return moves;
  }
  
  /**
   * @return the amount of button on the patch
   */
  public int buttons() {
    return buttons;
  }
  

  @Override
  public int value() {
    return price;
  }
  
  /**
   * access CurrentCoordinates
   * @return
   */
  public Set<Coordinates> currentCoordinates(){
  	return rotations.get(currentRotation);
  }
  /**
   * 90° left rotation
   */
  public Patch rotateLeft() {
    currentRotation = Math.floorMod(currentRotation - 1, rotations.size());
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
  
  /**
   * Says if it's possible for the patch to move up
   * @return
   */
  public boolean canMoveUp(QuiltBoard quilt) {
  	return currentCoordinates().stream()
  	    .map(Coordinates::y)
  	    .min(Integer::compare)
  	    .get() 
  	    + absoluteOrigin.y() > 0;
  }
  
  /**
   * Says if it's possible for the patch to move down
   * @return
   */
  public boolean canMoveDown(QuiltBoard quilt) {
  	return currentCoordinates().stream()
        .map(Coordinates::y)
        .max(Integer::compare)
        .get() 
        + absoluteOrigin.y() < quilt.height() - 1;
  }
  
  /**
   * Says if it's possible for the patch to move left
   * @return
   */
  public boolean canMoveLeft(QuiltBoard quilt) {
  	return currentCoordinates().stream()
        .map(Coordinates::x)
        .min(Integer::compare)
        .get() 
        + absoluteOrigin.x() > 0;
  }
  
  /**
   * Says if it's possible for the patch to move right
   * @return
   */
  public boolean canMoveRight(QuiltBoard quilt) {
  	return currentCoordinates().stream()
        .map(Coordinates::x)
        .max(Integer::compare)
        .get() 
        + absoluteOrigin.x() < quilt.width() - 1;
  }
  
  /**
   * Count the number of cells in the patch
   * @return
   */
  public int countCells() {
    return rotations.get(currentRotation).size();
  }
  
  /**
   * Return a set of the absolute positions of the patch cells
   * @return
   */
  public Set<Coordinates> absoluteCoordinates(){
    return new HashSet<>(
        rotations.get(currentRotation).stream()
        .map(c -> c.add(absoluteOrigin))
        .toList());
  }
  
  /**
   * Return true if patch overlap an other patch
   * @param patch
   * @return true if overlap, else false 
   */
  public boolean overlap(Patch patch) {
    Objects.requireNonNull(patch, "Can't test overlapping on null");
    for(var cell: patch.absoluteCoordinates()) {
      if(meets(cell)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Check if any cell of the patch 
   * is at a given pair of coordinates
   * @param coordinates
   * @return
   */
  public boolean meets(Coordinates coordinates) {
    return absoluteCoordinates().contains(coordinates);
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
      // number of cells not enough for square
      return false;
    }
    // the vector with which the origin must form the expected square
    var vector = farthestCoordinates(cells)
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
    for(var cell: cells) {
      if(!cell.inRectangle(c1, c2)) {
        return false;
      }
    }
    return true;
  }
  
  public boolean isSquare() {
    return isSquare(rotations.get(currentRotation));
  }
  
  /**
   * Return the farthest coordinates from origin (0,0)
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
    rotationsList.add(new HashSet<>(cells));
    if(!isSquare(cells)) {
      // 3 rotations left
      var prevRotation = rotationsList.get(0);
      for(var i = 1; i < 4; i++) {
        var rotation = prevRotation.stream()
            .map(Coordinates::rotateClockwise)
            .collect(toSet());
        if(!rotationsList.contains(rotation)) {
          rotationsList.add(rotation);
          prevRotation = rotation;
        }
      }
    }
    return rotationsList;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buttons, moves, price, rotations);
  }
  
  /**
   * Test equality on fields
   * @param obj
   * @return
   */
  public boolean sameAs(Object obj) {
    return obj instanceof Patch o
        && buttons == o.buttons
        && moves == o.moves
        && price == o.price
        && rotations.contains(o.rotations.get(o.currentRotation));
  }
  
  /**
   * Move the absolute origin of the patch to the given coordinates
   * @param coordinates
   */
  public void absoluteMoveTo(Coordinates coordinates) {
    absoluteOrigin = coordinates;
  }

  @Override
  public String toString() {
    return rotations.get(currentRotation) 
        + " (p: "+ price 
        + ", m: " + moves 
        + ", b: " + buttons + ") "
        + "AbsOrigin: " + absoluteOrigin
        ;
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    // We use a conceptual square to deal with absolute coordinates.
    // While the patch doesn't fit in, we expand the square
    // and replace the origin of the patch at the center of it
    var width = 2;
    var height = 2;
    while(!this.fits(width, height)) {
      absoluteMoveTo(new Coordinates(height / 2, width / 2));
      width += 1;
      height += 1;
    }
    // draw the patch
    ui.builder().append("[")
    .append("Price: ").append(price)
    .append(", Moves: ").append(moves)
    .append(", Buttons: ").append(buttons)
    .append("]\n\n");
    for(var y = 0; y < height; y++) {
      ui.builder().append("  ");
      for(var x = 0; x < width; x++) {
        if(absoluteCoordinates().contains(new Coordinates(y, x))) {
          ui.builder().append("x");
        }else {
          ui.builder().append(" ");
        }
      }
      ui.builder().append("\n");
    }
  }
  
  /**
   * Make new patch from a XMLElement
   * @param element
   * @return
   */
  public static Patch fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    var price = Integer.parseInt(element.getByTagName("price").content());
    var moves = Integer.parseInt(element.getByTagName("moves").content());
    var buttons = Integer.parseInt(element.getByTagName("buttons").content());
    var coordinatesList = element.getByTagName("coordinatesList")
        .getAllByTagName("Coordinates").stream().map(Coordinates::fromXML).toList();
    return new Patch(price, moves, buttons, coordinatesList);
  }
  
}