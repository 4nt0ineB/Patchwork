package view;

import java.util.Objects;

public enum Action {
  ADVANCE("a", "Advance"),
  TAKE_PATCH("b", "Take and place a patch"),
  QUIT("c", "Ragequit"),
  ERROR("err", "Just die");
  
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
  
}
