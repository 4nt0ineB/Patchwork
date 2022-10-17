package view;

import model.Patch;

public interface UserInterface {
  
  void movePatch(Patch patch);
  void draw();
  void clear();
  Action choice();
  
  Action getPlayerActionForTurn();
  
  /**
   * Close the interface
   */
  void close();
  
}
