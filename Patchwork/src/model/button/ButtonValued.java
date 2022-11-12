package model.button;

/**
 * Defines API of exchangeable/purchasable items of the game in button
 *
 */
public interface ButtonValued {
  
  /**
   * Returns the amount of buttons the object is valued at.
   * The value/the price.
   * @return
   */
  int value();
}
