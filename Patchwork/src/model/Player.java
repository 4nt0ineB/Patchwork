package model;

import java.util.Objects;

import view.cli.DisplayableOnCLI;

public class Player implements DisplayableOnCLI {
  private final String name;
  private int buttons;
  // private final List<SpecialTile> tiles;
  private final QuiltBoard quilt;

  public Player(String name, int buttons, QuiltBoard quilt) {
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.buttons = buttons;
    this.quilt = quilt;
  }

  public void buttonIncome(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("A button income can't be negative");
    }
    buttons += amount;
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
    return Objects.hash(buttons, name);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Player o && buttons == o.buttons && name.equals(o.name);
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

  @Override
  public String toString() {
    return "[" + name + "] buttons:" + buttons;
  }

  @Override
  public void drawOnCLI() {
    System.out.println("[" + name + "] buttons:" + buttons);
    System.out.println(quilt.patches());
  }

}
