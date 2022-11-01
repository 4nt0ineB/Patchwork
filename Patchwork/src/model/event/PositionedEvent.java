package model.event;

import model.Effect;

public class PositionedEvent extends Event {

  private final int position;

  public PositionedEvent(int position, boolean oneUse, Effect effect) {
    super(oneUse, effect);
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
}