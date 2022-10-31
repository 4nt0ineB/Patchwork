package view;

import java.util.Objects;

public enum Action {
  ADVANCE("a", "Advance"),
  TAKE_PATCH("b", "Take and place a patch"),
  QUIT("q", "Ragequit"),
  DEFAULT("default", "default"),
  UP("z", "up"),
  DOWN("e", "down"),
  RIGHT("r", "right"),
  LEFT("a", "left"),
  ROTATE_RIGHT("t", "rotate right"),
  ROTATE_LEFT("y", "rotate left"), 
  PLACE("p", "Buy and place the patch");
  
  private final String bind;
  private final String description;
  
  private Action(String bind, String description) {
    Objects.requireNonNull(bind, "Bind can't be null");
    Objects.requireNonNull(description, "Description can't be null");
    this.bind = bind;
    this.description = description;
  }
  
  public String bind() {
    return bind;
  }
  
  public String description() {
    return description;
  }
  
  @Override
  public String toString() {
    return "[" + bind + "] " + description ;
  }
  
}
