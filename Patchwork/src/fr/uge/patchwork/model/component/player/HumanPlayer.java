package fr.uge.patchwork.model.component.player;

import java.util.Objects;

import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.patch.LeatherPatch;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;

public class HumanPlayer implements Player {
  
  private final String name;
  private final QuiltBoard quilt;
  private int position;
  private boolean specialTile;
  private int buttons;

  public HumanPlayer(String name, int buttons, QuiltBoard quilt) {
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.quilt = quilt;
    this.buttons = buttons;
  }

  public int position() {
    return position;
  }
  
  public String name() {
    return name;
  }
  
  public int buttons() {
    return buttons;
  }
  

  public QuiltBoard quilt() {
    return quilt;
  }
  
  @Override
  public void move(int position) {
    this.position = position;
  }
  

  public boolean specialTile() {
    return specialTile;
  }
  
 
  /**
   * Buy the patch. Test if the player have enough buttons to buy the patch and if
   * it can be place on his quilt
   *
   * @param patch
   * @return true or false
   */
  public boolean placePatch(Patch patch) {
    Objects.requireNonNull(patch, "The patch can't be null");
    if(patch instanceof RegularPatch) {
      buttons -= ((RegularPatch) patch).price();
    }
    return quilt.add(patch);
  }
  

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof HumanPlayer o
        && name.equals(o.name);
  }

  @Override
  public String toString() {
    return "[" + name + "] buttons:" + buttons;
  }

  public void earnSpecialTile() {
  	specialTile = true;
  }
  
  /**
   * Returns the score of the player following Game rules
   * 
   * @return void
   */
  public int score() {
  	return buttons 
  	    + (specialTile ? 7 : 0) 
  	    - (quilt.countEmptySpaces() * 2);
  }

  public boolean canAdd(RegularPatch patch) {
    return buttons >= patch.price();
  }
  
  public boolean canAdd(LeatherPatch patch) {
    return true;
  }

  public void addButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("The amount of buttons must be positive");
    }
    buttons += amount;
  }

  @Override
  public boolean isAutonomous() {
    return false;
  }


  
}
