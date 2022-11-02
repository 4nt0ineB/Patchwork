package model.gameboard;

public interface GameBoardFactory {
  // Design pattern 
  // explained here 
  // https://refactoring.guru/fr/design-patterns/abstract-factory
  GameBoard makeBoard();
}
