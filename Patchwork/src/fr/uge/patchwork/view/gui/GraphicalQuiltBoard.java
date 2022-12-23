package fr.uge.patchwork.view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.Patch;

/**
 * 
 * Graphical layer of a quilt board implementation
 * for a graphical user interface.
 *
 */
public class GraphicalQuiltBoard {
  
  public final QuiltBoard board;
  private final Coordinates origin;
  private final int width;
  private final Color bgColor = new Color(140, 85, 52);
  private final double squareSide; // side of a square
  private Set<KeybindedChoice> infos = new HashSet<KeybindedChoice>();
  
  public GraphicalQuiltBoard(QuiltBoard board, int x, int y, int width) {
    this.board = Objects.requireNonNull(board);
    origin = new Coordinates(y, x);
    this.width = width;
    squareSide = width / board.width();
    initOptions();
  }
  
  /**
   * Draw the quilt board on a graphical user interface
   * @param ui a graphical user interface
   */
  public void draw(GraphicalUserInterface ui) {
    drawQuiltBox(ui, (int) origin.x(), (int) origin.y(), width);
    drawPatches(ui);
    drawInfo(ui);
  }
  
  private void drawPatches(GraphicalUserInterface ui) {
    board.patches().forEach(p -> drawPatch(ui, p));
  }  
  
  /**
   * Draw the quilt board on a graphical user interface
   * with a dummy patch. Useful for manipulating a patch
   * and moving it on the quilt without adding it to the patch
   * @param ui a graphical user interface
   */
  public void drawWithPatchAsDummy(GraphicalUserInterface ui, Patch patch) {
    ui.addDrawingAction(g2 -> {
      for(var coord: patch.absoluteCoordinates()) {
        g2.setColor(new Color(patch.hashCode()));
        if(board.occupied(coord)) {
          g2.setColor(Color.RED);
        }
        var rect = new Rectangle2D.Double(origin.x() + coord.x() * squareSide, 
            origin.y() + coord.y() * squareSide, squareSide, squareSide);
        g2.fill(rect);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(rect);
      }
    });
  }
  
  /**
   * Draw the patches of the quilt on a graphical user interface
   * @param ui a graphical user interface
   */
  public void drawPatch(GraphicalUserInterface ui, Patch patch) {
    var squares = new LinkedList<Shape>();
    for(var coord: patch.absoluteCoordinates()) {
      squares.add(new Rectangle2D.Double(origin.x() + coord.x() * squareSide, 
          origin.y() + coord.y() * squareSide, squareSide, squareSide));
    }
    ui.addDrawingAction(g2 -> {
      g2.setColor(new Color(patch.hashCode()));
      squares.forEach(g2::fill);
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(2.0f));
      squares.forEach(g2::draw);
    });
  }
  
  private void drawQuiltBox(GraphicalUserInterface ui, int x, int y, int width) {
    ui.addDrawingAction(g2 -> {
      g2.setColor(bgColor);
      g2.fillRect(x, y, (int) (width - width * 0.001), (int) (board.height() * squareSide));
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(2f));
      g2.drawRect(x, y, width, (int)  (board.height() * squareSide));

      
    });
  }
  
  private void drawInfo(GraphicalUserInterface ui) {
    var fontSize = (int) (squareSide * 0.3);
    var font = new Font("", Font.BOLD, fontSize);
    ui.addDrawingAction(g2 -> {
      g2.setFont(font);
      var txt = "Buttons : " + board.buttons();
      var txtWidth = g2.getFontMetrics().stringWidth(txt);
      g2.drawString(txt, origin.x() + width - txtWidth, origin.y() + width + 20);
    });
    
  }
  

  private void initOptions() {
    infos.add(new KeybindedChoice('p', "Place the patch"));
    infos.add(new KeybindedChoice('s', "up"));
    infos.add(new KeybindedChoice('w', "down"));
    infos.add(new KeybindedChoice('q', "left"));
    infos.add(new KeybindedChoice('d', "right"));
    infos.add(new KeybindedChoice('z', "rotate left"));
    infos.add(new KeybindedChoice('a', "rotate right"));
    infos.add(new KeybindedChoice('f', "flip"));
    infos.add(new KeybindedChoice('b', "back"));
  }
  
  public Set<KeybindedChoice> infos(){
  	return infos;
  }
  
  public Coordinates coords() {
  	return origin;
  }
  
  public int width() {
  	return width;
  }
}
