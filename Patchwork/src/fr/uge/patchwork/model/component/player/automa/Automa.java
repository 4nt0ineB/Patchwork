package fr.uge.patchwork.model.component.player.automa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.model.component.player.Player;

public class Automa implements Player {
  private final LinkedList<Patch> patches = new LinkedList<>();
  private final List<NormalCard> deck;
  private int position;
  private final AutomaDifficulty difficulty;
  private boolean specialTile = false;
  private int buttons;
  
  private int currentCard;
  
  public Automa(AutomaDifficulty difficulty, List<NormalCard> cards) {
    this.difficulty = Objects.requireNonNull(difficulty);
    deck = new ArrayList<>(cards);
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
  
  public AutomaDifficulty difficulty() {
    return difficulty;
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
  
  public NormalCard card() {
    return deck.get(currentCard);
  }
  
  public void discardCard() {
    if(currentCard == deck.size() - 1) {
      currentCard = 0;
      Collections.shuffle(deck);
    }else {
      currentCard++;
    }
  }

  @Override
  public boolean isAutonomous() {
    return true;
  }

  public void add(RegularPatch patch) {
    patches.add(patch);
  }

}
