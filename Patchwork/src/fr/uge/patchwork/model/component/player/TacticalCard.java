package fr.uge.patchwork.model.component.player;

import java.util.List;

import fr.uge.patchwork.model.component.player.automa.Card;
import fr.uge.patchwork.model.component.player.automa.CardFilter;
import fr.uge.patchwork.model.component.player.automa.NormalCard;

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
