package fr.uge.patchwork.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import fr.uge.patchwork.model.Game;
import fr.uge.patchwork.model.GameMode;
import fr.uge.patchwork.model.component.Coordinates;
import fr.uge.patchwork.model.component.gameboard.GameBoard;
import fr.uge.patchwork.view.UserInterface;
import fr.uge.patchwork.view.cli.CommandLineInterface;

public class PatchworkController {
  
  /**
   * Menu Loop that draw the menu and wait for the user to choose
   * his game mode 
   *
   * @param ui the user interface
   * @return Choosen game mode
   */
  public static GameMode menu(UserInterface ui) {
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
   * @param ui the user interface
   * @param board the game board
   * @return true if ends normally, 
   * otherwise false, if the player asked to quit during the game
   */
  public static boolean run(UserInterface ui, GameBoard board) {
    Objects.requireNonNull(ui, "The interface can't be null");
    Objects.requireNonNull(ui, "The game board can't be null");
    var action = PlayerAction.DEFAULT;
    while(action != PlayerAction.QUIT && !board.isFinished()) { // -- Game loop
      if(board.nextTurn()){
        ui.clearMessages();
      }    
      /*board already drawn in this function so it was printing it twice*/
      action = doActionForTurn(ui, board);
      while (board.nextPatchToPlay().isPresent()) {
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
    return action.equals(PlayerAction.QUIT) ? false : true;
  }
  
  /**
   * Function that do the action the user select
   *
   * @param ui the user interface
   * @param board the game board
   * @return Action
   */
  private static PlayerAction doActionForTurn(UserInterface ui, GameBoard board) {
    var action = PlayerAction.DEFAULT;
    var choices = new HashSet<KeybindedChoice>();
    if(board.playerCanAdvance()) {
      choices.add(new KeybindedChoice('a', "Advance"));
    }
    if(board.playerCanSelectPatch()) {
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
        if(patch.isPresent()) {
          board.selectPatch(patch.get());
        }
        return PlayerAction.BACK; 
      }
      case 'a' -> board.playerAdvance();
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
   * @param ui 
   * @param board
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
    var patch = board.nextPatchToPlay().get();
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
  public static boolean endGame(UserInterface ui, GameBoard gameBoard) {
    Objects.requireNonNull(ui, "The interface can't be null");
    Objects.requireNonNull(ui, "The game board can't be null");
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
    // chose ui from arg in phase 3
    var userInterface = new CommandLineInterface();
    Game game;
    do {
      try {
        game = Game.fromGameMode(menu(userInterface));
      } catch (IOException e) {
        System.err.println(e.getMessage());
        userInterface.close();
        System.exit(1);
        return;
      }
    }while(run(userInterface, game.gameBoard())
        && endGame(userInterface, game.gameBoard()));
    userInterface.close();
  }

}