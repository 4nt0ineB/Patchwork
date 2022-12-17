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
  private final LinkedList<RegularPatch> patches = new LinkedList<>();
  private final List<Card> deck;
  private int position;
  private final AutomaDifficulty difficulty;
  private boolean specialTile = false;
  private int buttons;
  
  private int currentCard;
  
  public Automa(AutomaDifficulty difficulty, List<Card> cards) {
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
    var score = 0;
    switch(difficulty) {
    case LEGEND:
      score += patches.size();
    case MASTER:
      score -= patches.size();
      score += buttonsOnPatches();
    case FELLOW:
      score += patches.size();
    case APPRENTICE:
      score += buttons;
    default:
      score += specialTile ? 7 : 0;
    }
    return score;
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
  
  public Card card() {
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

  public List<Patch> patches(){
    return List.copyOf(patches);
  }
  
  public int buttonsOnPatches() {
    return patches.stream().mapToInt(RegularPatch::buttons).sum();
  }
  
  @Override
  public boolean isAutonomous() {
    return true;
  }

  public void add(RegularPatch patch) {
    patches.add(patch);
  }

}
