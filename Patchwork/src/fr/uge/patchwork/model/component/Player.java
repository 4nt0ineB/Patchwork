package fr.uge.patchwork.model.component;

import java.util.Objects;

import fr.uge.patchwork.model.component.patch.LeatherPatch;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

public class Player implements DrawableOnCLI, Comparable<Player> {
  
  private final String name;
  private final QuiltBoard quilt;
  private int position;
  private int specialTile;
  private int buttons;

  public Player(String name, int buttons, QuiltBoard quilt) {
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.quilt = quilt;
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
  public boolean placePatch(Patch patch) {
    Objects.requireNonNull(patch, "The patch can't be null");
    return quilt.add(patch);
  }
  
  /**
   * Buy the patch. Test if the player have enough buttons to buy the patch and if
   * it can be place on his quilt
   *
   * @param patch
   * @return true or false
   */
  public boolean placePatch(RegularPatch patch) {
    if(placePatch(patch)) {
      buttons -= patch.price();
      return true;
    }
    return false;
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
  public void drawOnCLI(CommandLineInterface ui) {
    ui.builder()
    .append(String.format("%5d|", position))
    .append(" " + name + " - buttons [" + buttons + "]")
    .append(specialTile > 0 ? " SpecialTile : " + specialTile : "");
  }

  public static Player fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    return new Player(
        element.getByTagName("name").content(),
        Integer.parseInt(element.getByTagName("buttons").content()),
        QuiltBoard.fromXML(element.getByTagName("QuiltBoard")));
  }
  
  public void earnSpecialTile() {
  	specialTile = 1;
  }
  
  /**
   * Returns the score of the player following Game rules
   * 
   * @return void
   */
  public int score() {
  	return buttons + specialTile * 7 - (quilt.countEmptySpaces() * 2);
  }
  

  public boolean canAdd(RegularPatch patch) {
    return buttons >= patch.price();
  }
  
  public boolean canAdd(LeatherPatch patch) {
    return true;
  }
  

  @Override
  /**
   * Compare on the score
   */
  public int compareTo(Player o) {
    return Integer.compare(score(), o.score());
  }

  public void addButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("The amount of buttons must be positive");
    }
    buttons += amount;
  }

  
}
