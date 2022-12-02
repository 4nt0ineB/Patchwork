package fr.uge.patchwork.controller;

import java.util.Objects;

import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

/**
 * Allow to bind a choice to a simple keybind, 
 * a single character
 */
public record KeybindedChoice(char key, String description) implements DrawableOnCLI {
  
  public KeybindedChoice {
    Objects.requireNonNull(description, "the description can't be null");
  }
  
  public char key() {
    return key;
  }
  
  public String description() {
    return description;
  }
  
  @Override
  public String toString() {
    return "[" + key + "] " + description; 
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    ui.builder()
    .append("[")
    .append(key)
    .append("] ")
    .append(description);
  }
  
}
