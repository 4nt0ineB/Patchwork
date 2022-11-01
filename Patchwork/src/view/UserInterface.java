package view;

import java.util.Set;

import controller.Action;
import model.GameBoard;
import model.Patch;
import model.QuiltBoard;

public interface UserInterface {

  void drawSplashScreen();

  void draw(GameBoard gb);

  void clear();

  Action choice();

  /**
   * Close the interface
   */
  void close();

  Action getPlayerActionForTurn(GameBoard gb, Set<Action> options);

  void selectPatch(GameBoard gb);

  void drawDummyQuilt(QuiltBoard quilt, Patch patch);

}
