package fr.uge.patchwork.view.gui;

import static java.util.stream.Collectors.groupingBy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.gameboard.event.EventType;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.player.Player;
import fr.uge.patchwork.model.component.player.automa.Automa;

/**
 * 
 * Graphical layer of a track board implementation
 * for a graphical user interface.
 *
 */
public class GraphicalTrackBoard {
  
  private final TrackBoard board;
  private final Point2D.Double origin;
  private final int side; // graphical side length of the board 
  private final int squaresOnSide; // number of square by each side;
  private final double squareSide; // side of a square
  private final Color bgColor = new Color(140, 85, 52);
  private final Color trackStartColor = new Color(134, 149, 69);
  private final int offset;
  
  private final LinkedList<Color> spaceColors = new LinkedList<>();
  private final LinkedList<Shape> spaceSquares = new LinkedList<>();
  
  public GraphicalTrackBoard(int x, int y, int side, TrackBoard trackBoard) {
    board = Objects.requireNonNull(trackBoard);
    if (x < 0 || y < 0) {
    	throw new IllegalArgumentException("Coords can't be negatives");
    }
    origin = new Point2D.Double(x, y);
    this.side = side;
    squaresOnSide = (int) Math.sqrt(nextPerfectSquare(board.spaces()));
    squareSide = side / squaresOnSide;
    offset = (int) (squaresOnSide * squaresOnSide - board.spaces() - 1);
    updateSpaces();
  }
  
  /**
   * Draw the track board on a graphical user interface
   * @param ui a graphical user interface
   */
  public void draw(GraphicalUserInterface ui) {
    // background
    ui.addDrawingAction(g2 -> {
      g2.setColor(bgColor);
      g2.fillRect((int) origin.x, (int) origin.y, side, side);
      g2.setColor(Color.BLACK);
      g2.setStroke(new BasicStroke(1));
      g2.drawRect((int) origin.x, (int) origin.y, side, side);
    });
    drawTrackBoardSpaces(ui);
    drawButtons(ui);
    drawPatches(ui);
    drawPlayers(ui);
  }  

  private void drawTrackBoardSpaces(GraphicalUserInterface ui) {
    var stroke = new BasicStroke(1.8f);
    ui.addDrawingAction(g2 -> {
      for(var i = 0; i < spaceSquares.size(); i++) {
        g2.setColor(spaceColors.get(i));
        g2.fill(spaceSquares.get(i));
        g2.setStroke(stroke);
        g2.setPaint(Color.BLACK);
        g2.draw(spaceSquares.get(i));
      }
    });
  }
  
  private void drawPlayer(GraphicalUserInterface ui, Player player, double x, double y) {
    var tokenWidth = squareSide * 0.7;
    var tokenHeight = squareSide * 0.3;
    var bottomSide = new Ellipse2D.Double(x, y + tokenHeight, tokenWidth, tokenHeight);
    var topSide = new Ellipse2D.Double(x, y + tokenHeight - 5, tokenWidth, tokenHeight);
    ui.addDrawingAction(g2 -> {
      if(player.isAutonomous()) {
        var automa = (Automa) player;
        if(!automa.specialTile()) {
          var specialPatchPos = posToPoint(board.spaces() - automa.difficulty().spaces() + offset);
          g2.setColor(new Color(160, 84, 51));
          g2.setFont(new Font("Arial", Font.BOLD, (int) squareSide / 2));
          g2.drawString("S", (int) (specialPatchPos.x + squareSide / 2)
              , (int) (specialPatchPos.y + squareSide / 2));
        } 
      }
      // bottom
      g2.setColor(Color.GRAY);
      g2.fill(bottomSide);
      g2.setColor(Color.BLACK);
      g2.drawOval((int) x, (int) (y + tokenHeight), (int) tokenWidth, (int) tokenHeight);
      // top
      g2.setColor(Color.GRAY);
      g2.fill(topSide);
      g2.setColor(Color.BLACK);
      g2.drawOval((int) x, (int) (y + tokenHeight - 5), (int) tokenWidth, (int) tokenHeight);
      // name
      g2.setFont(new Font("Arial", Font.BOLD, 15));
      g2.drawString(player.name(), (int) (x + 2), (int) (y + squareSide / 2));
      
    });
  }
  
  private void drawPlayers(GraphicalUserInterface ui) {
    var players = board.players().stream().collect(groupingBy(Player::position));
    for(var position: players.entrySet()) {
      var coord = posToCoordinates(position.getKey() + offset);
      var squareOrigin = coordinatesToPoint(coord.x(), coord.y());
      for(var player: position.getValue()) {
        drawPlayer(ui, player, squareOrigin.x, squareOrigin.y);
        squareOrigin = new Point2D.Double(squareOrigin.x, squareOrigin.y - 10);
      }
    }
  }
  
  private void drawButton(GraphicalUserInterface ui, Event button) {
  	var coord = posToCoordinates(button.position() + offset);
  	var squareOrigin = coordinatesToPoint(coord.x(), coord.y());
  	var buttonX = squareOrigin.x + squareSide / 8;
  	var buttonY = squareOrigin.y + squareSide / 8;
  	var holeSize = squareSide / 24;
  	var buttonSide = squareSide / 20;
    var ellipses = new LinkedList<Shape>(List.of(
        new Ellipse2D.Double(buttonX + 1.3 * buttonSide, buttonY + 1.2 * buttonSide, holeSize, holeSize),
        new Ellipse2D.Double(buttonX + 2.8 * buttonSide, buttonY + 2.9 * buttonSide, holeSize, holeSize),
        new Ellipse2D.Double(buttonX + 1.3 * buttonSide, buttonY + 2.9 * buttonSide, holeSize, holeSize)
        ,new Ellipse2D.Double(buttonX + 2.8 * buttonSide, buttonY + 1.2 * buttonSide, holeSize, holeSize)
        ));
  	ui.addDrawingAction(g2 -> {
  		g2.setColor(new Color(67, 165, 198));
  		g2.fill(new Ellipse2D.Double(buttonX, buttonY, squareSide / 4, squareSide / 4));
  		g2.setColor(new Color(47, 115, 138));
  		ellipses.forEach(g2::fill);
  	});
  }
  
  private void drawButtons(GraphicalUserInterface ui) {
  	board.events().stream()
  		.filter(e -> e.type().equals(EventType.BUTTON_INCOME))
  		.forEach(e -> drawButton(ui, e));
  }
  
  private void drawPatch(GraphicalUserInterface ui, Event patch) {
  	var coord = posToCoordinates(patch.position() + offset);
  	var squareOrigin = coordinatesToPoint(coord.x(), coord.y());
  	ui.addDrawingAction(g2 -> {
  		g2.setColor(new Color(78, 62, 21));
  		g2.fill(new Rectangle2D.Double(squareOrigin.x + squareSide / 16, 
  		    squareOrigin.y + squareSide / 16, squareSide / 4 + squareSide / 8, squareSide / 4 + squareSide / 8));
  		g2.setColor(new Color(97, 78, 26));
  		g2.fill(new Rectangle2D.Double(squareOrigin.x + squareSide / 8, 
  		    squareOrigin.y + squareSide / 8, squareSide / 4, squareSide / 4));
  	});
  }
  
  private void drawPatches(GraphicalUserInterface ui) {
  	board.events().stream()
  		.filter(e -> e.type().equals(EventType.PATCH_INCOME))
  		.forEach(p -> drawPatch(ui, p));
  }
  
  /**
   * Update the spaces data, shape and color
   */
  private void updateSpaces() { 
    var color = trackStartColor;
    var totalSpaces = board.spaces() + offset;
    for(var space = offset; space < totalSpaces; space++) {
      var squareOrigin = posToPoint(space);
      var hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
      color = new Color(Color.HSBtoRGB((float) ((space * 100) / totalSpaces) / 100, hsb[1], hsb[2]));
      spaceColors.add(color);
      spaceSquares.add(new Rectangle2D.Double(squareOrigin.x, squareOrigin.y, squareSide, squareSide));
    }
  }
  
  /**
   * Transform a track board position into indexes 
   * on its 2d array representation
   * @param space
   * @return
   */
  public Point2D.Double posToPoint(int space){
    var coord = posToCoordinates(space);
    return coordinatesToPoint(coord.x(), coord.y());
  }
  
  /**
   * Compute the graphical point of the square for 
   * given board 2D array index
   * @param coordinates
   * @param i x
   * @param j y
   * @return
   */
  public Point2D.Double coordinatesToPoint(int i, int j){
    return new Point2D.Double(origin.x + squareSide * j, origin.y + squareSide * i);
  }
  
  private Coordinates posToCoordinates(int pos) {
    var start = new Coordinates(squaresOnSide - 1 , squaresOnSide - 1);
    var advance = pos; // how many spaces to advance
    int directionI = 0;
    Coordinates[] turnCycle = {
        new Coordinates(-1, 0),
        new Coordinates(0, -1),
        new Coordinates(1, 0),
        new Coordinates(0, 1)
    };
    int ncorners = (squaresOnSide % 2 == 0) ? (squaresOnSide / 2) * 4 - 1 
        : ((int) squaresOnSide / 2) * 4 - ((int) squaresOnSide / 2);
    var corners = new int[ncorners + 1]; // store computed remaining spaces before new corner (at each corner)
    int currentCorner = 0;
    // set the remaining square before getting 
    // to the next first corner (bottom right square)
    corners[currentCorner] = squaresOnSide - 1;
    var leftBeforeCorner = corners[currentCorner];
    while(advance != 0) {
      start = start.add(turnCycle[directionI]);
      advance--;
      leftBeforeCorner--;
      if(leftBeforeCorner == 0) {
        currentCorner += 1;
        directionI = (directionI + 1) % 4;
        if(currentCorner < 3) { // 3 corners with equal distance at start
          corners[currentCorner] = squaresOnSide - 1;
        }else if(corners[currentCorner - 1] == corners[currentCorner - 2]) { 
          // we passed two corners, the next one its at same distance but minus 1
          corners[currentCorner] = corners[currentCorner - 1] - 1;
        }else {
          // passed the 3rd corner, its only 2 corners at equal distance now
          corners[currentCorner] = corners[currentCorner - 1];
        }
        leftBeforeCorner = corners[currentCorner];
      }
    }
    return start;
  }
  
  /**
   * Return the next perfect square greater than a given number
   * @param x
   * @return
   */
  private int nextPerfectSquare(int x) {
    int upperx = (int) Math.floor(Math.sqrt(x)) + 1;
    return upperx * upperx;
  }
  
}
