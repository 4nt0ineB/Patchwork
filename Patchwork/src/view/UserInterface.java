package view;

import model.GameBoard;
import model.Patch;

public interface UserInterface {
  
  void movePatch(Patch patch);
  void draw(GameBoard gameBoard);
  void clear();
  Action choice();
  
  Action getPlayerActionForTurn();
  
  /**
   * Close the interface
   */
  void close();
  
}
