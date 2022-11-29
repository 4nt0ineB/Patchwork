package controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import model.game.Game;
import model.game.GameMode;
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
  	var choices = new LinkedHashSet<KeybindedChoice>();
  	choices.add(new KeybindedChoice('b', "The basic game"));
  	choices.add(new KeybindedChoice('f', "The full game"));
    GameMode mode = null;
    do {
      ui.clear();
      ui.drawMessages();
      ui.display();
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
  private static PlayerAction mainLoop(UserInterface ui, GameBoard board) {
    PlayerAction action = PlayerAction.DEFAULT;
    while(action != PlayerAction.QUIT && !board.isFinished()) { // -- Game loop
      if(board.nextTurn()){
        ui.clearMessages();
      }
      ui.clear();
      ui.draw(board);
      ui.drawMessages();
      ui.display();      
      action = doActionForTurn(ui, board);
      while (board.nextPatchToPlay() != null) {
        switch((PlayerAction) manipulatePatch(ui, board)){
          case BACK -> board.unselectPatch(); // Abandon this patch
          case PLACE -> board.playNextPatch(); // Add the patch to the quilt
          default -> {
            throw new AssertionError("There shouldn't be other choices");
          }
        }
      }
      board.eventQueue().forEach(ui::draw);
    }
    return action;
  }
  
  /**
   * Function that do the action the user select
   *
   * @param ui, board
   * @return Action
   */
  private static PlayerAction doActionForTurn(UserInterface ui, GameBoard board) {
    var action = PlayerAction.DEFAULT;
    var choices = new HashSet<KeybindedChoice>();
    if(board.currentPlayerCanAdvance()) {
      choices.add(new KeybindedChoice('a', "Advance"));
    }
    if(board.currentPlayerCanSelectPatch()) {
      choices.add(new KeybindedChoice('s', "Select a patch"));
    }
    if(choices.isEmpty()) {
      return action;
    }
    choices.add(new KeybindedChoice('r', "Ragequit"));
    ui.clear();
    ui.draw(board);
    ui.drawMessages();
    ui.display();
    switch (ui.getPlayerChoice(choices)) {
      case 's' -> {
        var patch = ui.selectPatch(board.availablePatches());
        if(patch == null) {
          return PlayerAction.BACK; 
        }
        board.selectPatch(patch);
      }
      case 'a' -> board.currentPlayerAdvance();
      case 'r' -> action = PlayerAction.QUIT;
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
  private static PlayerAction manipulatePatch(UserInterface ui, GameBoard board) {
    var choices = new HashSet<KeybindedChoice>();
    var basicChoices = Set.of(
        new KeybindedChoice('b', "back"), 
        new KeybindedChoice('z', "rotate left"), 
        new KeybindedChoice('a', "rotate right"));
    choices.addAll(basicChoices);
    // We use a dummy quilt to play with the patch
    var patch = board.nextPatchToPlay();
    var quilt = board.currentPlayer().quilt();
    patch.absoluteMoveTo(new Coordinates(quilt.width() / 2, quilt.height() / 2));
    var action = PlayerAction.DEFAULT;
    do {
      ui.clear();
      ui.draw(board);
      ui.drawMessages();
      ui.drawDummyQuilt(quilt, patch);
      ui.display();
      choices.clear();
      choices.addAll(basicChoices);
      if (quilt.canAdd(patch) && board.currentPlayer().canBuy(patch)) {
        choices.add(new KeybindedChoice('p', "Buy and place the patch"));
      }
      if (patch.canMoveUp(quilt)) {
        choices.add(new KeybindedChoice('s', "up"));
      }
      if (patch.canMoveDown(quilt)) {
        choices.add(new KeybindedChoice('w', "down"));
      }
      if (patch.canMoveLeft(quilt)) {
        choices.add(new KeybindedChoice('q', "left"));
      }
      if (patch.canMoveRight(quilt)) {
        choices.add(new KeybindedChoice('d', "right"));
      }
      switch (ui.getPlayerChoice(choices)) {
        case 's'  -> patch.moveUp();
        case 'w' -> patch.moveDown();
        case 'd' -> patch.moveRight();
        case 'q' -> patch.moveLeft();
        case 'z' -> patch.rotateLeft();
        case 'a' -> patch.rotateRight();
        case 'p' -> { return PlayerAction.PLACE; }
        case 'b' -> { action = PlayerAction.BACK; }
        case -1 -> {}
        default -> { throw new AssertionError("There shouldn't be other choices"); }
      }
    } while (!action.equals(PlayerAction.BACK));
    return action;
  }
  
  /**
   * End game loop
   * @param ui
   * @param gameBoard
   * @return true if want a new game, otherwise false
   */
  private static boolean endGame(UserInterface ui, GameBoard gameBoard) {
    var choices = Set.of(
        new KeybindedChoice('q', "Quit"), 
        new KeybindedChoice('n', "New game"));
    var choice = -1;
    ui.clear();
    ui.draw(gameBoard);
    ui.drawMessages();
    ui.display(); 
    do {
      switch (ui.getPlayerChoice(choices)) {
        case 'q' -> { return false; }
        case 'n' -> { return true; } 
        case -1 -> {}
        default -> { throw new AssertionError("There shouldn't be other choices"); }
      };
    }while(choice == -1);
    return false;
  }
  

  public static void main(String[] args) {
    // chose ui from arg
    var ui = new CommandLineInterface();
    Game game;
    do {
      try {
        game = Game.fromGameMode(menu(ui));
      } catch (IOException e) {
        System.err.println(e.getMessage());
        ui.close();
        System.exit(1);
        return;
      }
      game.gameBoard().init();
    }while(!mainLoop(ui, game.gameBoard()).equals(PlayerAction.QUIT)
        && endGame(ui, game.gameBoard()));
    ui.close();
  }

}