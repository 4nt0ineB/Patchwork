package model.gameboard.event;

import java.util.Objects;

import model.Patch;
import model.gameboard.ConditionalEffect;
import model.gameboard.GameBoard;
import view.cli.Color;
import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public sealed class Event implements DrawableOnCLI permits PositionedEvent {

  private final ConditionalEffect effect;
  private final boolean oneUse;
  private boolean active = true;
  private EventType type;

  public Event(EventType type, boolean oneUse, ConditionalEffect effect) {
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
    return true;
  }

  public Boolean active() {
    return active;
  }

  @Override
  public String toString() {
    return "oneUse: " + oneUse + ", active: " + active;
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

  public static Event fromText(String text) {
    Objects.requireNonNull(text, "Can't make new event out of null String");
    var parameters = text.split("\\|");
    var type = EventType.valueOf(parameters[0]);
    return switch(type) {
      case BUTTON_INCOME -> {
       yield new Event(type, 
           Boolean.parseBoolean(parameters[1]), 
           ConditionalEffect.makeButtonIncomeEffect());
      }
      case PATCH_INCOME -> {
        yield new Event(type, 
            Boolean.parseBoolean(parameters[1]), 
            ConditionalEffect.makePatchIncomeEffect(Patch.fromText(parameters[3])));
  
      }
      default -> {
        throw new AssertionError("This type of event doesn't exists");
      }
    };
  }
}