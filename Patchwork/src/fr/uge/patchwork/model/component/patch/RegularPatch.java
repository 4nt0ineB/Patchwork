package fr.uge.patchwork.model.component.patch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
  public void flip() {
    patch.flip();
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
    return patch.canMoveDown(maxY);
  }

  @Override
  public boolean canMoveLeft(int minX) {
    return patch.canMoveLeft(minX);
  }

  @Override
  public boolean canMoveRight(int maxX) {
    return patch.canMoveRight(maxX);
  }

  @Override
  public Set<Coordinates> absoluteCoordinates() {
    return patch.absoluteCoordinates();
  }

  @Override
  public boolean overlap(Patch patch) {
    return this.patch.overlap(patch);
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
   
  
  public static List<RegularPatch> fromFile(Path path) throws IOException{
    var patches = new ArrayList<RegularPatch>();
    try (var reader = Files.newBufferedReader(path)) {
      String line;
      while((line = reader.readLine()) != null) {
        if(!line.isBlank()) {
          var values = line.split(",");
          var price = Integer.parseInt(values[0]);
          var moves = Integer.parseInt(values[1]);
          var buttons = Integer.parseInt(values[2]);
          var endOfDeclaration = false;
          var formAsTxt = "";
          do {
            line = reader.readLine();
            if(line == null || line.isBlank()) {
              endOfDeclaration = true;
            }else {
              formAsTxt += line + "\n";
            }
          }while(!endOfDeclaration);
          patches.add(
              new RegularPatch(price, 
                  moves, 
                  buttons,
                  Form.fromText(formAsTxt)));
        }
      }
    }
    return patches;
  }
  
  
  
}