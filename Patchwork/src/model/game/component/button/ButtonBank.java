package model.game.component.button;

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
public class ButtonBank implements ButtonOwner {
  
  private int buttons;
  
  /**
   * Make a new button bank
   * @param buttons
   * @throws IllegalArgumentException If the given 
   * amount of button is negative
   */
  public ButtonBank(int buttons) {
    if(buttons < 0) {
      throw new IllegalArgumentException("The amount of buttons can't be negative");
    }
    this.buttons = buttons;
  }
  
  @Override
  public boolean canPay(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("The amount of buttons can't be negative");
    }
    return buttons >= amount;
  }
  
  @Override
  public boolean canBuy(ButtonValued thing) {
    return canPay(thing.value());
  }
  
  @Override
  public void pay(ButtonOwner owner, int amount) {
    if(!canPay(amount)) {
      throw new IllegalArgumentException("Not enough money to pay " + amount + "buttons");
    }
    buttons -= amount;
    owner.buttonBank().buttons += amount;
  }
  
  @Override
  public void payOwnerFor(ButtonOwner owner, ButtonValued thing) {
    Objects.requireNonNull(owner, "owner can't be null");
    Objects.requireNonNull(thing, "thing can't be null");
    pay(owner, thing.value());
  }
  
  public int buttons() {
    return buttons;
  }

  @Override
  public ButtonBank buttonBank() {
    return this;
  }
  
}