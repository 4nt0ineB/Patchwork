package fr.uge.patchwork.view.cli;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.Coordinates;
import fr.uge.patchwork.model.component.Patch;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.view.Drawable;
import fr.uge.patchwork.view.UserInterface;

/**
 * 
 * Implementation of a patchwork game command line interface
 *
 */
public final class CommandLineInterface implements UserInterface {
  
  // Should be a singleton then ? Because there is only one System.in
  // and closing a PatchworkCLI instance (therefore the scanner) 
  // would mess with other instance also using System.in
  private static final Scanner scanner = new Scanner(System.in); 
  // It's like the window, we draw our elements on it and we refresh the display
  private final StringBuilder builder = new StringBuilder();
  private final LinkedHashSet<String> messages = new LinkedHashSet<>();
  
  /**
   * Access the string builder of the command line interface
   * having the same purpose of a "window", on which text content can be printed
   * @return
   */
  public StringBuilder builder() {
    return builder;
  }
  
  public void addMessage(String message) {
    Objects.requireNonNull(message, "The message can't be null");
    messages.add(message);
  }
  
  public void clearMessages() {
    messages.clear();
  }
  
  public void drawMessages() {
    messages.forEach(message -> {
      builder
      .append("\n")
      .append(message)
      .append("\n");
      }); 
  }
  
  @Override
  public void draw(Drawable drawable) {
    Objects.requireNonNull("The drawable object can't be null");
    ((DrawableOnCLI) drawable).drawOnCLI(this);
  }
  
  public void display() {
    System.out.print(builder);
  }
  
  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
//    System.out.flush();
    builder.setLength(0);
    drawSplashScreen();
  }

  @Override
  public Optional<Patch> selectPatch(List<Patch> patches) {
    Objects.requireNonNull(patches);
    if(patches.isEmpty()) {
      throw new IllegalArgumentException("Their should be at least 1 patch in the list");
    }
    var i = 0;
    clear();
    // Draw choices
    builder.append("\n");
    for(var patch: patches) {
      i++;
      builder.append(i + ". ");
      patch.drawOnCLI(this);
      builder.append("\n");
    }
    display();
    // Menu
    var localBuilder = new StringBuilder();
    localBuilder
    .append(Color.ANSI_ORANGE)
    .append("\nChoose : ")
    .append(Color.ANSI_RESET);
    System.out.print(localBuilder);
    if(scanner.hasNextInt()) {
      var input = scanner.nextInt();
      scanner.nextLine();
      if(input > 0 && input <= i) {
        return Optional.of(patches.get(input - 1));
      }
    }else {
      scanner.nextLine();
    }
    System.out.println("Wrong choice\n");
    return Optional.empty();
  }
  @Override
  public void drawDummyQuilt(QuiltBoard quilt, Patch patch) {
    Objects.requireNonNull(quilt, "the quilt can't be null");
    Objects.requireNonNull(patch, "The patch can't be null");
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
  }
  
  @Override
  public int getPlayerChoice(Set<KeybindedChoice> choices){
    Objects.requireNonNull(choices, "the quilt can't be null");
    if(choices.isEmpty()) {
      throw new IllegalArgumentException("The set of choices can't be empty");
    }
    var localBuilder = new StringBuilder();
    localBuilder
    .append(Color.ANSI_ORANGE)
    .append("\n[Choices]\n")
    .append(Color.ANSI_RESET);
    choices.forEach(option -> 
      localBuilder.append(option).append("\n"));
    localBuilder.append("\nChoice ? : ");
    System.out.print(localBuilder);
    String input;
    if(scanner.hasNextLine() 
        && (input = scanner.nextLine()).length() == 1) {
      for(var choice: choices) {
        if(choice.key() == input.charAt(0)) {
          return choice.key();
        }
      }
    }
    System.out.println("Wrong choice\n");
    return -1;
  }
  
  /**
   * Close the interface: <br>
   * 
   * close scanner on {@link System#in}
   */
  @Override
  public void close() {
    scanner.close();
  }

  @Override
  public void drawSplashScreen() {
    var splash = Color.ANSI_BOLD + "  _____      _       _                       _    \n"
        + " |  __ \\    | |     | |                     | |   \n"
        + " | |__) |_ _| |_ ___| |____      _____  _ __| | __\n"
        + " |  ___/ _` | __/ __| '_ \\ \\ /\\ / / _ \\| '__| |/ /\n"
        + " | |  | (_| | || (__| | | \\ V  V / (_) | |  |   < \n"
        + " |_|   \\__,_|\\__\\___|_| |_|\\_/\\_/ \\___/|_|  |_|\\_\\\n"
        + Color.ANSI_GREEN + "└─────────────────────────────────────────────────┘"
        + Color.rgb(2, 77, 24) +  "v2.0\n" + Color.ANSI_RESET
        + "\n"
        + Color.ANSI_RESET;
    builder.append(splash);
  }
  
}
