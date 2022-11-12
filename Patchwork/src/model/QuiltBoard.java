package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import util.xml.XMLElement;
import view.cli.Color;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class QuiltBoard implements DrawableOnCLI {
  private final int width;
  private final int height;
  private final ArrayList<Patch> patches;
  
  public QuiltBoard(int width, int height) {
    if (width < 1 || height < 1) {
      throw new IllegalArgumentException("The QuiltBoard must be at least 1x1");
    }
    this.width = width;
    this.height = height;
    patches = new ArrayList<>();
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

  /**
   * Add a patch to the Quilt
   * 
   * @param patch
   * @return false if the given patch exceeds the borders or overlap a patch
   *         already on the Quilt, else true
   */
  public boolean add(Patch patch) {
    Objects.requireNonNull(patch, "can't add null obj as a patch");
    return canAdd(patch) && patches.add(patch);
  }

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
    return (width * height) - patches.stream().mapToInt(patch -> patch.countCells()).sum();
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
          builder.append(Color.ANSI_CYAN_BACKGROUND).append("x").append(Color.ANSI_RESET);
        } else {
          builder.append(" ");
        }
      }
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
   * Sum the patches buttons on the quilt
   * @return the sum
   */
  public int buttons() {
    return patches.stream().mapToInt(Patch::buttons).sum();
  }

  public static QuiltBoard fromText(String text) {
    Objects.requireNonNull(text, "Can't make new player out of null String");
    var parameters = text.replaceAll("[\\(\\)]", "").split(",");
    return new QuiltBoard(Integer.parseInt(parameters[0]), 
        Integer.parseInt(parameters[1]));
  }

  public static QuiltBoard fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    return new QuiltBoard(Integer.parseInt(element.getByTagName("width").content()),
        Integer.parseInt(element.getByTagName("height").content())
        );
  }

}
