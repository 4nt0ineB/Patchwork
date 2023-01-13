package fr.uge.patchwork.model.component.player.automa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 
 * Enumerates the implemented deck type for Automa
 *
 */
public enum DeckType {
  NORMAL
  , TACTICAL
  ;

  /**
   * Built a deck of card for the given deck type
   * @param type
   * @return a list of card
   * @throws IOException if an parsing error occur in the setting files
   */
  public static List<Card> fromType(DeckType type) throws IOException{
    Path path;
    Function<String, Card> builder;
    switch(type) {
      case NORMAL -> {
        path = Path.of("resources/settings/automa/cards/normal");
        builder = NormalCard::fromText;
      }
      case TACTICAL -> {
        path = Path.of("resources/settings/automa/cards/tactical");
        builder = TacticalCard::fromText;
      }
      default -> throw new IllegalArgumentException("This type of deck does not exists");
    }
    var deck = new ArrayList<Card>();
    try (var reader = Files.newBufferedReader(path)){
      String line;
      while((line = reader.readLine()) != null) {
        line = line.replace(" ", "");
        deck.add(builder.apply(line));
      }
    }
    return deck;
  }
}
