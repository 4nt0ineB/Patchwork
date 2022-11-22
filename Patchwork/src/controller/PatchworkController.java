package controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import model.game.Game;
import model.game.GameMode;
import model.game.InGameAction;
import model.game.component.Coordinates;
import model.game.component.gameboard.GameBoard;
import view.UserInterface;
import view.cli.CommandLineInterface;

public class PatchworkController {
  
  

  
  
  /**
   * Menu Loop that draw the menu and wait for the user to choose
   * his game mode 
   *
   * @param menu, cli
   * @return Choosen game mode
   */
  private static GameMode menu(UserInterface ui) {
  	ui.clear();
  	ui.drawMessages();
  	ui.display();
  	var choices = new LinkedHashSet<KeybindedChoice>();
  	choices.add(new KeybindedChoice('b', "The basic game"));
  	choices.add(new KeybindedChoice('f', "The full game"));
    GameMode mode = null;
    do {
     mode = switch(ui.getPlayerChoice(choices)) {
       case 'b' -> GameMode.PATCHWORK_BASIC;
       case 'f' -> GameMode.PATCHWORK_FULL;
       case -1 -> null;
       default -> throw new AssertionError("there shoulnd't be other possiblities");
     };
    }while(mode == null);
    return mode;
  }
    
  /**
   * MainLoop that draw the game and make users play the game
   *
   * @param ui, board
   * @return void
   */
  private static void mainLoop(UserInterface ui, GameBoard board) {
    InGameAction action = InGameAction.DEFAULT;
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
      while (board.nextPatchToPlay() != null) {
        switch((InGameAction) manipulatePatch(ui, board)){
          case BACK -> board.unselectPatch(); // Abandon this patch
          case PLACE -> board.playNextPatch(); // Add the patch to the quilt
          default -> {
            throw new AssertionError("There shouldn't be other choices");
          }
        }
      }
      board.eventQueue().forEach(e -> ui.draw(e));
      //board.runWaitingEvents();
    } while (action != InGameAction.QUIT && !board.isFinished());
    ui.close();
  }
  
  /**
   * Function that do the action the user select
   *
   * @param ui, board
   * @return Action
   */
  private static InGameAction doActionForTurn(UserInterface ui, GameBoard board) {
    var action = InGameAction.DEFAULT;
    var choices = new HashSet<KeybindedChoice>();
    var advance = new KeybindedChoice('a', "Advance");
    var select = new KeybindedChoice('s', "Select a patch");
    var quit = new KeybindedChoice('r', "Ragequit");
    if(board.currentPlayerCanAdvance()) {
      choices.add(advance);
    }
    if(board.currentPlayerCanSelectPatch()) {
      choices.add(select);
    }
    if(choices.isEmpty()) {
      return action;
    }
    choices.add(quit);
    ui.clear();
    switch (ui.getPlayerChoice(choices)) {
      case 's' -> {
        var patch = ui.selectPatch(board.availablePatches());
        if(patch == null) {
          return InGameAction.BACK; 
        }
        board.selectPatch(patch);
      }
      case 'a' -> board.currentPlayerAdvance();
      case 'r' -> action = InGameAction.QUIT;
      case -1 -> {}
      default -> throw new AssertionError("There shouldn't be other choices");
    }
    return action;
  }
  
  /**
   * Function that allows user to manipulate the given patch
   * on his quilt. Moving it in direction he wants if possible
   * Placing it on his quilt or even going back to the previous
   * Action
   *
   * @param ui, board, patch
   * @return Action
   */
  private static InGameAction manipulatePatch(UserInterface ui, GameBoard board) {
    // A list of actions
    // new KeybindChoice('r', "Ragequit", InGameAction.QUIT),
    var choices = new HashSet<KeybindedChoice>();
    var place = new KeybindedChoice('p', "Buy and place the patch");
    var rotateR = new KeybindedChoice('a', "rotate right");
    var rotateL = new KeybindedChoice('z', "rotate left");
    var up = new KeybindedChoice('s', "up");
    var down = new KeybindedChoice('w', "down");
    var right = new KeybindedChoice('d', "right");
    var left = new KeybindedChoice('q', "left");
    var back = new KeybindedChoice('b', "back");
    var action = InGameAction.DEFAULT;
    choices.addAll(Set.of(back, rotateR, rotateL));
    // We use a dummy quilt to play with the patch
    var patch = board.nextPatchToPlay();
    var quilt = board.currentPlayer().quilt();
    patch.absoluteMoveTo(new Coordinates(quilt.width() / 2, quilt.height() / 2));
    do {
      ui.clear();
      ui.draw(board);
      ui.drawMessages();
      ui.drawDummyQuilt(quilt, patch);
      ui.display();
      choices.removeAll(Set.of(up, down, left, right, place));
      if (quilt.canAdd(patch) && board.currentPlayer().canBuy(patch)) {
        choices.add(place);
      }
      if (patch.canMoveUp(quilt)) {
        choices.add(up);
      }
      if (patch.canMoveDown(quilt)) {
        choices.add(down);
      }
      if (patch.canMoveLeft(quilt)) {
        choices.add(left);
      }
      if (patch.canMoveRight(quilt)) {
        choices.add(right);
      }
      switch (ui.getPlayerChoice(choices)) {
        case 's'  -> patch.moveUp();
        case 'w' -> patch.moveDown();
        case 'd' -> patch.moveRight();
        case 'q' -> patch.moveLeft();
        case 'z' -> patch.rotateLeft();
        case 'a' -> patch.rotateRight();
        case 'p' -> { return InGameAction.PLACE; }
        case 'b' -> { action = InGameAction.BACK; }
        case -1 -> {}
        default -> { throw new AssertionError("There shouldn't be other choices"); }
      }
    } while (action != InGameAction.BACK);
    return action;
  }

  public static void main(String[] args) {
    var ui = new CommandLineInterface();
    Game game;
    try {
      game = Game.fromGameMode(menu(ui));
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
      return;
    }
    mainLoop(ui, game.gameBoard()); 
  }

}