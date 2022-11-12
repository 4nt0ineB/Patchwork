package model.gameboard.event;

import java.util.Objects;

import model.Patch;
import model.gameboard.GameBoard;
import util.xml.XMLElement;
import view.cli.Color;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class Event implements DrawableOnCLI {

  private final Effect effect;
  private final boolean oneUse;
  private boolean active = true;
  private final int position;
  private EffectType type;

  public Event(EffectType type, boolean oneUse, int position, Effect effect) {
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
    return position > -1 && position >= n && position <= m;
  }

  public void run(GameBoard gameboard) {
    if (effect.run(gameboard) && oneUse) {
      active = false;
    }
  }

  public Boolean runEachTurn() {
    return true;
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
      case BUTTON_INCOME ->  Effect.makeButtonIncomeEffect();
      case PATCH_INCOME -> Effect.makePatchIncomeEffect(Patch.fromXML(element.getByTagName("patch")));
      case SPECIAL_TILE -> {
          throw new IllegalArgumentException("This type event must be implemented !");
      }
       default -> {
          throw new IllegalArgumentException("This type does not exists. ("+ type + ")");
       }
    };
    return new Event(type, oneUse, position, effect);
  }
}