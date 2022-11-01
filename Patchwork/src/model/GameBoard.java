package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import view.cli.Color;
import view.cli.DisplayableOnCLI;



public class GameBoard implements DisplayableOnCLI {
  
  
  private static class Event {
    private final Effect effect;
    private int position;
    private boolean eachTurn;
    private boolean consumable;
    
    Event(int position, int uses, boolean eachTurn, boolean consumable, Effect effect) {
      if(position < 0) {
        throw new IllegalArgumentException("The position of the effect can't be negative");
      }
      if(uses < 0) {
        throw new IllegalArgumentException("The effect must be used at least 1 time.");
      }
      this.position = position;
      this.eachTurn = eachTurn;
      this.consumable = consumable;
      this.effect = Objects.requireNonNull(effect, "Effect can't be null");
    }
    
  } ;
  
  public static enum InGameAction {
    SELECT_PATCH(List.of("ADVANCE")),
    ADVANCE(List.of("SELECT_PATCH"))
    ;
    
    private final List<String> incompatible;
    
    InGameAction(List<String> incompatible){
      this.incompatible = incompatible;
    }
    
    public List<InGameAction> deactivate(){
      return incompatible.stream().map(InGameAction::valueOf).toList();
    }
    
    private void execute(GameBoard gameBoard) {
      switch(this) {
        case ADVANCE -> {
          gameBoard.currentPlayerAdvance();
        }
        case SELECT_PATCH -> {
          
        }
        default -> {}
      }
    }
  }
  

  
  // Max number of patches the player can choose from
  private final int nextPatches = 3;
  // Number of squares on the board
  private final int size = 53;
  // The bank
  private int buttons;
  // The patches
  private int neutralTokenPos;
  private final ArrayList<Patch> patches;
  private int selectedPatch;
  private final Queue<Patch> patchesEarned = new LinkedList<>();
  //Current player is always at the top of the stack
  private int currentPlayerPos;
  // Players indexed by position
  private final SortedMap<Integer, Stack<Player>> players = new TreeMap<>();
  //
  private final ArrayList<Event> events = new ArrayList<>();
  
  /**
   * GameBoard constructor
   * @param size the number of squares on the board
   * @param patches the patches around the board at the beginning
   * @param players the players
   */
  private GameBoard(int buttons, List<Patch> patches, List<Player> players, ArrayList<Event> events){
    Objects.requireNonNull(patches, "List of patches can't be null");
    Objects.requireNonNull(players, "List of players can't be null");
    if(patches.size() < 1) {
      throw new IllegalArgumentException("There must be at least 1 patches");
    }
    if(players.size() < 2) {
      throw new IllegalArgumentException("There must be at least 2 players");
    }
    this.buttons = buttons;
    this.patches = new ArrayList<>(patches);
    this.players.put(0, new Stack<Player>());
    this.players.get(0).addAll(players);
    this.events.addAll(events);
  }
  
  private void init() {
    Collections.shuffle(this.patches);
    Collections.shuffle(this.players.get(0));
    currentPlayerPos = 0;
    // delegate
    this.players.get(0).stream().forEach(p -> p.buttonIncome(getButtons(5)));
    neutralTokenPos = Patch.minPatch(this.patches);
    // @Todo check min square size to fit each patch ?
  }
  
  /**
   * Make and initialize a new basic Patchwork game board 
   * @return
   */
  public static GameBoard makeBoard() {
    // Basic version
    // turn this into config files ?
    
    
    var patches = new ArrayList<Patch>();
    for(var i = 0; i < 20; i++) {
      patches.add(new Patch(
          1, 
          4, 
          3
          , List.of(
            new Coordinates(0, 0),
            new Coordinates(0, 1),
            new Coordinates(1, 0),
            new Coordinates(1, 1)
            )
          , new Coordinates(0, 0)
          ));
     patches.add(
         new Patch(0, 2, 2
             , List.of(
//               new Coordinates(0, 0),
//               new Coordinates(0, 1),
//               new Coordinates(1, 0),
//               new Coordinates(1, 1)
               new Coordinates(0, 0),
               new Coordinates(-1, 0),
               new Coordinates(-1, 1),
               new Coordinates(1, 0)
               )
             , new Coordinates(0, 0)
             ));
    }
    var player1 = new Player("Player 1", 0, new QuiltBoard(9, 9));
    var player2 = new Player("Player 2", 0, new QuiltBoard(9, 9));
    
    var events = new ArrayList<Event>();
    events.add(
        new Event(2, 0, false, false, (GameBoard gb) -> { System.out.println(gb.neutralTokenPos); } )
        );
    
    var gameBoard = new GameBoard(162, patches, List.of(player1, player2), events);
    gameBoard.init();
    System.out.println(InGameAction.ADVANCE.deactivate());
    return gameBoard;
  }
  
  public Patch patchEarned() {
    return patchesEarned.poll();
  }
  
  public Player currentPlayer() {
    return players.get(currentPlayerPos).peek();
  }
  
  /**
   * Subtract amount of buttons from the game board buttons.
   * if it goes below 0, return the maximum.
   * @param 
   * @return the maximum amount possible for the amount asked
   */
  private int getButtons(int amount) {
    if(amount < 0) {
      throw new IllegalArgumentException("Can't get " + amount  + " buttons");
    }
    var max = buttons - amount < 0 ? buttons : amount;
    buttons -= max;
    return max;
  }
  
  /**
   * Advance the current player
   */
  public void currentPlayerAdvance() {
    if(!currentPlayerCanAdvance()) {
      return;
    }
    int newPosition = nextPositionWithPlayers(currentPlayerPos + 1) + 1;
    int buttonIncome = newPosition - currentPlayerPos;
    currentPlayerMove(newPosition);
    currentPlayer().buttonIncome(buttonIncome);
  }
  
  /**
   * Move the current player at the given position
   * @param newPosition
   */
  private void currentPlayerMove(int newPosition) {
    if(newPosition < 0 || newPosition > size) {
      throw new IllegalArgumentException("The new position can't exceed boundaries (0 <= newp: " + newPosition + "<= " + size);
    }
    var player = players.get(currentPlayerPos).pop();
    var playersAtPos = players.get(newPosition);
    if(playersAtPos == null) {
      players.put(newPosition, new Stack<>());
    }
    players.get(newPosition).add(player);
    currentPlayerPos = newPosition;
  }
  
  /**
   * Test if the current player can advance on the board
   * The player can advance if he has not reached the end
   * and if a player is ahead of him
   * @return true or false
   */
  public boolean currentPlayerCanAdvance() {
    return currentPlayerPos != size && nextPositionWithPlayers(currentPlayerPos + 1) != -1;
  }
  
  /**
   * Test if 
   * @return
   */
  public boolean currentPlayerCanChosePatch() {
    var nextPatches = nextPatches();
    if(nextPatches.isEmpty()) {
      return false;
    }
    return nextPatches.stream().anyMatch(patch -> currentPlayer().canBuyPatch(patch) == true);
  }
  
  /**
   * Set the next currentPlayer 
   * @return
   */
  public void nextTurn() {
    currentPlayerPos = nextPositionWithPlayers(0); // the most behind player
    unselectPatch();
    patchesEarned.clear();
    if(!currentPlayerCanAdvance() 
        && !currentPlayerCanChosePatch()
        ){
      throw new AssertionError("Unwanted game state. Player is stucked. "
          + "Probably bad init settings for the game board");
    }
  }
  
  /**
   * Find the first next position where there are players
   * from a given position
   * 
   * 
   * @param pos Start position for searching 
   * @return the position, or -1 if not players found
   */
  private int nextPositionWithPlayers(int pos) {
    if(pos < 0 || pos > size) {
      throw new IllegalArgumentException("Invalid position. 0 < [" + pos + "] <= " + size);
    }
    for(var i = pos; i < size; i++) {
      var playersAtPos = players.get(i);
      if(playersAtPos != null && !playersAtPos.isEmpty()) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Return a list of the next available patches
   * for this turn
   * @return
   */
  public List<Patch> nextPatches(){
    var nextPatchesForTurn = new ArrayList<Patch>();
    // NEXTPATCHES *after* the token 
    for(var i = 1; i <= nextPatches; i++) {
      var patch = nextPatch(neutralTokenPos + i);
      nextPatchesForTurn.add(patch);
    }
    return nextPatchesForTurn;
  }
  
  /**
   * Select a patch among the next available
   * @param patch
   */
  public void selectPatch(int i) {
//    Objects.requireNonNull(patch, "The path can't be null");
    var nextPatches = nextPatches();
    if(nextPatches.isEmpty()) {
      return;
    }
    if(i < 0 || i >= nextPatches.size()) {
      throw new IllegalArgumentException("Given index does'nt exists for the nextPatches");
    }
    selectedPatch = neutralTokenPos + 1 + i;
    //    for(var i = 0; i < nextPatches; i++) {
//      // We want to compare pointers !
//      if(nextPatch(neutralTokenPos + 1 + i) == patch) {
//        selectedPatch = neutralTokenPos + 1 + i;
//      }
//    }
  }
  
  public Patch selectedPatch() {
    if(selectedPatch == -1) {
      throw new AssertionError("A patch should have been selected first");
    }
    return patches.get(selectedPatch);
  }
  
  public void unselectPatch() {
    selectedPatch = -1;
  }
  
  public boolean playSelectedPatch() {
    if(!currentPlayer().buyAndPlacePatch(selectedPatch())){
      return false;
    }
    // Moves
    var newPosition = Math.min(size, currentPlayerPos + selectedPatch().moves());
    currentPlayerMove(newPosition);
    // Buttons
    var patch = patches.remove(selectedPatch);
    buttons += patch.price();
    //
    neutralTokenPos = selectedPatch % patches.size();
    unselectPatch();
    return true;
  }
  
  /**
   * Return the next patch after index 
   * by using modulo on the number of patches
   * (loop trough patches)
   * @param index
   * @return the patch or null
   */
  private Patch nextPatch(int index) {
    if(patches.size() == 0) {
      return null;
    }
    return patches.get(index % patches.size());
  }
  
  @Override
  public void drawOnCLI() {
    for(var position: players.entrySet()) {
      for(var player: position.getValue()) {
        System.out.print("Position " + position.getKey() + "| ");
        if(player == currentPlayer()) {
          System.out.print(Color.ANSI_GREEN);
        }
        player.drawOnCLI();
        System.out.print(Color.ANSI_RESET);
      }
    }
    System.out.println();
  }
  
}
