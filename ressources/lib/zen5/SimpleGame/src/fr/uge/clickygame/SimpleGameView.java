package fr.uge.clickygame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import fr.umlv.zen5.ApplicationContext;

public class SimpleGameView{
	private final int xOrigin;
	private final int yOrigin;
	private final int width;
	private final int length;
	private final int squareSize;

	private SimpleGameView(int xOrigin, int yOrigin, int length, int width, int squareSize) {
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.length = length;
		this.width = width;
		this.squareSize = squareSize;
	}

	public static SimpleGameView initGameGraphics(int xOrigin, int yOrigin, int length, SimpleGameData data) {
		int squareSize = (int) (length * 1.0 / data.getNbLines());
		return new SimpleGameView(xOrigin, yOrigin, length, data.getNbColumns()*squareSize, squareSize);
	}

	private int indexFromReaCoord(float coord, int origin) { // attention, il manque des test de validité des coordonnées!
		return (int) ((coord - origin) / squareSize);
	}

	/**
	 * Transforms a real y-coordinate into the index of the corresponding line.
	 * @param y a float y-coordinate
	 * @return the index of the corresponding line.
	 * @throws IllegalArgumentException if the float coordinate doesn't fit in the game board.
	 */
	public int lineFromY(float y) {
		return indexFromReaCoord(y, yOrigin);
	}
	
	/**
	 * Transforms a real x-coordinate into the index of the corresponding column.
	 * @param x a float x-coordinate
	 * @return the index of the corresponding column.
	 * @throws IllegalArgumentException if the float coordinate doesn't fit in the game board.
	 */
	public int columnFromX(float x) {
		return indexFromReaCoord(x, xOrigin);
	}

	private float realCoordFromIndex(int index, int origin) {
		return origin + index * squareSize;
	}

	private float xFromI(int i) {
		return realCoordFromIndex(i, xOrigin);
	}

	private float yFromJ(int j) {
		return realCoordFromIndex(j, yOrigin);
	}

	private RectangularShape drawCell(int i, int j) {
		return new Rectangle2D.Float(xFromI(j) + 2, yFromJ(i) + 2, squareSize - 4, squareSize - 4);
	}

	private RectangularShape drawSelectedCell(int i, int j) {
		return new Rectangle2D.Float(xFromI(j), yFromJ(i), squareSize, squareSize);
	}
	
	/**
	 * Draws the game board from its data, using an existing Graphics2D object.
	 * @param graphics a Graphics2D object provided by the default method {@code draw(ApplicationContext, GameData)}
	 * @param data the GameData containing the game data.
	 */
	private void draw(Graphics2D graphics, SimpleGameData data) {
		// example
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fill(new Rectangle2D.Float(xOrigin, yOrigin, width, length));

		graphics.setColor(Color.WHITE);
		for (int i = 0; i <= data.getNbLines(); i++) {
			graphics.draw(
					new Line2D.Float(xOrigin, yOrigin + i * squareSize, xOrigin + width, yOrigin + i * squareSize));
		}

		for (int i = 0; i <= data.getNbColumns(); i++) {
			graphics.draw(
					new Line2D.Float(xOrigin + i * squareSize, yOrigin, xOrigin + i * squareSize, yOrigin + length));
		}

		Coordinates c = data.getSelected();
		if (c != null) {
			graphics.setColor(Color.BLACK);
			graphics.fill(drawSelectedCell(c.i(), c.j()));
		}

		for (int i = 0; i < data.getNbLines(); i++) {
			for (int j = 0; j < data.getNbColumns(); j++) {
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.fill(drawCell(i, j));
				graphics.setColor(data.getCellColor(i, j));
				graphics.drawString(Integer.toString(data.getCellValue(i, j)), xFromI(j) + squareSize / 2,
						yFromJ(i) + +squareSize / 2);
			}
		}
	}
	
	/**
	 * Draws the game board from its data, using an existing {@code ApplicationContext}.
	 * @param context the {@code ApplicationContext} of the game
	 * @param data the GameData containing the game data.
	 * @param view the GameView on which to draw.
	 */
	public static void draw(ApplicationContext context, SimpleGameData data, SimpleGameView view) {
		context.renderFrame(graphics -> view.draw(graphics, data)); // do not modify
	}
	
	/**
	 * Draws only the cell specified by the given coordinates in the game board from its data, using an existing Graphics2D object.
	 * @param graphics a Graphics2D object provided by the default method {@code draw(ApplicationContext, GameData)}
	 * @param data the GameData containing the game data.
	 * @param x the float x-coordinate of the cell.
	 * @param y the float y-coordinate of the cell.
	 */
	private void drawOnlyOneCell(Graphics2D graphics, SimpleGameData data, float x, float y) {
		// to do
	}
	
	/**
	 * Draws only the cell specified by the given coordinates in the game board from its data, using an existing {@code ApplicationContext}.
	 * @param context the {@code ApplicationContext} of the game
	 * @param data the GameData containing the game data.
	 * @param view the GameView on which to draw.
	 * @param x the float x-coordinate of the cell.
	 * @param y the float y-coordinate of the cell.
	 */
	public static void drawOnlyOneCell(ApplicationContext context, SimpleGameData data, SimpleGameView view, float x, float y) {
		context.renderFrame(graphics -> view.drawOnlyOneCell(graphics, data, x, y)); // do not modify
	}
}
