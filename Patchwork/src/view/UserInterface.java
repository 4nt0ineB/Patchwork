package view;

import java.util.List;
import java.util.Set;

import model.Action;
import model.Patch;
import model.QuiltBoard;

public interface UserInterface {

  void drawSplashScreen();

  void draw(Drawable drawable);
  
  Patch selectPatch(List<Patch> patches);
  
  Action getPlayerAction(Set<Action> options);

  void drawDummyQuilt(QuiltBoard quilt, Patch patch);

  void drawMessages();
  void clearMessages();
  void display();
  void clear();
  
  /**
   * Close the interface
   */
  void close();

  

}
