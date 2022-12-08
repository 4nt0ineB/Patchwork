package fr.uge.patchwork.model.component.patch;

import java.util.Objects;
import java.util.Set;

import fr.uge.patchwork.model.component.Coordinates;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

/**
 * 
 * Provides an implementation of a patch
 *
 */
public final class RegularPatch implements Patch, DrawableOnCLI {
  
  // buttons in case of income
  private final int buttons; 
  // number of move to execute for the player if the patch is placed on his quilt
  private final int moves; 
  // the price of the patch (in buttons)
  private final int price; 
  private final Patch2D patch;
  
  public RegularPatch(int price, int moves, int buttons, Form form) {
    if(buttons < 0) {
      throw new IllegalArgumentException("Buttons can't be negative");
    }
    if(moves < 0) {
      throw new IllegalArgumentException("Moves can't be negative");
    }
    if(price < 0) {
      throw new IllegalArgumentException("The price can't be negative");
    }
    this.buttons = buttons;
    this.moves = moves;
    this.price = price;
    patch = new Patch2D(form);
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
  
  public int price() {
    return price;
  }
  
  @Override
  public String toString() {
    return 
        " (p: "+ price 
        + ", m: " + moves 
        + ", b: " + buttons + ") "
        + patch
        ;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buttons, moves, price, patch);
  }
  
  /**
   * Make new patch from a XMLElement
   * @param element
   * @return
   */
  public static RegularPatch fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    var price = Integer.parseInt(element.getByTagName("price").content());
    var moves = Integer.parseInt(element.getByTagName("moves").content());
    var buttons = Integer.parseInt(element.getByTagName("buttons").content());
    var coordinatesList = element.getByTagName("coordinatesList")
        .getAllByTagName("Coordinates").stream().map(Coordinates::fromXML).toList();
    return new RegularPatch(price, moves, buttons, coordinatesList);
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    ui.builder().append("[")
    .append("Price: ").append(price)
    .append(", Moves: ").append(moves)
    .append(", Buttons: ").append(buttons)
    .append("]\n\n");
    patch.drawOnCLI(ui);
  }

  @Override
  public Form form() {
    return patch.form();
  }
  
  @Override
  public void rotateLeft() {
    patch.rotateLeft();
  }

  @Override
  public void rotateRight() {
    patch.rotateRight();
  }

  @Override
  public void moveUp() {
    patch.moveUp();
  }

  @Override
  public void moveDown() {
    patch.moveDown();
  }

  @Override
  public void moveLeft() {
    patch.moveLeft();
  }

  @Override
  public void moveRight() {
    patch.moveRight();
  }

  @Override
  public boolean canMoveUp(int miny) {
    return patch.canMoveUp(miny);
  }

  @Override
  public boolean canMoveDown(int maxY) {
    return patch.canMoveUp(maxY);
  }

  @Override
  public boolean canMoveLeft(int minX) {
    return patch.canMoveUp(minX);
  }

  @Override
  public boolean canMoveRight(int maxX) {
    return patch.canMoveUp(maxX);
  }

  @Override
  public Set<Coordinates> absoluteCoordinates() {
    return patch.absoluteCoordinates();
  }

  @Override
  public boolean overlap(RegularPatch patch) {
    return patch.overlap(patch);
  }

  @Override
  public boolean fits(int width, int height) {
    return patch.fits(width, height);
  }

  @Override
  public boolean meets(Coordinates coordinates) {
    return patch.meets(coordinates);
  }

  @Override
  public void absoluteMoveTo(Coordinates coordinates) {
    patch.absoluteMoveTo(coordinates);
  }
  
}