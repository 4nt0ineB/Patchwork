package fr.uge.patchwork.model.component.player.automa;

import java.util.List;

public interface Card {
  int virtualButtons();
  int buttonIncome();
  List<CardFilter> filters();
  boolean tactical();
}
