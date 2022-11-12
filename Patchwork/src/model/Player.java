package model;

import java.util.Objects;

import model.button.ButtonOwner;
import util.xml.XMLElement;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class Player extends ButtonOwner implements DrawableOnCLI {
  
  private final String name;
  private final QuiltBoard quilt;
  private int position;

  public Player(String name, int buttons, QuiltBoard quilt) {
    super(buttons);
    Objects.requireNonNull(name, "The player must have a name");
    if (buttons < 0) {
      throw new IllegalArgumentException("The player can't have debts at start-up");
    }
    this.name = name;
    this.quilt = quilt;
    position = 0;
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
    .append(" " + name + " - buttons [" + buttons() + "]");
  }

  public static Player fromText(String text) {
    Objects.requireNonNull(text, "Can't make new player out of null String");
    var parameters = text.split("\\|");
    return new Player(parameters[0], 
        Integer.parseInt(parameters[1]),
        QuiltBoard.fromText(parameters[2]));
  }
  
  public static Player fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    return new Player(
        element.getByTagName("name").content(),
        Integer.parseInt(element.getByTagName("buttons").content()),
        QuiltBoard.fromXML(element.getByTagName("QuiltBoard")));
  }
  
}
