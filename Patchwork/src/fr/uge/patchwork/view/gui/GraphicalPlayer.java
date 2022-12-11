package fr.uge.patchwork.view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Objects;

import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.Form;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;

public class GraphicalPlayer {
  
  public final Player player;
  private final Coordinates origin;
  private final int width;
  private final Color bgColor = new Color(140, 85, 52);
  private final double squareSide; // side of a square
  
  public GraphicalPlayer(Player player, int x, int y, int width) {
    this.player = Objects.requireNonNull(player);
    origin = new Coordinates(y, x);
    this.width = width;
    squareSide = width / player.quilt().width();
  }
  
  public void draw(GraphicalUserInterface ui) {
    // background
//    ui.addDrawingAction(g2 -> {
//      g2.setColor(bgColor);
//      g2.fillRect((int) origin.x, (int) origin.y, side, side);
//      g2.setColor(Color.BLACK);
//      g2.setStroke(new BasicStroke(1));
//      g2.drawRect((int) origin.x, (int) origin.y, side, side);
//    });
    var x = new RegularPatch(0,0,0, Form.fromText("oxx\nx\n\n"));
    x.absoluteMoveTo(new Coordinates(5, 5));
    player.quilt().add(x);
    drawQuilt(ui);
  }

  private void drawQuilt(GraphicalUserInterface ui) {
    drawQuiltBox(ui, (int) origin.x(), (int) origin.y(), width);
    drawPatches(ui);
  }
  
  private void drawPatches(GraphicalUserInterface ui) {
    player.quilt().patches().forEach(p -> drawPatch(ui, p));
  }  
  
  public void drawPatchAsDummy(GraphicalUserInterface ui, Patch patch) {
    ui.addDrawingAction(g2 -> {
      for(var coord: patch.absoluteCoordinates()) {
        g2.setColor(new Color(patch.hashCode()));
        if(player.quilt().occupied(coord)) {
          g2.setColor(Color.RED);
        }
        g2.fill(new Rectangle2D.Double(origin.x() + coord.x() * squareSide, 
          origin.y() + coord.y() * squareSide, squareSide, squareSide));
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
    });
  }
  

  private void drawQuiltBox(GraphicalUserInterface ui, int x, int y, int width) {
    ui.addDrawingAction(g2 -> {
      g2.setColor(bgColor);
      g2.fillRect(x, y, width, (int) (player.quilt().height() * squareSide));
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(2f));
      g2.drawRect(x, y, width, (int)  (player.quilt().height() * squareSide));
    });
  }
}
