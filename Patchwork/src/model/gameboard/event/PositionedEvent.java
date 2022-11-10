package model.gameboard.event;

import java.util.Objects;

import model.Patch;
import model.gameboard.ConditionalEffect;
import view.cli.CommandLineInterface;

public final class PositionedEvent extends Event {

  private final int position;

  public PositionedEvent(EventType type, int position, boolean oneUse, ConditionalEffect effect) {
    super(type, oneUse, effect);
    this.position = position;
  }

  @Override
  public Boolean isPositionedBetween(int n, int m) {
    return position >= n && position <= m;
  }

  @Override
  public String toString() {
    return "[Positioned: " + position + ") , " + super.toString();
  }
  
  public static PositionedEvent fromText(String text) {
    Objects.requireNonNull(text, "Can't make new event out of null String");
    var parameters = text.split("\\|(?=[^\"]*(?:\"[^\"]*\")*$)");
    var type = EventType.valueOf(parameters[0]);
    return switch(type) {
      case BUTTON_INCOME -> {
       yield new PositionedEvent(type, 
           Integer.parseInt(parameters[2]), 
           Boolean.parseBoolean(parameters[1]),
           ConditionalEffect.makeButtonIncomeEffect());
      }
      case PATCH_INCOME -> {
        yield new PositionedEvent(type, 
            Integer.parseInt(parameters[2]), 
            Boolean.parseBoolean(parameters[1]),
            ConditionalEffect.makePatchIncomeEffect(Patch.fromText(parameters[3].replaceAll("\"", ""))));
      }
      default -> {
        throw new AssertionError("This type of event doesn't exists");
      }
    };
  }
  
}