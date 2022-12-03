package fr.uge.patchwork.controller;

import java.util.Objects;

import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

/**
 * Allow to bind a choice to a simple keybind, 
 * a single character
 * 
 * @param key the key of the key bind
 * @param description description of the action related to this keybind
 */
public record KeybindedChoice(char key, String description) implements DrawableOnCLI {
  
  public KeybindedChoice {
    Objects.requireNonNull(description, "the description can't be null");
  }
  
  @Override
  public String toString() {
    return "[" + key + "] " + description; 
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    ui.builder()
    .append("[").append(key).append("] ")
    .append(description);
  }
  
}
