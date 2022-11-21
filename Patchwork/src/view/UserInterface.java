package view;

import java.util.List;
import java.util.Set;

import controller.KeybindedChoice;
import model.game.component.Patch;
import model.game.component.QuiltBoard;

public interface UserInterface {

  void drawSplashScreen();

  void draw(Drawable drawable);
  
  Patch selectPatch(List<Patch> patches);
  
  int getPlayerChoice(Set<KeybindedChoice> choices);

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
