package fr.uge.patchwork.model.component.player.automa;

import java.util.List;

/**
 * 
 * Implements a tactical card, that differentiate from a normal card
 * by providing information on the virtual buttons on its back
 *
 */
public class TacticalCard implements Card {
  private final NormalCard card;
  
  public TacticalCard(int virtualButtons, int buttonIncome, List<CardFilter> filters) {
    card = new NormalCard(virtualButtons, buttonIncome, filters);
  }
  
  private TacticalCard(NormalCard card) {
    this.card = card;
  }
  
  @Override
  public int virtualButtons() {
    return card.virtualButtons();
  }
  
  @Override
  public int buttonIncome() {
    return card.buttonIncome();
  }
  
  @Override
  public List<CardFilter> filters() {
    return card.filters();
  }
  
  @Override
  public boolean tactical() {
    return true;
  }
 
  public static TacticalCard fromText(String txt) {
    return new TacticalCard(NormalCard.fromText(txt));
  }
  
}
