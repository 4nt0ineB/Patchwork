package view.cli;

import java.util.Scanner;
import java.util.Set;

import controller.Action;
import model.Coordinates;
import model.GameBoard;
import model.Patch;
import model.QuiltBoard;
import view.UserInterface;

public class PatchworkCLI implements UserInterface {
  
  private static final Scanner scanner = new Scanner(System.in);
  
  @Override
  public void draw(GameBoard gb) {
    gb.drawOnCLI();
  }
  
  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  @Override
  public Action choice() {
    return Action.QUIT;
  }
  
  @Override
  public void selectPatch(GameBoard gb) {
    var i = 0;
    var input = -1;
    // Draw choices
    var patches = gb.neutralToken().availablePatches();
    for(var patch: patches) {
      i++;
      System.out.print(i + ". ");
      patch.drawOnCLI();
    }
    // Menu
    boolean validInput = false;
    var builder = new StringBuilder();
    builder
    .append(Color.ANSI_ORANGE)
    .append("Choose : ")
    .append(Color.ANSI_RESET);
    // Selection loop
    do {
      System.out.print(builder);
      input = Integer.valueOf(scanner.nextLine());
      if(input > 0 && input <= i) {
        validInput = true;
      }
    }while(!validInput);
    gb.selectPatch(patches.get(input - 1));
  }
  
  public void drawDummyQuilt(QuiltBoard quilt, Patch patch) {
    var builder = new StringBuilder();
    // top
    builder.append("┌");
    for(var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┐\n");
    // body
    for(var y = 0; y < quilt.height(); y++) {
      builder.append("|");
      for(var x = 0; x < quilt.width(); x++) {
        var isPatchHere = patch.meets(new Coordinates(y, x));
        if(quilt.occupied(new Coordinates(y, x))) {
          if(isPatchHere){
            builder.append(Color.ANSI_RED_BACKGROUND)
            .append("░")
            .append(Color.ANSI_RESET);
          }else {
            builder.append(Color.ANSI_CYAN_BACKGROUND)
            .append("▒")
            .append(Color.ANSI_RESET);
          }
        }else {
          if(isPatchHere) {
            builder.append(Color.ANSI_YELLOW_BACKGROUND)
            .append("▓");
          }else {
            builder.append(" ");
          }
          builder.append(Color.ANSI_RESET);
        }
      }
      builder.append("|\n");
    }
    // bottom
    builder.append("└");
    for(var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┘");
    System.out.println(builder);
  }
  
  @Override
  public Action getPlayerActionForTurn(GameBoard gb, Set<Action> options) {
    var builder = new StringBuilder();
    builder
    .append(Color.ANSI_ORANGE)
    .append("[Actions] ")
    .append(Color.ANSI_RESET)
    .append("\n");
    options.stream().forEach(option -> 
      builder.append(option).append("\n"));
    builder.append("\nChoice ? : ");
    System.out.print(builder);
    var input = scanner.nextLine();
    for(var op: options) {
      if(op.bind().equals(input)) {
        return op;
      }
    }
    System.out.println("Wrong choice");
    return Action.DEFAULT;
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
