package fr.uge.patchwork.model.component;

import java.util.Objects;

import fr.uge.patchwork.model.component.button.ButtonBank;
import fr.uge.patchwork.model.component.button.ButtonOwner;
import fr.uge.patchwork.model.component.button.ButtonValued;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

public class Player implements ButtonOwner, DrawableOnCLI, Comparable<Player> {
  
  private final String name;
  private final QuiltBoard quilt;
  private int position;
  private int specialTile;
  private final ButtonBank buttonBank;

  public Player(String name, int buttons, QuiltBoard quilt) {
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.quilt = quilt;
    position = 0;
    buttonBank = new ButtonBank(buttons);
  }

  public int position() {
    return position;
  }
  
  public String name() {
    return name;
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
    return "[" + name + "] buttons:" + buttons();
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    ui.builder()
    .append(String.format("%5d|", position))
    .append(" " + name + " - buttons [" + buttons() + "]")
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
  	return buttons() + specialTile * 7 - (quilt.countEmptySpaces() * 2);
  }

  @Override
  /**
   * Compare on the score
   */
  public int compareTo(Player o) {
    return Integer.compare(score(), o.score());
  }

  @Override
  public boolean canPay(int amount) {
    return buttonBank.canPay(amount);
  }

  @Override
  public boolean canBuy(ButtonValued thing) {
    return buttonBank.canBuy(thing);
  }

  @Override
  public void pay(ButtonOwner owner, int amount) {
    buttonBank.pay(owner, amount);
  }

  @Override
  public void payOwnerFor(ButtonOwner owner, ButtonValued thing) {
    buttonBank.payOwnerFor(owner, thing);
  }

  @Override
  public int buttons() {
    return buttonBank.buttons();
  }

  @Override
  public ButtonBank buttonBank() {
    return buttonBank;
  }
  
}
