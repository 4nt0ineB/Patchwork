package fr.uge.patchwork.view.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import fr.uge.patchwork.model.component.QuiltBoard;

public class GraphicalQuiltBoard {
	private final Point2D.Double origin;
	private final QuiltBoard qb;
	private final int squareHeight;
	private final int squareWidth; /*Might change names because not a square if window not a square ?*/
	private final int widthOffset;
	private final int heightOffset;
	
	public GraphicalQuiltBoard(int x, int y, int windowHeight, int windowWidth, QuiltBoard qb) {
		this.qb = Objects.requireNonNull(qb);
		if (x < 0 || y < 0 ) {
    	throw new IllegalArgumentException("Coords can't be negatives");
    }
		if (windowHeight < 0 || windowWidth < 0) {
			throw new IllegalArgumentException("Window can't be negative height");
		}
		this.origin = new Point2D.Double(x, y);
		this.widthOffset = windowHeight / 4;
		this.heightOffset = windowWidth / 4;	/*drawing of quiltboard takes half of screen place*/
		this.squareHeight = (windowHeight - this.widthOffset) / this.qb.height(); 
		this.squareWidth = (windowWidth - this.heightOffset) / this.qb.width();
	}
	
	public void draw(GraphicalUserInterface ui) {
		/* TODO */
		Objects.requireNonNull(ui);
		drawQuiltBoard(ui);
	}
	
	private void drawPatches(GraphicalUserInterface ui) {
		/* TODO */
	}
	
	private void drawQuiltBoard(GraphicalUserInterface ui) {
		/* TODO */
		var stroke = new BasicStroke(1.8f);
		ui.addDrawingAction(g2 -> {
			// BackGround
			g2.setColor(Color.GRAY);
			g2.fill(new Rectangle2D.Double(origin.x, origin.y, squareHeight * qb.height(), squareWidth * qb.width()));
			// 
			g2.setColor(Color.BLACK);
			var offset = stroke.getLineWidth();
			g2.draw(new Line2D.Double(origin.x - offset, origin.y - offset, origin.x - offset, squareHeight * qb.height() + offset));
			g2.draw(new Line2D.Double(origin.x - offset, origin.y - offset, squareWidth * qb.width() + offset, origin.y + offset));
			g2.draw(new Line2D.Double(squareWidth * qb.width() + offset, squareHeight * qb.height() + offset, squareWidth * qb.width() - offset, origin.y - offset));
			g2.draw(new Line2D.Double(squareWidth * qb.width() + offset, squareHeight * qb.height() + offset, origin.x - offset, squareHeight * qb.height() + offset));
		});
	}
	
	
}
