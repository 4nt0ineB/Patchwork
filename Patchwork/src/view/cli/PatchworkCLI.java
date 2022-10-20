package view.cli;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import model.Coordinates;
import model.GameBoard;
import model.Patch;
import model.QuiltBoard;
import view.Action;
import view.UserInterface;

public class PatchworkCLI implements UserInterface {
  
  private static Scanner scanner = new Scanner(System.in);
  private static HashMap<String, Action> actionBinding = new HashMap<>();
  
  public void init() {
    actionBinding.put("a", Action.ADVANCE);
    actionBinding.put("b", Action.TAKE_PATCH);
    actionBinding.put("q", Action.QUIT);
  }
  
  @Override
  public void draw(GameBoard gb) {
    var sb = new StringBuilder();
    for(var player: gb.players()) {
      if(player == gb.currentPlayer()) {
        sb.append(Color.ANSI_GREEN);
      }
      sb.append("[").append(player.name()).append("]");
      sb.append(Color.ANSI_RESET);
      sb.append(" Pos :").append(player.position());
      sb.append(", Buttons :").append(player.buttons()).append("\n");
    }
    System.out.println(sb);
  }
  
  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
    // System.out.flush();
  }

  @Override
  public Action choice() {
    return Action.QUIT;
  }

  private void drawQuilt(QuiltBoard quilt) {
    var w = quilt.width();
    var h = quilt.height();
    var sb = new StringBuilder();
    // top
    sb.append("┌");
    for(var i = 0; i < w; i++) {
      sb.append("─");
    }
    sb.append("┐\n");
    // body
    for(var y = 0; y < h; y++) {
      sb.append("|");
      for(var x = 0; x < w; x++) {
        if(quilt.occupied(new Coordinates(y, x))) {
          sb.append(Color.ANSI_CYAN_BACKGROUND)
          .append("x")
          .append(Color.ANSI_RESET);
        }else {
          sb.append(" ");
        }
      }
      sb.append("|\n");
    }
    // bottom
    sb.append("└");
    for(var i = 0; i < w; i++) {
      sb.append("─");
    }
    sb.append("┘");
    System.out.println(sb);
  }
  
  @Override
  public int tryAndBuyPatch(GameBoard gb) {
    Objects.requireNonNull(patches, "Can't select over null");
    if(patches.size() == 0) {
      throw new IllegalArgumentException("The player has no patch to select");
    }
    for(var i = 0; i < patches.size(); i++) {
      System.out.println(i + ". ");
      drawPatch(patches.get(0));
    }
    
    return 0;
  }
  
  public void drawPatch(Patch patch) {
    // we use a quilt board to deal with absolute coordinates
    var quilt = new QuiltBoard(2, 2);
    while(!quilt.add(patch)) {
      patch.absoluteMoveTo(new Coordinates(quilt.height() / 2, quilt.width() / 2));
      quilt = new QuiltBoard(quilt.height() + 1, quilt.width() + 1);
    }
    // draw the patch
    var sb = new StringBuilder();
    for(var y = 0; y < quilt.height(); y++) {
      sb.append("   ");
      for(var x = 0; x < quilt.width(); x++) {
        if(quilt.occupied(new Coordinates(y, x))) {
          sb.append("x");
        }
      }
      sb.append("\n");
    }
    System.out.println(sb);
  }
  
  @Override
  public Action letPlayerTryPatch(GameBoard gb) {
  System.out.println(Color.ANSI_GREEN +
      "[" + gb.currentPlayer().name() + "]"
      + Color.ANSI_RESET
      );
  drawQuilt(gb.currentPlayer().quilt());
    
    return Action.DONT_PLACE_PATCH;
  }
  
  @Override
  public Action getPlayerActionForTurn(GameBoard gb) {
    Action action = Action.ERROR;
    boolean validInput = true;
    do {
      System.out.println(Color.ANSI_ORANGE + "[Action]" + Color.ANSI_RESET);
      if(gb.currentPlayerCanAdvance()) {
        System.out.println("(a) Advance");
      }
      System.out.print("""
              (b) Take and place a patch
              (q) Ragequit
              """);
      System.out.print("Choice ? : ");
      var input = scanner.nextLine();
      action = actionBinding.getOrDefault(input, Action.ERROR);
      if(
          (action.equals(Action.ADVANCE) && !gb.currentPlayerCanAdvance())
          || action.equals(Action.ERROR)
          ){
        System.out.println("Wrong choice.");
        validInput = false;
      }
      
    }while(!validInput);
    return action;
  }
  
  /**
   * Close the interface: <br>
   * 
   * close scanner on {@link System.in}
   */
  public void close() {
    scanner.close();
    System.out.println("Bye");
    
  }

  @Override
  public void movePatch(Patch patch) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawSplashScreen() {
    var splash = Color.ANSI_BOLD+ "  _____      _       _                       _    \n"
        + " |  __ \\    | |     | |                     | |   \n"
        + " | |__) |_ _| |_ ___| |____      _____  _ __| | __\n"
        + " |  ___/ _` | __/ __| '_ \\ \\ /\\ / / _ \\| '__| |/ /\n"
        + " | |  | (_| | || (__| | | \\ V  V / (_) | |  |   < \n"
        + " |_|   \\__,_|\\__\\___|_| |_|\\_/\\_/ \\___/|_|  |_|\\_\\\n"
        + Color.ANSI_GREEN + "└─────────────────────────────────────────────────┘\n"
        + "\n"
        + Color.ANSI_RESET;
    System.out.println(splash);
  }

}
