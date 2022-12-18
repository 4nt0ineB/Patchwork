package fr.uge.patchwork.model.component.player.automa;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record NormalCard(int virtualButtons, int buttonIncome, List<CardFilter> filters) implements Card{
  
  public NormalCard {
    if(virtualButtons < 0) {
      throw new IllegalArgumentException("The virtual buttons can't be negative");
    }
    if(buttonIncome < 0) {
      throw new IllegalArgumentException("The virtual buttons can't be negative");
    }
    filters = List.copyOf(filters);
  }
  
  public static NormalCard fromText(String txt) {
    Objects.requireNonNull(txt, "The string can't be null");
    var data = txt.split(",");
    var virtualButtons = Integer.parseInt(data[0]);
    var buttonIncome =  Integer.parseInt(data[1]);
    var filters = new ArrayList<CardFilter>();
    for(var i = 2; i < data.length; i++) {
      filters.add(CardFilter.valueOf(data[i]));
    }
    return new NormalCard(virtualButtons, buttonIncome, filters);
  }

  @Override
  public boolean tactical() {
    return false;
  }
  
}
