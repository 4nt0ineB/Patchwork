package fr.uge.patchwork.model.component.player;

import java.util.LinkedList;
import java.util.Objects;

import fr.uge.patchwork.model.component.patch.Patch;

public class Automa implements Player {
  private final LinkedList<Patch> patches = new LinkedList<>();
  private int position;
  private final AutomaDifficulty difficulty;
  private boolean specialTile = false;
  private int buttons;
  
  public Automa(AutomaDifficulty difficulty) {
    this.difficulty = Objects.requireNonNull(difficulty);
  }
  
  @Override
  public void move(int position) {
    this.position = position;
  }

  @Override
  public int position() {
    return position;
  }

  @Override
  public int score() {
    switch(difficulty) {
    case APPRENTICE:
      break;
    case FELLOW:
      break;
    case INTERN:
      break;
    case LEGEND:
      break;
    case MASTER:
      break;
    default:
      break;
    }
    
    return -999999999;
  }

  @Override
  public void earnSpecialTile() {
    specialTile = true;
  }

  @Override
  public String name() {
    return "Automa";
  }

  @Override
  public int buttons() {
    return buttons;
  }
  
  @Override
  public void addButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("The amount of buttons must be positive");
    }
    buttons += amount;
  }

  @Override
  public boolean specialTile() {
    return specialTile;
  }

}
