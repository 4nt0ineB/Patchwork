package fr.uge.patchwork.model.component.patch;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import fr.uge.patchwork.model.component.Coordinates;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

public final class Patch2D implements Patch, DrawableOnCLI {
  // Absolute origin on the plan associated to the relative origin of the patch
  // (0,0)
  private Coordinates absoluteOrigin = new Coordinates(0, 0);
  // All unique possible rotations for the patch
  private Form form;

  /**
   * Patch constructor
   * 
   * @param buttons     number of buttons associated with the patch, for potential
   *                    button income
   * @param moves       the number of move a player can do after placing the path
   *                    on his quilt
   * @param price       the price of the patch
   * @param coordinates list of cells (Point) of the patch. The (y=0,x=0)
   *                    coordinate is relative origin of the patch For example,
   *                    this patch :
   * 
   *                    <pre>
   *  --- ---
   * |   | x |
   *  --- ---
   *     |   |
   *      ---
   *                    </pre>
   * 
   *                    might be given as [(0, -1), (0, 0), (1, 0)].
   */
  public Patch2D(Form form) {
    Objects.requireNonNull(form, "Must provide a a form");
    this.form = form;
  }

  @Override
  public Form form() {
    return form;
  }

  /**
   * 90° left rotation
   */
  @Override
  public void rotateLeft() {
    form = form.rotateLeft();
  }

  /**
   * 90° right rotation
   */
  @Override
  public void rotateRight() {
    form = form.rotateRight();
  }

  /**
   * Decrement by one the absolute coordinates along y axis
   */
  @Override
  public void moveUp() {
    absoluteOrigin = absoluteOrigin.sub(new Coordinates(1, 0));
  }

  /**
   * Increment by one the absolute coordinates along y axis
   */
  @Override
  public void moveDown() {
    absoluteOrigin = absoluteOrigin.add(new Coordinates(1, 0));
  }

  /**
   * Decrement by one the absolute coordinates along x axis
   */
  @Override
  public void moveLeft() {
    absoluteOrigin = absoluteOrigin.sub(new Coordinates(0, 1));
  }

  /**
   * Increment by one the absolute coordinates along x axis
   */
  @Override
  public void moveRight() {
    absoluteOrigin = absoluteOrigin.add(new Coordinates(0, 1));
  }

  /**
   * Says if it's possible for the patch to move up
   * 
   * @return
   */
  @Override
  public boolean canMoveUp(int miny) {
    return form.coordinates().stream().map(Coordinates::y).min(Integer::compare).get() + absoluteOrigin.y() > miny;
  }

  /**
   * Says if it's possible for the patch to move down
   * 
   * @return
   */
  @Override
  public boolean canMoveDown(int maxY) {
    return form.coordinates().stream().map(Coordinates::y).max(Integer::compare).get() + absoluteOrigin.y() < maxY - 1;
  }

  /**
   * Says if it's possible for the patch to move left
   * 
   * @return
   */
  @Override
  public boolean canMoveLeft(int minX) {
    return form.coordinates().stream().map(Coordinates::x).min(Integer::compare).get() + absoluteOrigin.x() > minX;
  }

  /**
   * Says if it's possible for the patch to move right
   * 
   * @return
   */
  @Override
  public boolean canMoveRight(int maxX) {
    return form.coordinates().stream().map(Coordinates::x).max(Integer::compare).get() + absoluteOrigin.x() < maxX - 1;
  }

  /**
   * Return a set of the absolute positions of the patch cells
   * 
   * @return
   */
  @Override
  public Set<Coordinates> absoluteCoordinates() {
    return new HashSet<>(form.coordinates().stream().map(c -> c.add(absoluteOrigin)).toList());
  }

  /**
   * Return true if patch overlap an other patch
   * 
   * @param patch
   * @return true if overlap, else false
   */
  @Override
  public boolean overlap(Patch patch) {
    Objects.requireNonNull(patch, "Can't test overlapping on null");
    for (var cell : absoluteCoordinates()) {
      if (meets(cell)) {
        return true;
      }
    }
    return false;
  }

  /**
   * check if the absolute coordinates of the patch fits in a defined
   * rectangle<br>
   * 
   * [0;width[<br>
   * [0;height[
   * 
   * @param width
   * @param height
   * @return false if its not fitting, else true
   */
  @Override
  public boolean fits(int width, int height) {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("The rectangle dimensions must be at least of 1x1");
    }
    var topleft = new Coordinates(0, 0);
    var lowerright = new Coordinates(height, width);
    for (var cell : form.coordinates()) {
      var absPos = cell.add(absoluteOrigin);
      if (!absPos.inRectangle(topleft, lowerright)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if any coordinates in a list match a coordinates of the patch
   * 
   * @param coordinates
   * @return
   */
  @Override
  public boolean meets(Coordinates coordinates) {
    return absoluteCoordinates().contains(coordinates);
  }

  /**
   * Move the absolute origin of the patch to the given coordinates
   * 
   * @param coordinates
   */
  @Override
  public void absoluteMoveTo(Coordinates coordinates) {
    absoluteOrigin = coordinates;
  }

  @Override
  public String toString() {
    return form().coordinates() + "AbsOrigin: " + absoluteOrigin;
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    // We use a conceptual square to deal with absolute coordinates.
    // While the patch doesn't fit in, we expand the square
    // and replace the origin of the patch at the center of it
    var width = 2;
    var height = 2;
    while (!this.fits(width, height)) {
      absoluteMoveTo(new Coordinates(height / 2, width / 2));
      width += 1;
      height += 1;
    }
    // draw the patch
    for (var y = 0; y < height; y++) {
      ui.builder().append("  ");
      for (var x = 0; x < width; x++) {
        if (absoluteCoordinates().contains(new Coordinates(y, x))) {
          ui.builder().append("x");
        } else {
          ui.builder().append(" ");
        }
      }
      ui.builder().append("\n");
    }
  }


}
