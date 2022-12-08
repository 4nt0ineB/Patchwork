package fr.uge.patchwork.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import fr.uge.patchwork.model.Game;
import fr.uge.patchwork.model.GameMode;
import fr.uge.patchwork.model.component.Coordinates;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.gameboard.event.Event;
import fr.uge.patchwork.model.component.patch.LeatherPatch;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.view.UserInterface;
import fr.uge.patchwork.view.cli.CommandLineInterface;

public class Controller2 {
  
  private Player player;
  private UserInterface ui;
  private Game game;
  private Stack<Event> triggeredEvents = new Stack<>();
  private GameMode gameMode;
  
  public Controller2(UserInterface ui) {
    this.ui = Objects.requireNonNull(ui);
  }
  
  /**
   * Menu Loop that draw the menu and wait for the user to choose
   * his game mode 
   *
   * @param ui the user interface
   * @return Choosen game mode
   */
  public void choseGameMode() {
    var choices = new LinkedHashSet<KeybindedChoice>();
    choices.add(new KeybindedChoice('b', "The basic game"));
    choices.add(new KeybindedChoice('f', "The full game"));
    do {
      ui.clear();
      ui.drawMessages();
      ui.display();
      gameMode = switch(ui.getPlayerChoice(choices)) {
       case 'b' -> GameMode.PATCHWORK_BASIC;
       case 'f' -> GameMode.PATCHWORK_FULL;
       case -1 -> null;
       default -> throw new AssertionError("there shoulnd't be other possiblities");
     };
    }while(gameMode == null);
  }
  
  public void init() {
    try {
      game = Game.fromGameMode(gameMode);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      ui.close();
      System.exit(1);
      return;
    }
  }
  
  public void run() {
    while(player.position() != game.trackBoard().spaces()) {
      triggeredEvents.clear();
      player = game.trackBoard().latestPlayer();
      playTurn();
      playEvents();
    }
  }
  
  /**
   * End game loop
   * @param ui
   * @param gameBoard
   * @return true if want a new game, otherwise false
   */
  public boolean endGame() {
    Objects.requireNonNull(ui, "The interface can't be null");
    Objects.requireNonNull(ui, "The game board can't be null");
    var choices = Set.of(
        new KeybindedChoice('q', "Quit"), 
        new KeybindedChoice('n', "New game"));
    var choice = -1;
    ui.clear();
    ui.draw(game.trackBoard());
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
  
  private void playEvents() {
    while(!triggeredEvents.isEmpty()) {
      var event = triggeredEvents.peek();
      switch(event.type()) {
        case BUTTON_INCOME -> {
          int amount = player.quilt().buttons();
          if(amount != 0) {
            player.addButtons(amount);
          }
        }
        case PATCH_INCOME -> {
          manipulatePatch(new LeatherPatch());
          game.trackBoard().removeEvent(event);
        }
        default -> {throw new AssertionError(); }
      }
    }
    if(player.quilt().hasFilledSquare(7)) {
      player.earnSpecialTile();
    }
    // print msg on ui
  }

  private void playTurn() {
    ui.clear();
    ui.draw(game.trackBoard());
    ui.drawMessages();
    ui.display();
    boolean hasPlayed = false;
    while(!hasPlayed) {
      switch (ui.getPlayerChoice(availableActions())) {
        case 's' -> {
          var selectedPatch = ui.selectPatch(game.patchManager().patches(3));
          if(selectedPatch.isPresent()) {
            if(manipulatePatch(selectedPatch.get())) {
              game.trackBoard().movePlayer(player, selectedPatch.get().moves());
              hasPlayed = true;
            }
          }
        }
        case 'a' -> {
          advancePlayer();
          hasPlayed = true;
        }
        case 'r' -> {}
        case -1 -> {}
        default -> throw new AssertionError("There shouldn't be other choices");
      }
    }
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
  
  /**
   * Function that allows user to manipulate the given patch
   * on a quilt. Moving it in direction he wants if possible
   * Placing it on his quilt or even going back to the previous
   * Action
   *
   * @param ui 
   * @param board
   * @return Action
   */
  private boolean manipulatePatch(Patch patch) {
    // We use a dummy quilt to play with the patch
    patch.absoluteMoveTo(new Coordinates(player.quilt().width() / 2, player.quilt().height() / 2));
    var loop = true;
    do {
      ui.clear();
      ui.draw(game.trackBoard());
      ui.drawMessages();
      ui.drawDummyQuilt(player.quilt(), patch);
      ui.display();
      switch (ui.getPlayerChoice(availableManipulations(player.quilt(), patch))) {
        case 's'  -> patch.moveUp();
        case 'w' -> patch.moveDown();
        case 'd' -> patch.moveRight();
        case 'q' -> patch.moveLeft();
        case 'z' -> patch.rotateLeft();
        case 'a' -> patch.rotateRight();
        case 'p' -> {
          return player.placePatch(patch);
        }
        case 'b' -> loop = false;
        case -1 -> {}
        default -> { throw new AssertionError("There shouldn't be other choices"); }
      }
    } while (loop);
    return false;
  }
  
  private Set<KeybindedChoice> availableManipulations(QuiltBoard quilt, Patch patch){
    var choices = new HashSet<KeybindedChoice>();
    var basicChoices = Set.of(
        new KeybindedChoice('b', "back"), 
        new KeybindedChoice('z', "rotate left"), 
        new KeybindedChoice('a', "rotate right"));
    choices.addAll(basicChoices);
    if (quilt.canAdd(patch)) {
      choices.add(new KeybindedChoice('p', "Buy and place the patch"));
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
    return choices;
  }
  
  /**
   * Advance the current player to the space in front of the next player. This
   * action lead to button income proportional of number of crossed spaces.
   */
  public void advancePlayer() {
    int newPosition = player.position() + 1;
    Player nextPlayer = game.trackBoard().nextPlayerFrom(newPosition);
    if(nextPlayer != null) { // player ahead
      newPosition = nextPlayer.position() + 1;
    }
    int buttonIncome = newPosition - player.position();
    triggeredEvents.addAll(game.trackBoard().movePlayer(player, newPosition));
    player.addButtons(buttonIncome);
  }
  
  public static void main(String[] args) {
    var userInterface = new CommandLineInterface();
    var loop = true;
    while(loop) {
      var controller = new Controller2(userInterface);
      controller.choseGameMode();
      controller.init();
      controller.run();
      loop = controller.endGame();
    }
    userInterface.close();
    
  }
  
}
