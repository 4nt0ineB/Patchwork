package view;

import java.util.List;

import model.GameBoard;
import model.Patch;

public interface UserInterface {
  
  void drawSplashScreen();
  void movePatch(Patch patch);
  void draw(GameBoard gameBoard);
  void clear();
  Action choice();
  
  /**
   * Close the interface
   */
  void close();
  Action getPlayerActionForTurn(GameBoard gb, List<Action> options);
  
  /**
   * 
   * @param gb
   * @return
   */
  boolean tryAndBuyPatch(GameBoard gb);
  
  void init();
}
