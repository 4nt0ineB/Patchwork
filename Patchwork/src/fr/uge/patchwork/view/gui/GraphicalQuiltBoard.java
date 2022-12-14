package fr.uge.patchwork.view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Objects;

import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.Patch;

public class GraphicalQuiltBoard {
  
  public final QuiltBoard board;
  private final Coordinates origin;
  private final int width;
  private final Color bgColor = new Color(140, 85, 52);
  private final double squareSide; // side of a square
  
  public GraphicalQuiltBoard(QuiltBoard board, int x, int y, int width) {
    this.board = Objects.requireNonNull(board);
    origin = new Coordinates(y, x);
    this.width = width;
    squareSide = width / board.width();
  }
  
  public void draw(GraphicalUserInterface ui) {
    drawQuilt(ui);
  }

  private void drawQuilt(GraphicalUserInterface ui) {
    drawQuiltBox(ui, (int) origin.x(), (int) origin.y(), width);
    drawPatches(ui);
    drawInfo(ui);
  }
  
  private void drawPatches(GraphicalUserInterface ui) {
    board.patches().forEach(p -> drawPatch(ui, p));
  }  
  
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
}
