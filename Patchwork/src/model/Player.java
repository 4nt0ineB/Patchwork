package model;

import java.util.Objects;

public class Player {
  private final String name;
  private int buttons;
  // private final List<SpecialTile> tiles;
  private int position;
  private final QuiltBoard quilt;
  
  
  public Player(String name, int buttons, int position, QuiltBoard quilt) {
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
    this.quilt = quilt;
  }
  
  /**
   * Update the amount of player buttons.
   * @param amount can be negative but shall not 
   * @return true if player buttons don't go below 0, 
   * else false (the number of buttons remain unchanged)
   */
  public boolean updateButtons(int amount) {
    if(buttons + amount < 0) {
      return false;
    }
    buttons += amount;
    return true;
  }
  
  /**
   * Buy the patch. Test if the player have enough buttons to buy 
   * the patch and if it can be place on his quilt
   *
   * @param patch
   * @return true or false
   */
  public boolean buyPatch(Patch patch) {
    if(patch.price() <= buttons && quilt.addPatch(patch)) {
      buttons -= patch.price();
      return true;
    }
    return false;
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
  
  public int position() {
    return position;
  }
  
  public void move(int n) {
    position += n;
  }
  
  @Override
  public String toString() {
    return "Player [name=" + name + ", buttons=" + buttons + ", position=" + position + "]";
  }
  
  
  
}
