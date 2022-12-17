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
}
