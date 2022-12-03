package fr.uge.patchwork.model.component.gameboard.event;

import java.util.Objects;
import java.util.function.Predicate;

import fr.uge.patchwork.model.component.Patch;
import fr.uge.patchwork.model.component.gameboard.GameBoard;
import fr.uge.patchwork.util.xml.XMLElement;
import fr.uge.patchwork.view.cli.Color;
import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

/**
 * 
 * Implement an event on the game board
 * 
 * <p>
 * The event are triggered depending of their position 
 *
 */
public class Event implements DrawableOnCLI {

  // The action to perform during the event
  private final Predicate<GameBoard> effect;
  // indicate if the event is a one use event
  private final boolean oneUse;
  // by default an event is active
  private boolean active = true;
  private final int position;
  private EffectType type;
  
  /**
   * 
   * @param type
   * @param oneUse
   * @param position a strictly negative value for an event to be triggered each turn like a routine (not positioned), or
   * a positive value for the event to be triggered at a specific space on the board
   * @param effect
   */
  public Event(EffectType type, boolean oneUse, int position, Predicate<GameBoard> effect) {
    this.type = Objects.requireNonNull(type, "Type can't be null");
    this.effect = Objects.requireNonNull(effect, "Effect can't be null");
    this.oneUse = oneUse;
    this.position = position;
  }
  
  /**
   * Test if the event is positioned inside a given interval
   * 
   * @param n start (included)
   * @param m end (included)
   * @return
   */
  public Boolean isPositionedBetween(int n, int m) {
    return !runEachTurn() && position >= n && position <= m;
  }
  
  public int position() {
  	return position;
  }
  
  public boolean oneUse() {
  	return oneUse;
  }
  
  public EffectType type() {
    return type;
  }

  public boolean run(GameBoard gameboard) {
    if (effect.test(gameboard) && oneUse) {
      active = false;
      return true;
    }
    return false;
  }

  public Boolean runEachTurn() {
    return position < 0;
  }

  public Boolean active() {
    return active;
  }

  @Override
  public String toString() {
    return  "{" + (position > -1 ? "Position: " + position + ", " : ", ")
        +  "oneUse: " + oneUse + ", active: " + active + "}";
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    var text = switch (type) {
      case BUTTON_INCOME -> {
        yield Color.ANSI_BBLUE + "Button Income !";
      }
      case PATCH_INCOME -> {
        yield Color.ANSI_YELLOW + "You've got a patch !";
  
      }
      case SPECIAL_TILE -> {
        yield Color.ANSI_PURPLE + "You've got a special tile !";
      }
      default -> {
        yield "";
      }
    };
    ui.addMessage(text + Color.ANSI_RESET);
  }
  
  /**
   * Make a new Event from a XMLElement
   * @param element XMLELement 
   * @exception IllegalStateException If the XMLElement is empty
   * @return
   */
  public static Event fromXML(XMLElement element) {
    XMLElement.requireNotEmpty(element);
    var type = EffectType.valueOf(element.getByTagName("type").content());
    var position = Integer.parseInt(element.getByTagName("position").content());
    var oneUse = Boolean.parseBoolean(element.getByTagName("oneUse").content());
    var effect = switch(type) {
      case BUTTON_INCOME ->  Effects.buttonIncome();
      case PATCH_INCOME -> Effects.patchIncome(Patch.fromXML(element.getByTagName("Patch")));
      case SPECIAL_TILE -> Effects.specialTile();
       default -> {
          throw new IllegalArgumentException("This type does not exists. ("+ type + ")");
       }
    };
    return new Event(type, oneUse, position, effect);
  }

  
  
}