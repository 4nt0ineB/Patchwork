package model.event;

import model.Effect;

public class OnTurnEvent extends Event {
  OnTurnEvent(boolean oneUse, Effect effect) {
    super(oneUse, effect);
  }

  @Override
  public Boolean runEachTurn() {
    return true;
  }

  @Override
  public String toString() {
    return "[OnTurn)  " + ", " + super.toString();
  }
}