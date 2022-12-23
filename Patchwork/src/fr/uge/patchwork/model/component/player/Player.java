package fr.uge.patchwork.model.component.player;


/**
 * Defines the players standards capabilities
 */
public interface Player extends Comparable<Player>  {
  
  /**
   * 
   * @return the position of the player
   */
  int position();
  
  /**
   * 
   * @return the score of the player
   */
  int score();
  
  /**
   * Change the player position
   * @param position the new position
   */
  void move(int position);
  
  /**
   * The player earn the special tile
   */
  void earnSpecialTile();
  
  /**
   * 
   * @return player name
   */
  String name();
  
  /**
   * 
   * @return the amount of button of the player
   */
  int buttons();
  
  /**
   * Give buttons to the player
   * @param amount
   */
  void addButtons(int amount);
  
  /**
   * 
   * @return false if the player is human, otherwise true
   */
  boolean isAutonomous();
  
  @Override
  /**
   * Compare on the score
   */
  public default int compareTo(Player o) {
    return Integer.compare(score(), o.score());
  }
  
  boolean specialTile();
  
}
