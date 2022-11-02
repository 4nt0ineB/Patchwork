package model.event;

import java.util.Objects;

import model.gameboard.Effect;
import model.gameboard.GameBoard;
import view.cli.Color;
import view.cli.DisplayableOnCLI;
import view.cli.PatchworkCLI;

public class Event implements DisplayableOnCLI {

  private final Effect effect;
  private final boolean oneUse;
  private boolean active = true;
  private EventType type;

  public Event(EventType type, boolean oneUse, Effect effect) {
    this.type = Objects.requireNonNull(type, "Type can't be null");
    this.effect = Objects.requireNonNull(effect, "Effect can't be null");
    this.oneUse = oneUse;
  }

  public void run(GameBoard gameboard) {
    if (effect.run(gameboard) && oneUse) {
      active = false;
    }
  }

  /**
   * Test if the event is positioned inside a given interval
   * 
   * @param n
   * @param m
   * @return
   */
  public Boolean isPositionedBetween(int n, int m) {
    return false;
  }

  public Boolean runEachTurn() {
    return false;
  }

  public Boolean active() {
    return active;
  }

  @Override
  public String toString() {
    return "oneUse: " + oneUse + ", active: " + active;
  }

  @Override
  public void drawOnCLI(PatchworkCLI ui) {
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

}