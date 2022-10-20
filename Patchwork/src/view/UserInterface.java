package view;

import java.util.List;

import model.GameBoard;
import model.Patch;
import model.QuiltBoard;

public interface UserInterface {
  
  void drawSplashScreen();
  void movePatch(Patch patch);
  void draw(GameBoard gb);
  void clear();
  Action choice();
  
  /**
   * Close the interface
   */
  void close();
  Action getPlayerActionForTurn(GameBoard gb, List<Action> options);
  void selectPatch(GameBoard gb);
  void drawDummyQuilt(QuiltBoard quilt, Patch patch);

}
