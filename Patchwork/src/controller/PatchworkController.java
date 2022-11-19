package controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import model.Action;
import model.Coordinates;
import model.Menu;
import model.MenuOption;
import model.Patch;
import model.gameboard.GameBoard;
import util.xml.XMLParser;
import view.UserInterface;
import view.cli.CommandLineInterface;

public class PatchworkController {
  
  private static LinkedHashSet<Action> patchActions = new LinkedHashSet<Action>(
      Set.of(Action.UP, Action.DOWN, Action.RIGHT, Action.LEFT, Action.ROTATE_LEFT,
          Action.ROTATE_RIGHT));

  /**
   * Menu Loop that draw the menu and wait for the user to choose
   * his game mode 
   *
   * @param menu, cli
   * @return Choosen game mode
   */
  private static MenuOption menuLoop(Menu menu, CommandLineInterface cli) {
  	Objects.requireNonNull(menu, "Can't do menu loop if menu given is null");
  	Objects.requireNonNull(cli, "Can't do menu loop if cli given is null");
  	cli.draw(menu);
  	cli.drawMessages();
    cli.display();
    var optionChoosed = cli.selectMenuOption(menu.getMenuOptions());
    while (optionChoosed == null) {
			cli.clear();
			cli.draw(menu);
			cli.drawMessages();
			cli.display();
			optionChoosed = cli.selectMenuOption(menu.getMenuOptions());
		}
    return optionChoosed;
  }
    
  /**
   * MainLoop that draw the game and make users play the game
   *
   * @param ui, board
   * @return void
   */
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
            // Patch added to the quilt we merge all the quilt patches
            board.currentPlayer().quilt().mergeAllPatches();
            // Look if the placed Patch permits the user to get the special tile
            // Only if special tile not already given
            if (board.specialTile()) {
            	// Special tile not already given
            	board.giveSpecialTileToPlayer(board.currentPlayer());
            }
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
  
  /**
   * Function that do the action the user select
   *
   * @param ui, board
   * @return Action
   */
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
  
  /**
   * Function that allows user to manipulate the given patch
   * on his quilt. Moving it in direction he wants if possible
   * Placing it on his quilt or even going back to the previous
   * Action
   *
   * @param ui, board, patch
   * @return Action
   */
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
  
  public static Path getPathFromGameMode(MenuOption gameMode) {
  	return switch(gameMode.getBind()) {
  		case 1 -> {yield Path.of("resources/settings/patchwork_basic.xml");}
  		case 2 -> {yield Path.of("resources/settings/patchwork_full.xml");}
  		default -> {throw new AssertionError("Can't be here");}
  	};
  }
  

  public static void main(String[] args) {
    // var path = Path.of("resources/settings/patchwork_full.xml");
  	var cli = new CommandLineInterface();
  	var menu = new Menu(cli);
  	var gameMode = menuLoop(menu, cli);
  	var path = getPathFromGameMode(gameMode);
    try {
      var xmlParser = new XMLParser();
      var xmlElement = xmlParser.parse(path);
      var board = GameBoard.fromXML(xmlElement, gameMode);
      mainLoop(new CommandLineInterface(), board);
    } catch (IOException e) {
      System.err.println("Error while trying to make game board from " + path);
      System.err.println(e.getMessage());
      System.exit(1);
      return;
    }
 
  }

}