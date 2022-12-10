package fr.uge.patchwork.view.cli;

import static java.util.Comparator.reverseOrder;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.gameboard.event.EventType;
import fr.uge.patchwork.model.component.patch.Coordinates;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.Patch2D;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.view.Color;
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
  public void init() {}

  @Override
  public Optional<KeybindedChoice> gameModeMenu(Set<KeybindedChoice> choices) {
    return getPlayerChoice(choices);
  }

  @Override
  public Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices) {
    return getPlayerChoice(choices);
  }

  @Override
  public Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices) {
    return getPlayerChoice(choices);
  }

  @Override
  public Optional<KeybindedChoice> manipulatePatch(Set<KeybindedChoice> choices) {
    return getPlayerChoice(choices);
  }
  
  public void draw(KeybindedChoice choice) {
    builder()
    .append("[").append(choice.key()).append("] ")
    .append(choice.description());
  }
  
  public void draw(Player player) {
    Objects.requireNonNull(player, "The player can't be null");
    builder()
    .append(String.format("%5d|", player.position()))
    .append(" " + player.name() + " - buttons [" + player.buttons() + "]")
    .append(player.specialTile() ? " (SpecialTile) " : "");
  }
  
  public void draw(QuiltBoard quilt) {
    // top
    builder.append("┌");
    for (var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┐\n");
    // body
    for (var y = 0; y < quilt.height(); y++) {
      builder.append("|");
      for (var x = 0; x < quilt.width(); x++) {
        if (quilt.occupied(new Coordinates(y, x))) {
          builder.append(CLIColor.ANSI_CYAN_BACKGROUND).append("x");
        } else {
          builder.append(" ");
        }
      }
      builder.append(CLIColor.ANSI_RESET);
      builder.append("|\n");
    }
    // bottom
    builder.append("└");
    for (var i = 0; i < quilt.width(); i++) {
      builder.append("─");
    }
    builder.append("┘");
  }
  
  @Override
  public void draw(TrackBoard trackboard) {
    Objects.requireNonNull(trackboard, "The track board can't be null");
    /*User needs to see what are the tiles where the patches income and buttons income
     *  are so he can prepare a proper strategy.*/
    var events = trackboard.events();
    if (!events.isEmpty()) {
        builder.append("[ ---- (Patch Tiles: ");
        events.stream()
          .filter(e -> e.type().equals(EventType.PATCH_INCOME))
          .forEach(e -> builder.append(e.position()).append(" "));
        builder.append(") ---- ]\n");
        builder.append("[ ---- (Button Tiles: ");
        events.stream()
          .filter(e -> e.type().equals(EventType.BUTTON_INCOME))
          .forEach(e -> builder.append(e.position()).append(" "));
          builder.append(") ---- ]\n");
    }
    builder.append("\n");
    for (var player : trackboard.players()) {
      if(player.equals(trackboard.latestPlayer())) {
        builder.append(CLIColor.ANSI_GREEN);
      }
      draw(player);
      builder.append(CLIColor.ANSI_RESET).append("\n");
    }
    builder.append("\n");
  }
  
  @Override
  public void drawScoreBoard(TrackBoard trackboard) {
    Objects.requireNonNull(trackboard, "The track board can't be null");
    builder.append(CLIColor.ANSI_ORANGE)
    .append("[ ---- Scores ---- ] \n")
    .append(CLIColor.ANSI_RESET);
    var sortedPlayers = trackboard.players().stream()
        .sorted(reverseOrder())
        .peek(p -> builder.append(p.name())
            .append(" : ")
            .append(p.score())
            .append("\n"))
        .toList();
    builder.append(CLIColor.ANSI_YELLOW)
    .append(sortedPlayers.get(0).name())
    .append(" Wins !\n")
    .append(CLIColor.ANSI_RESET);
  }


  public void draw(PatchManager patchmanager) {
    Objects.requireNonNull(patchmanager, "The patch manager can't be null");
  }
  
  public void display() {
    System.out.print(builder);
  }
  
  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
    // System.out.flush();
    builder.setLength(0);
    drawSplashScreen();
  }
  
  public void draw(Patch2D patch) {
    // We use a conceptual square to deal with absolute coordinates.
    // While the patch doesn't fit in, we expand the square
    // and replace the origin of the patch at the center of it
    var width = 2;
    var height = 2;
    while (!patch.fits(width, height)) {
      patch.absoluteMoveTo(new Coordinates(height / 2, width / 2));
      width += 1;
      height += 1;
    }
    // draw the patch
    for (var y = 0; y < height; y++) {
      builder().append("  ");
      for (var x = 0; x < width; x++) {
        if (patch.absoluteCoordinates().contains(new Coordinates(y, x))) {
          builder().append("x");
        } else {
          builder().append(" ");
        }
      }
      builder().append("\n");
    }
  }
  
  public void draw(RegularPatch patch) {
    builder().append("[")
    .append("Price: ").append(patch.price())
    .append(", Moves: ").append(patch.moves())
    .append(", Buttons: ").append(patch.buttons())
    .append("]\n\n");
    draw(patch.patch2D());
  }

  @Override
  public Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager) {
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
      draw(patch);
      builder.append("\n");
    }
    display();
    // Menu
    var localBuilder = new StringBuilder();
    localBuilder
    .append(CLIColor.ANSI_ORANGE)
    .append("\nChoose : ")
    .append(CLIColor.ANSI_RESET);
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
  public void drawDummyQuilt(Player player, Patch patch) {
    Objects.requireNonNull(player.quilt(), "the quilt can't be null");
    Objects.requireNonNull(patch, "The patch can't be null");
    // top
    builder.append("┌");
    for(var i = 0; i < player.quilt().width(); i++) {
      builder.append("─");
    }
    builder.append("┐\n");
    // body
    for(var y = 0; y < player.quilt().height(); y++) {
      builder.append("|");
      for(var x = 0; x < player.quilt().width(); x++) {
        var isPatchHere = patch.meets(new Coordinates(y, x));
        if(player.quilt().occupied(new Coordinates(y, x))) {
          if(isPatchHere){
            builder.append(CLIColor.ANSI_RED_BACKGROUND)
            .append("░")
            .append(CLIColor.ANSI_RESET);
          }else {
            builder.append(CLIColor.ANSI_CYAN_BACKGROUND)
            .append("▒")
            .append(CLIColor.ANSI_RESET);
          }
        }else {
          if(isPatchHere) {
            builder.append(CLIColor.ANSI_YELLOW_BACKGROUND)
            .append("▓");
          }else {
            builder.append(" ");
          }
          builder.append(CLIColor.ANSI_RESET);
        }
      }
      builder.append("|\n");
    }
    // bottom
    builder.append("└");
    for(var i = 0; i < player.quilt().width(); i++) {
      builder.append("─");
    }
    builder.append("┘");
  }
  
  public Optional<KeybindedChoice> getPlayerChoice(Set<KeybindedChoice> choices){
    Objects.requireNonNull(choices, "the quilt can't be null");
    if(choices.isEmpty()) {
      throw new IllegalArgumentException("The set of choices can't be empty");
    }
    var localBuilder = new StringBuilder();
    localBuilder
    .append(CLIColor.ANSI_ORANGE)
    .append("\n[Choices]\n")
    .append(CLIColor.ANSI_RESET);
    choices.forEach(option -> 
      localBuilder.append(option).append("\n"));
    localBuilder.append("\nChoice ? : ");
    System.out.print(localBuilder);
    String input;
    if(scanner.hasNextLine() 
        && (input = scanner.nextLine()).length() == 1) {
      for(var choice: choices) {
        if(choice.key() == input.charAt(0)) {
          return Optional.of(choice);
        }
      }
    }
    System.out.println("Wrong choice\n");
    return Optional.empty();
  }
  
  public void drawSplashScreen() {
    var splash = CLIColor.ANSI_BOLD + "  _____      _       _                       _    \n"
        + " |  __ \\    | |     | |                     | |   \n"
        + " | |__) |_ _| |_ ___| |____      _____  _ __| | __\n"
        + " |  ___/ _` | __/ __| '_ \\ \\ /\\ / / _ \\| '__| |/ /\n"
        + " | |  | (_| | || (__| | | \\ V  V / (_) | |  |   < \n"
        + " |_|   \\__,_|\\__\\___|_| |_|\\_/\\_/ \\___/|_|  |_|\\_\\\n"
        + CLIColor.ANSI_GREEN + "└─────────────────────────────────────────────────┘"
        + CLIColor.rgb(2, 77, 24) +  "v2.0\n" + CLIColor.ANSI_RESET
        + "\n"
        + CLIColor.ANSI_RESET;
    builder.append(splash);
  }

  public void drawMessage(String txt, Color color) {
    Objects.requireNonNull(txt);
    builder.append(CLIColor.fromColor(color));
    drawMessage(txt);
  }
  
  public void drawMessage(String txt) {
    Objects.requireNonNull(txt);
    builder.append(txt).append(CLIColor.ANSI_RESET);
  }

 
}
