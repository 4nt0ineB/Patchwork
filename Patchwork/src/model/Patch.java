package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import model.button.ButtonValued;
import util.xml.XMLElement;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

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
   * Getter method for moves
   * @return int
   */
  public int moves() {
    return moves;
  }
  
  /**
   * Return the amount of button on the patch
   * @return 
   */
  public int buttons() {
    return buttons;
  }
  
  /**
   * Getter method for price
   * @return int
   */
  @Override
  public int value() {
    return price;
  }
  
  /**
   * Getter method for CurrentCoordinates
   * @return Set<Coordinates>
   */
  public Set<Coordinates> getCurrentCoordinates(){
  	return this.rotations.get(this.currentRotation);
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
  public void moveUp(QuiltBoard qb) {
  	if (this.canMoveUp(qb)) {
      absoluteOrigin = absoluteOrigin.sub(new Coordinates(1, 0));		
  	}
  }
  
  /**
   * Increment by one the absolute coordinates along y axis
   */
  public void moveDown(QuiltBoard qb) {
  	if (this.canMoveDown(qb)) {
  		absoluteOrigin = absoluteOrigin.add(new Coordinates(1, 0));
  	}
  }
  
  /**
   * Decrement by one the absolute coordinates along x axis
   */
  public void moveLeft(QuiltBoard qb) {
  	if  (this.canMoveLeft(qb)) {
  		absoluteOrigin = absoluteOrigin.sub(new Coordinates(0, 1));
  	}
  }
  
  /**
   * Increment by one the absolute coordinates along x axis
   */
  public void moveRight(QuiltBoard qb) {
  	if (this.canMoveRight(qb)) {
  	  absoluteOrigin = absoluteOrigin.add(new Coordinates(0, 1));
  	}
  }
  
  /**
   * Says if it's possible for the patch to move up
   * @return
   */
  public boolean canMoveUp(QuiltBoard qb) {
  	return (this.maxUpCoord() + this.absoluteOrigin.y() > 0);
  }
  
  /**
   * Says if it's possible for the patch to move down
   * @return
   */
  public boolean canMoveDown(QuiltBoard qb) {
  	return (this.maxDownCoord() + this.absoluteOrigin.y() < qb.height() - 1);
  }
  
  /**
   * Says if it's possible for the patch to move left
   * @return
   */
  public boolean canMoveLeft(QuiltBoard qb) {
  	return (this.maxLeftCoord() + this.absoluteOrigin.x() > 0);
  }
  
  /**
   * Says if it's possible for the patch to move right
   * @return
   */
  public boolean canMoveRight(QuiltBoard qb) {
  	return (this.maxRightCoord() + this.absoluteOrigin.x() < qb.width() - 1);
  }
  
  /**
   * Get the most left piece of the Patch
   * @return
   */
  private int maxLeftCoord() {
  	var maxLeft = 0; // case the absolute origin is the max left
  	var iterator = this.rotations.get(currentRotation).iterator();
  	while(iterator.hasNext()) {
  		var coordinates = iterator.next();
  		if (maxLeft > coordinates.x()) {
  			maxLeft = coordinates.x();
  		}
  	}
  	return maxLeft;
  }
  
  /**
   * Get the most right piece of the Patch
   * @return
   */
  private int maxRightCoord() {
  	var maxRight = 0; // case the absolute origin is the max right
  	var iterator = this.rotations.get(currentRotation).iterator();
  	while(iterator.hasNext()) {
  		var coordinates = iterator.next();
  		if (maxRight < coordinates.x()) {
  			maxRight = coordinates.x();
  		}
  	}
  	return maxRight;
  }
  
  /**
   * Get the most up piece of the Patch
   * @return
   */
  private int maxUpCoord() {
  	var maxUp = 0; // case the absolute origin is the max left
  	var iterator = this.rotations.get(currentRotation).iterator();
  	while(iterator.hasNext()) {
  		var coordinates = iterator.next();
  		if (maxUp > coordinates.y()) {
  			maxUp = coordinates.y();
  		}
  	}
  	return maxUp;
  }
  
  /**
   * Get the most down piece of the Patch
   * @return
   */
  private int maxDownCoord() {
  	var maxDown = 0; // case the absolute origin is the max down
  	var iterator = this.rotations.get(currentRotation).iterator();
  	while(iterator.hasNext()) {
  		var coordinates = iterator.next();
  		if (maxDown < coordinates.y()) {
  			maxDown = coordinates.y();
  		}
  	}
  	return maxDown;
  }
  
  /**
   * Count the number of coordinates in the patch
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
    return new HashSet<>(rotations.get(currentRotation).stream()
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
    rotationsList.add(new HashSet<>(cells));
    if(!isSquare(cells)) {
      // 3 rotations left
      var prevRotation = rotationsList.get(0);
      for(var i = 1; i < 4; i++) {
        var rotation = prevRotation.stream()
            .map(Coordinates::rotateClockwise)
            .collect(Collectors.toSet());
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

  /**
   * Make a special Patch of 1*1
   * 
   * @return patch
   */
  public static Patch getSpecialPatch() {
  	var listCoordinates = new ArrayList<Coordinates>();
  	listCoordinates.add(new Coordinates(0, 0));
  	return new Patch(0, 0, 0, listCoordinates);
  }
  
  
  /**
   * return if this patch is connected from one of his coordinates to the other patch
   * @param element
   * @return
   */
  public boolean isNeighbour(Patch other) {
  	Objects.requireNonNull(other, "Can't verify neighbour if other is null");
  	for (var otherCoordinate : other.absoluteCoordinates()) {
  		// if we find one Coordinate that is a neighbour then patches are neighbour 
  		// from this coordinates so we can return already.
  		var neighbourCount = this.absoluteCoordinates().stream()
    	.filter(coordinate -> coordinate.isNeighbour(otherCoordinate)).count();
  		if (neighbourCount != 0) {
  			return true;
  		}
  	}
  	return false;
  }
  
  /**
   * return if this patch is connected from one of his coordinates to the other patch
   * @param element
   * @return
   */
  public Patch mergePatch(Patch other) {
  	Objects.requireNonNull(other, "can't merge with null patch");
  	if (!this.isNeighbour(other)) {
  		// Can't merge two patches that aren't neighbours because
  		// that doesn't make sense at all
  		return null;
  	}
  	var mergedList = new ArrayList<Coordinates>();
  	mergedList.addAll(this.getCurrentCoordinates());
  	mergedList.addAll(other.getCurrentCoordinates().stream()
  			.map(coord -> new Coordinates(coord.x() + coord.distanceInX(this.absoluteOrigin), coord.y() + coord.distanceInY(this.absoluteOrigin))).toList());
  	return new Patch(this.value() + other.value() , this.moves() + other.moves(), this.buttons() + other.buttons(), mergedList);
  }
  
  /**
   * return if this patch is a 7*7 patch so if it's placed on a quilt board
   * the player should be given a special tile
   * 
   * @return boolean
   */
  public boolean isSpecialTileWorthy() {
  	for (var j = this.maxUpCoord(); j < this.maxDownCoord(); j++) {
  		for (var i = this.maxLeftCoord(); i < this.maxRightCoord(); i++) {
  		
    		// if for every (j, i) (cause coordinates are (y, x)) we have a patch coordinates then it's a 7*7 else return false
  			if (!this.getCurrentCoordinates().contains(new Coordinates(j, i))) {
  				return false;
  			}
    	}
  	}
  	return true;
  }
  
}