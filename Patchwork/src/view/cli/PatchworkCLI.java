package view.cli;

import java.util.Scanner;

import model.GameBoard;
import model.Patch;
import model.Player;
import view.Action;
import view.UserInterface;

public class PatchworkCLI implements UserInterface {

//  private static enum TurnChoices {
//    MOVE('m')
//    , PICKPATCH('p')
//    , TEST('d')
//    ;
//    
//    private final char inputValue;
//    
//    TurnChoices(char input) {
//      inputValue = input;
//    }
//  }
  
  private static Scanner scanner = new Scanner(System.in);

  @Override
  public void draw(GameBoard gb) {
    System.out.println(Color.ANSI_GREEN + "Hey");
    
  }
  
  public void drawPlayer(Player player) {
    
  }

  @Override
  public void clear() {
    System.out.print("\033[H\033[2J");
//    System.out.flush();
  }

  @Override
  public Action choice() {
    return Action.QUIT;
  }

  @Override
  public Action getPlayerActionForTurn() {
    Action action = Action.QUIT;
    boolean validInput = true;
    do {
      System.out.print("What to do: ");
      var c = scanner.nextLine().charAt(0);
      switch(c) {
        case 'd' -> action = Action.PICK_PATCH;
        case 'q' -> action = Action.QUIT;
        default -> {
          System.out.println("Wrong choice.");
          validInput = false;
        }
      }
    }while(!validInput);
    return action;
  }
  
  /**
   * Close the interface: <br>
   * 
   * close scanner on {@link System.in}
   */
  public void close() {
    scanner.close();
    System.out.println("Bye");
    
  }

  @Override
  public void movePatch(Patch patch) {
    // TODO Auto-generated method stub
    
  }

}
