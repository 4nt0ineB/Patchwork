package model;

import java.util.Objects;

public class Player {
  private final String name;
  private int buttons;
  // private final List<SpecialTile> tiles;
  private int position;
  
  
  public Player(String name, int buttons, int position) {
    Objects.requireNonNull(name, "The player must have a name");
    if(buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    if(position < 0) {
      throw new IllegalArgumentException("The player can't go lower than the first square of the BoardGame");
    }
    this.name = name;
    this.buttons = buttons;
    this.position = position;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(buttons, name, position);
  }
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Player o
        && buttons == o.buttons
        && position == o.position
        && name.equals(name)
        ;
  }
  @Override
  public String toString() {
    return "Player [name=" + name + ", buttons=" + buttons + ", position=" + position + "]";
  }
  
  
  
}
