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
  Action getPlayerActionForTurn(GameBoard gb);
  Action letPlayerTryPatch(GameBoard gb);
  
  /**
   * Interface to let the player select a patch
   * @param patches
   * @return the index of the selected patch
   */
  int letPlayerSelectPatch(List<Patch> patches);
}
