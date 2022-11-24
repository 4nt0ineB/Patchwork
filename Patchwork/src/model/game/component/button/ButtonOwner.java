package model.game.component.button;

public interface ButtonOwner {
  
  /**
   * @param amount
   * @throws IllegalArgumentException If the given amount is negative
   * @return true if can pay, else false
   */
  public boolean canPay(int amount);
  
  /**
   * 
   * @param thing
   * @return
   */
  public boolean canBuy(ButtonValued thing);
  
   /* Pay a button owner for a given amount
   * @param owner
   * @param amount
   * @throws IllegalArgumentException If cannot pay the owner
   */
  public void pay(ButtonOwner owner, int amount);
       
  /**
   * Pay a button owner for a button valued thing
   * @param owner
   * @param thing
   */
  public void payOwnerFor(ButtonOwner owner, ButtonValued thing);
  
  public int buttons();
  
  public ButtonBank buttonBank();
}
