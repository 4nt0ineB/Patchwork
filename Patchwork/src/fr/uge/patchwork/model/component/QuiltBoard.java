package fr.uge.patchwork.model.component;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.uge.patchwork.model.component.patch.Form;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.Color;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

public class QuiltBoard implements DrawableOnCLI {
  private final int width;
  private final int height;
  private ArrayList<Patch> patches = new ArrayList<>();
  private int buttons;
  
  
  public QuiltBoard(int width, int height) {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("The QuiltBoard must be at least 1x1");
    }
    this.width = width;
    this.height = height;
  }
  
  public List<Patch> patches() {
    return patches;
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
  }
  
  public boolean add(Patch patch) {
    if(canAdd(patch)) {
      patches.add(patch);
      return true;
    }
    return false;
  }

  /**
   * Add a patch to the Quilt
   * 
   * @param patch
   * @return false if the given patch exceeds the borders or overlap a patch
   *         already on the Quilt, else true
   */
  public void add(RegularPatch patch) {
    Objects.requireNonNull(patch, "can't add null obj as a patch");
    if(add((Patch) patch)) {
      buttons += patch.buttons();
    }
  }

  /**
   * Test if a patch can be added to the quilt 
   * considering his absolute position and rotation
   * @param patch
   * @return
   */
  public boolean canAdd(Patch patch) {
    // fits ?
    if (!patch.fits(width - 1, height - 1)) {
      return false;
    }
    // Overlap ?
    for (var p : patches()) {
      if (patch.overlap(p)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Test if the quilt has a filled square of size 'side'
   * @param side
   * @return 
   */
  public boolean hasFilledSquare(int side) {
    if(side < 1) {
      throw new IllegalArgumentException("The square must be at least 1x1");
    }
    var allCoordinates = patches.stream().
        flatMap(p -> p.absoluteCoordinates().stream())
        .collect(toSet());
    return allCoordinates.stream()
        .anyMatch(c -> {
              for(var i = c.y(); i < c.y() + side; i++) {
                for(var j = c.x(); j < c.x() + side; j++) {
                  if(!allCoordinates.contains(new Coordinates(i, j))) {
                    return false;
                  }
                }
              }
              return true;
        });
  }
  
  /**
   * Test if given coordinates is occupied by a cell of a patch on the quilt
   * 
   * @param coordinates
   * @return
   */
  public boolean occupied(Coordinates coordinates) {
    for (var patch : patches) {
      // @Todo delegate ?!
      if (patch.absoluteCoordinates().contains(coordinates)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Count the number of empty spaces on the Quilt
   * 
   * @return
   */
  public int countEmptySpaces() {
    return (width * height) - 
        patches.stream()
        .map(Patch::form)
        .mapToInt(Form::countCoordinates)
        .sum();
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    var builder = ui.builder();
    // top
    builder.append("┌");
    for (var i = 0; i < width; i++) {
      builder.append("─");
    }
    builder.append("┐\n");
    // body
    for (var y = 0; y < height; y++) {
      builder.append("|");
      for (var x = 0; x < width; x++) {
        if (occupied(new Coordinates(y, x))) {
          builder.append(Color.ANSI_CYAN_BACKGROUND).append("x");
        } else {
          builder.append(" ");
        }
      }
      builder.append(Color.ANSI_RESET);
      builder.append("|\n");
    }
    // bottom
    builder.append("└");
    for (var i = 0; i < width; i++) {
      builder.append("─");
    }
    builder.append("┘");
    System.out.println(builder);
  }
  
  /**
   * Sum the total of buttons on the quilt
   * @return the sum
   */
  public int buttons() {
    return buttons;
  }

  public static QuiltBoard fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    return new QuiltBoard(Integer.parseInt(element.getByTagName("width").content()),
        Integer.parseInt(element.getByTagName("height").content()));
  }

}
