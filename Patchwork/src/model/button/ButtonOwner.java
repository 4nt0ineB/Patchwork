package model.button;

import java.util.Objects;

/**
 * 
 * Defines in game entities behavior
 * able to exchange buttons (as money)
 * 
 *  <p>
 *  The money of the game (the buttons) must circulate through button owner hands.
 *  Therefore money can't be created out of nothing 
 *  and shall respect the money circulation principle.
 *
 */
public abstract class ButtonOwner implements Comparable<ButtonOwner>{
  private int buttons;
  
  public ButtonOwner(int buttons) {
    if(buttons < 0) {
      throw new IllegalArgumentException("The amount of buttons must be positive");
    }
    this.buttons = buttons;
  }
  
  public boolean canPay(int amount) {
    if(buttons < 0) {
      throw new IllegalArgumentException("The amount of buttons must be positive");
    }
    return buttons >= amount;
  }
  
  public boolean canBuy(ButtonValued thing) {
    return canPay(thing.value());
  }
  
  
  public boolean pay(ButtonOwner owner, int amount) {
    if(!canPay(amount)) {
      return false;
    }
    buttons -= amount;
    owner.buttons += amount;
    return true;
  }

  public boolean payFor(ButtonOwner owner, ButtonValued thing) {
    Objects.requireNonNull(owner, "owner can't be null");
    Objects.requireNonNull(thing, "thing can't be null");
    return pay(owner, thing.value());
  }
  
  @Override
  public int compareTo(ButtonOwner o) {
    return Integer.compare(buttons, o.buttons);
  }
  
  public int buttons() {
    return buttons;
  }
  
}
