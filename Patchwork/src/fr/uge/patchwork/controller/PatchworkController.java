package fr.uge.patchwork.controller;

import java.awt.Color;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import fr.uge.patchwork.model.Game;
import fr.uge.patchwork.model.GameMode;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.LeatherPatch;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.view.UserInterface;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.gui.GraphicalUserInterface;
import fr.umlv.zen5.Application;

public class PatchworkController {
  
  private Player player;
  private UserInterface ui;
  private Game game;
  private Stack<Event> triggeredEvents = new Stack<>();
  private GameMode gameMode;
  private boolean specialTile = true;

  public PatchworkController(UserInterface ui) {
    this.ui = Objects.requireNonNull(ui);
  }
  
  /**
   * Menu Loop that draw the menu and wait for the user to choose
   * his game mode 
   *
   * @param ui the user interface
   * @return Choosen game mode
   */
  public boolean choseGameMode() {
    var choices = new LinkedHashSet<KeybindedChoice>();
    choices.add(new KeybindedChoice('b', "The basic game"));
    choices.add(new KeybindedChoice('f', "The full game"));
    choices.add(new KeybindedChoice('q', "Quit"));
    var wantToPlay = true;
    do {
      ui.clear();
      var chose = ui.gameModeMenu(choices);
      if(!chose.isEmpty()) {
        gameMode = switch(chose.get().key()) {
          case 'b' -> GameMode.PATCHWORK_BASIC;
          case 'f' -> GameMode.PATCHWORK_FULL;
          case 'q' -> {
            wantToPlay = false;
            yield null;
          }
          default -> throw new AssertionError("there shoulnd't be other possiblities");
        };
      }
     ui.display();
    }while(wantToPlay && gameMode == null);
    return wantToPlay;
  }
  
  public void init() throws IOException {
    game = Game.fromGameMode(gameMode);
    player = game.trackBoard().latestPlayer();
  }
  
  public boolean run() {
    while((player = game.trackBoard().latestPlayer()) != null   
        && player.position() != game.trackBoard().spaces()) {
      triggeredEvents.clear();
      if(!playTurn()) { // quit asked
       return false; 
      }
      playEvents();
    }
    return endGame();
  }
  
  /**
   * End game loop
   * @param ui
   * @param gameBoard
   * @return true if want a new game, otherwise false
   */
  public boolean endGame() {
    var choices = Set.of(
        new KeybindedChoice('q', "Quit"), 
        new KeybindedChoice('n', "New game"));
    for(;;) {
      ui.clear();
      ui.drawScoreBoard(game.trackBoard());
      ui.display(); 
      var chose = ui.endGameMenu(choices);
      if(chose.isPresent()) {
        switch (chose.get().key()) {
          case 'q' -> { return false; }
          case 'n' -> { return true; } 
          default -> { throw new AssertionError("There shouldn't be other choices"); }
        }
      }
      ui.display();
    }
  }
  
  private void playEvents() {
    while(!triggeredEvents.isEmpty()) {
      ui.clear();
      var event = triggeredEvents.peek();
      switch(event.type()) {
        case BUTTON_INCOME -> {
          int amount = player.quilt().buttons();
          if(amount != 0) {
            player.addButtons(amount);
          }
          triggeredEvents.pop();
        }
        case PATCH_INCOME -> {
          if(manipulatePatch(new LeatherPatch())) {
            game.trackBoard().removeEvent(event);
            triggeredEvents.pop();
          }
        }
        default -> {throw new AssertionError(); }
      }
      ui.display();
    }
    testSpecialTile();
  }

  public void testSpecialTile() {
    if(specialTile && player.quilt().hasFilledSquare(7)) {
      player.earnSpecialTile();
      specialTile = false;
    }
  }

  private boolean playTurn() {
    for(;;) {
      ui.clear();
      ui.draw(game.trackBoard());
      ui.draw(game.patchManager());
      ui.display();
      var chose = ui.turnMenu(availableActions());
      if(chose.isPresent()) {
        switch (chose.get().key()) {
          case 's' -> { 
            // select a patch
            var selectedPatch = selectPatch();
            // try placing it on the quilt
            if(manipulatePatch(selectedPatch)) { 
              // placed
              game.trackBoard().movePlayer(player, selectedPatch.moves());
              // update patch manager
              game.patchManager().moveNeutralToken(game.patchManager().patches(3).indexOf(selectedPatch));
              game.patchManager().removeAtToken();
              return true;
            }
          }
          case 'a' -> {
            advancePlayer();
            return true;
          }
          case 'r' -> {
            return false; // quit asked
          }
          default -> throw new AssertionError("There shouldn't be other choices");
        }
      }    
      ui.display();
    }
  }
  
  private RegularPatch selectPatch() {
    for(;;) {
      ui.clear();
      ui.draw(game.trackBoard());
      ui.display();
      var selectedPatch = ui.selectPatch(game.patchManager().patches(3), game.patchManager());
      if(selectedPatch.isPresent()) {
        return selectedPatch.get();
      }
      ui.display();
    }
  }
  
  /**
   * Advance the current player to the space in front of the next player. This
   * action lead to button income proportional of number of crossed spaces.
   */
  public void advancePlayer() {
    int moves = 1;
    Player nextPlayer = game.trackBoard().nextPlayerFrom(player.position() + moves);
    if(nextPlayer != null) { // player ahead
      moves = nextPlayer.position() + 1 - player.position();
    }
    triggeredEvents.addAll(game.trackBoard().movePlayer(player, moves));
    player.addButtons(moves);
  }
  
  /**
   * Function that allows user to manipulate the given patch
   * on a quilt. Moving it in direction he wants if possible
   * Placing it on his quilt or even going back to the previous
   * Action
   *
   * @param ui 
   * @param board
   * @return true if patch is placed, otherwise false
   */
  private boolean manipulatePatch(Patch patch) {
    // We use a dummy quilt to play with the patch
    patch.absoluteMoveTo(new Coordinates(player.quilt().width() / 2, player.quilt().height() / 2));
    var loop = true;
    do {
      ui.clear();
      ui.drawDummyQuilt(player, patch);
      ui.display();
      var chose = ui.manipulatePatch(availableManipulations(player.quilt(), patch));
      if(chose.isPresent()) {
        switch (chose.get().key()) {
          case 's'  -> patch.moveUp();
          case 'w' -> patch.moveDown();
          case 'd' -> patch.moveRight();
          case 'q' -> patch.moveLeft();
          case 'z' -> patch.rotateLeft();
          case 'a' -> patch.rotateRight();
          case 'f' -> patch.flip();
          case 'p' -> {
            return player.placePatch(patch);
          }
          case 'b' -> loop = false;
          default -> { throw new AssertionError("There shouldn't be other choices"); }
        }
      }
      ui.display();
    } while (loop);
    return false;
  }
  
  private Set<KeybindedChoice> availableActions(){
    var choices = new HashSet<KeybindedChoice>();
    if(game.trackBoard().playerCanAdvance(player)) {
      choices.add(new KeybindedChoice('a', "Advance"));
    }
    var patches = game.patchManager().patches(3);
    if(!patches.isEmpty() &&
        patches.stream()
        .anyMatch(patch -> player.buttons() >= patch.price())) {
      choices.add(new KeybindedChoice('s', "Select a patch"));
    }
    choices.add(new KeybindedChoice('r', "Ragequit"));
    return choices;
  }
  
  private Set<KeybindedChoice> availableManipulations(QuiltBoard quilt, Patch patch){
    var choices = new HashSet<KeybindedChoice>();
    if (quilt.canAdd(patch)) {
      choices.add(new KeybindedChoice('p', "Place the patch"));
    }
    if (patch.canMoveUp(0)) {
      choices.add(new KeybindedChoice('s', "up"));
    }
    if (patch.canMoveDown(quilt.height())) {
      choices.add(new KeybindedChoice('w', "down"));
    }
    if (patch.canMoveLeft(0)) {
      choices.add(new KeybindedChoice('q', "left"));
    }
    if (patch.canMoveRight(quilt.width())) {
      choices.add(new KeybindedChoice('d', "right"));
    }
    var basicChoices = Set.of( 
        new KeybindedChoice('z', "rotate left"), 
        new KeybindedChoice('a', "rotate right"),
        new KeybindedChoice('f', "flip"),
        new KeybindedChoice('b', "back"));
    choices.addAll(basicChoices);
    return choices;
  }
  
  public static void startGame(UserInterface userInterface) {
    var loop = true;
    while(loop) {
      var controller = new PatchworkController(userInterface);
      if(!controller.choseGameMode()) {
        break;
      }
      try {
        controller.init();
      } catch (IOException e) {
        System.err.println(e.getMessage());
        userInterface.close();
        System.exit(1);
        return;
      }
      if(!controller.run()) {
        break;
      }
    }
    userInterface.close();
  }
    
  public static void main(String[] args) {
    var cli = false;
    if(cli) {
      startGame(new CommandLineInterface());
    }else {
      Application.run(Color.BLACK, (context) -> {
        var ui = new GraphicalUserInterface(context);
        try {
          ui.init();
        } catch (IOException e) {
          System.err.println(e.getMessage());
          ui.close();
          System.exit(1);
          return;
        }
        startGame(ui);
      });
    }
  }
  
}
