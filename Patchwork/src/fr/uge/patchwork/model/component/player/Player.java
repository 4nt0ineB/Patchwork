package fr.uge.patchwork.model.component.player;

public interface Player extends Comparable<Player>  {
  int position();
  int score();
  void move(int position);
  void earnSpecialTile();
  String name();
  int buttons();
  void addButtons(int amount);
  
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
