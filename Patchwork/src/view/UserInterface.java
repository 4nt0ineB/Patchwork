package view;

import java.util.List;
import java.util.Set;

import model.Action;
import model.Patch;
import model.QuiltBoard;
import model.gameboard.GameBoard;
import model.gameboard.event.Event;

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

  void drawEvents(List<Event> eventQueue);

  void display();

  void clearMessages();

  void drawMessages();

  

}
