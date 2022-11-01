package model;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import view.cli.DisplayableOnCLI;
import view.cli.Color;

public class QuiltBoard implements DisplayableOnCLI {
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
    for (var p : patches) {
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
      if (patch.absolutePositions().contains(coordinates)) {
        return true;
      }
    }
    return false;
  }

  public Set<Patch> patches() {
    return Set.copyOf(patches);
  }

  public int width() {
    return width;
  }

  public int height() {
    return height;
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
  public void drawOnCLI() {
    var builder = new StringBuilder();
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

  public QuiltBoard clone() {
    var quilt = new QuiltBoard(width, height);
    patches.stream().forEach(patch -> quilt.add(patch));
    return quilt;
  }

}
