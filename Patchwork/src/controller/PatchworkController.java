package controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import model.Action;
import model.Coordinates;
import model.Patch;
import model.gameboard.GameBoard;
import util.xml.XMLParser;
import view.UserInterface;
import view.cli.Color;
import view.cli.CommandLineInterface;

public class PatchworkController {
  
  private static LinkedHashSet<Action> patchActions = new LinkedHashSet<Action>(
      Set.of(Action.UP, Action.DOWN, Action.RIGHT, Action.LEFT, Action.ROTATE_LEFT,
          Action.ROTATE_RIGHT));

  private static void menu(UserInterface ui, GameBoard board) {
    // var actions = List.of(new Action(""));
  }
    
  private static void mainLoop(UserInterface ui, GameBoard board) {
    var action = Action.DEFAULT;
    board.init();
    do { // -- Game loop
      if(board.nextTurn()){
        ui.clearMessages();
      }
      ui.clear();
      ui.draw(board);
      ui.drawMessages();
      ui.display();      
      action = doActionForTurn(ui, board);
      // The player has patches to place
      while (board.nextPatchToPlay() != null) {
        switch(manipulatePatch(ui, board, board.nextPatchToPlay())){
          case BACK -> { // Abandon this patch
            board.unselectPatch();
          }
          case PLACE -> { // Add the patch to the quilt
            board.playNextPatch();
          }
          default -> {
            throw new AssertionError("Their shouldn't be other choices");
          }
        }
      }
      board.eventQueue().forEach(e -> ui.draw(e));
      board.runWaitingEvents();
    } while (action != Action.QUIT && !board.isFinished());
    ui.close();
  }
  
  private static Action doActionForTurn(UserInterface ui, GameBoard board) {
    var action = Action.DEFAULT;
    var options = new LinkedHashSet<Action>(board.availableActions());
    if(options.isEmpty()) {
      return action;
    }
    options.add(Action.QUIT);
    action = ui.getPlayerAction(options);
    ui.clear();
    switch (action) {
      case SELECT_PATCH -> {
        var patch = ui.selectPatch(board.availablePatches());
        if(patch == null) {
          return Action.BACK; 
        }
        board.selectPatch(patch);
      }
      case ADVANCE -> board.currentPlayerAdvance();
      default -> {}
    }
    return action;
  }
  
  private static Action manipulatePatch(UserInterface ui, GameBoard board, Patch patch) {
    // A list of actions
    var action = Action.DEFAULT;
    var actions = new LinkedHashSet<>(patchActions);
    actions.add(Action.BACK);
    // We use a dummy quilt to play with the patch
    var quilt = board.currentPlayer().quilt();
    patch.absoluteMoveTo(new Coordinates(quilt.width() / 2, quilt.height() / 2));
    do {
      ui.clear();
      ui.draw(board);
      ui.drawMessages();
      ui.drawDummyQuilt(quilt, patch);
      ui.display();
      actions.remove(Action.PLACE);
      if (quilt.canAdd(patch) && board.currentPlayer().canBuy(patch)) {
        actions.add(Action.PLACE);
      }
      actions.remove(Action.UP);
      actions.remove(Action.DOWN);
      actions.remove(Action.LEFT);
      actions.remove(Action.RIGHT);
      if (patch.canMoveUp(quilt)) {
      	actions.add(Action.UP);
      }
      if (patch.canMoveDown(quilt)) {
      	actions.add(Action.DOWN);
      }
      if (patch.canMoveLeft(quilt)) {
      	actions.add(Action.LEFT);
      }
      if (patch.canMoveRight(quilt)) {
      	actions.add(Action.RIGHT);
      }
      
      switch (ui.getPlayerAction(actions)) {
        case UP -> patch.moveUp(quilt);
        case DOWN -> patch.moveDown(quilt);
        case RIGHT -> patch.moveRight(quilt);
        case LEFT -> patch.moveLeft(quilt);
        case ROTATE_LEFT -> patch.rotateLeft();
        case ROTATE_RIGHT -> patch.rotateRight();
        case PLACE -> { return Action.PLACE; }
        case BACK -> { action = Action.BACK; }
        case DEFAULT -> {}
        default -> { throw new AssertionError("Their shouldn't be other choices"); }
      }
    } while (action != Action.BACK);
    return action;
  }

  public static void main(String[] args) {
    // var path = Path.of("resources/settings/patchwork_full.xml");
    var path = Path.of("resources/settings/patchwork_full.xml");
    try {
      var xmlParser = new XMLParser();
      var xmlElement = xmlParser.parse(path);
      var board = GameBoard.fromXML(xmlElement);
      mainLoop(new CommandLineInterface(), board);
    } catch (IOException e) {
      System.err.println("Error while trying to make game board from " + path);
      System.err.println(e.getMessage());
      System.exit(1);
      return;
    }
 
  }

}