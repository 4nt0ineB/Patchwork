package fr.uge.patchwork.controller;

import java.util.Objects;

/**
 * Allow to bind a choice to a simple keybind, 
 * a single character
 * 
 * @param key the key of the key bind
 * @param description description of the action related to this keybind
 */
public record KeybindedChoice(char key, String description) {
  
  public KeybindedChoice {
    Objects.requireNonNull(description, "the description can't be null");
  }
  
  @Override
  public String toString() {
    return "[" + key + "] " + description; 
  }

}
