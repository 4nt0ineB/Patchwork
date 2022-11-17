package controller;

import java.util.Objects;

public record Action(String name, String bind, String description) {
  public Action {
    Objects.requireNonNull(name);
    Objects.requireNonNull(bind);
    Objects.requireNonNull(description);
  }
}
