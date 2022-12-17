package fr.uge.patchwork.view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.RegularPatch;

/**
 * 
 * Graphical layer of a patch manager implementation
 * for a graphical user interface.
 *
 */
public class GraphicalPatchManager {
  private final PatchManager manager;
  private final Coordinates origin;
  private final int width;
  private final int height;
  private final int shown; // max patch to display
  private final int squareSide;
  private RegularPatch toEnhance;
  
  public GraphicalPatchManager(PatchManager manager, int shown, int x, int y, int width, int height) {
    this.manager = Objects.requireNonNull(manager);
    origin = new Coordinates(y, x);
    if(width < 0 || height < 0) {
      throw new IllegalArgumentException("Width and height can't be negative");
    }
    this.width = width;
    this.height = height;
    this.shown = Math.max(0, Math.min(shown,  manager.numberOfPatches()));
    squareSide = Math.max(width, height) / shown;
  }
  
  /**
   * Draw the patch manager on a graphical user interface
   * @param ui a graphical user interface
   */
  public void draw(GraphicalUserInterface ui) {
    ui.addDrawingAction(g2 -> {
      g2.setStroke(new BasicStroke(2f));
      g2.setColor(Color.DARK_GRAY);
      g2.drawRect(origin.x(), origin.y(), width, height);
    });
    drawPatches(ui);
  }
  
  private void drawPatches(GraphicalUserInterface ui) {
    var x = origin.x();
    var y = origin.y() +  (int) (squareSide * 0.1);
    var patches = manager.patches(shown);
    for(var i = patches.size() - 1; i >= 0; i--) {
      drawPatch(ui, patches.get(i), x, y, squareSide);
      if(width > height) {
        x += squareSide;
      }else {
        y += squareSide;
      }
    }
  }
  
  /**
   * Set a patch to be enhanced
   * @param patch
   */
  public void enhance(RegularPatch patch) {
    Objects.requireNonNull(patch, "Can't enhance null");
    toEnhance = patch;
  }
  
  private void drawPatch(GraphicalUserInterface ui, RegularPatch patch, int x, int y, int side) {
    var width = 1;
    var height = 1;
    do {
      width += 1;
      height += 1;
      patch.absoluteMoveTo(new Coordinates(height / 2, width / 2));     
    }while(!patch.fits(width, height));
    // draw the patch
    var quilt = new QuiltBoard(width, height);
    var quiltSide = side / 2;
    new GraphicalQuiltBoard(quilt, x + quiltSide, y, quiltSide).drawPatch(ui, patch);
    // info
    var fontSize = (int) (squareSide * 0.1);
    var font = new Font("", Font.BOLD, (int) (squareSide * 0.1));
    ui.addDrawingAction(g2 -> {
      g2.setFont(font);
      g2.drawString("Price "+ patch.price(), x + quiltSide+ squareSide, y + squareSide / 2);
      g2.drawString("Moves "+ patch.moves(), x + quiltSide+ squareSide, y + squareSide / 2 + fontSize);
      g2.drawString((patch.buttons() > 0? "\nButtons " + patch.buttons() : "")
          , x + quiltSide+ squareSide,
          y + squareSide / 2 + fontSize * 2);
    });
    if(toEnhance == patch) { // we compare pointers here
      var rect = new Rectangle2D.Double(x ,  y + squareSide / 4, squareSide / 4, squareSide / 6);
      var color = new Color(0, 149, 186);
      // triangle
      int[] xs = {(int) rect.x + (int) rect.width, 0, 0};
      xs[1] = xs[0];
      xs[2] = xs[1] + (int) squareSide / 4;
      int[] ys = {(int) rect.y - (int) rect.height, 0, 0};
      ys[1] = ys[0] + (int) rect.height * 3;
      ys[2] = (int) (ys[0] + ys[1]) / 2;
      var triangle = new Polygon(xs, ys ,3);
      ui.addDrawingAction(g2 -> {
        g2.setColor(color);
        g2.fill(rect);
        g2.fill(triangle);
      });
    }
  }
  
}
