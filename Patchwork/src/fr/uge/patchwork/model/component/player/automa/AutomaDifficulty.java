package fr.uge.patchwork.model.component.player.automa;

public enum AutomaDifficulty {
  INTERN(1)
  , APPRENTICE(9)
  , FELLOW(12)
  , MASTER(15)
  , LEGEND(18);
  
  private final int spaces;
  
  private AutomaDifficulty(int spaces) {
    this.spaces = spaces;
  }
  
  public int spaces() {
    return spaces;
  }
  
  @Override
  public String toString() {
    return this.name();
  }
}
