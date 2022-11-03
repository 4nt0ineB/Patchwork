package model.gameboard.event;

import model.gameboard.Effect;

public class OnTurnEvent extends Event {
  OnTurnEvent(EventType type, boolean oneUse, Effect effect) {
    super(type, oneUse, effect);
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