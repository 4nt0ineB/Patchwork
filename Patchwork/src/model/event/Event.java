package model.event;

import java.util.Objects;

import model.Effect;
import model.GameBoard;

public class Event {
  private final Effect effect;
  private final boolean oneUse;
  private boolean active = true;

  public Event(boolean oneUse, Effect effect) {
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

}