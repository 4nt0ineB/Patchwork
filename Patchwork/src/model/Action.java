package model;

import java.util.Objects;

public enum Action {
  
  ADVANCE("a", "Advance"),
  SELECT_PATCH("b", "Take and place a patch"),
  BACK("r", "back"),
  QUIT("r", "Ragequit"),
  DEFAULT("default", "default"),
  // wqsdb
  UP("s", "up"),
  DOWN("w", "down"),
  RIGHT("d", "right"),
  LEFT("q", "left"),
  ROTATE_RIGHT("a", "rotate right"),
  ROTATE_LEFT("z", "rotate left"), 
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
