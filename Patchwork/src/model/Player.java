package model;

import java.util.Objects;

import view.cli.DisplayableOnCLI;
import view.cli.PatchworkCLI;

public class Player implements DisplayableOnCLI  {
  
  private final String name;
  private int buttons;
  private final QuiltBoard quilt;
  private int position;

  public Player(String name, int buttons, QuiltBoard quilt) {
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.buttons = buttons;
    this.quilt = quilt;
    position = 0;
  }

  public int position() {
    return position;
  }
  
  public int buttons() {
    return buttons;
  }

  public String name() {
    return name;
  }

  public QuiltBoard quilt() {
    return quilt;
  }
  
  public void buttonIncome(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("A button income can't be negative");
    }
    buttons += amount;
  }
  
  public void move(int position) {
    this.position = position;
  }
  

  /**
   * Buy the patch. Test if the player have enough buttons to buy the patch and if
   * it can be place on his quilt
   *
   * @param patch
   * @return true or false
   */
  public boolean buyAndPlacePatch(Patch patch) {
    Objects.requireNonNull(patch, "The patch can't be null");
    if (!canBuyPatch(patch) || !quilt.add(patch)) {
      return false;
    }
    buttons -= patch.price();
    return true;
  }

  public boolean canBuyPatch(Patch patch) {
    Objects.requireNonNull(patch, "The patch can't be null");
    return patch.price() <= buttons;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Player o
        && name.equals(o.name);
  }

  @Override
  public String toString() {
    return "[" + name + "] buttons:" + buttons;
  }

  @Override
  public void drawOnCLI(PatchworkCLI ui) {
    ui.builder()
    .append(String.format("%5d|", position))
    .append(" " + name + " - buttons [" + buttons + "]");
  }

}
