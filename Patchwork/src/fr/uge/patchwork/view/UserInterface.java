package fr.uge.patchwork.view;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.model.component.player.HumanPlayer;

public interface UserInterface {
  
  /**
   * Init the interface
   * @throws IOException
   */
  void init() throws IOException;
  
  /**
   * Draw the track board
   * @param trackboard
   */
  void draw(TrackBoard trackboard);
  
  /**
   * Draw the patch manager
   */
  void draw(PatchManager manager);
  
  /**
   * Draw the score board at the end of the game
   * @param trackboard
   */
  void drawScoreBoard(TrackBoard trackboard);
  
  /**
   * Draw a dummy quilt with a given patch to 
   * show if the patch could fit in
   * @param quilt
   * @param patch
   */
  void drawDummyQuilt(HumanPlayer player, Patch patch);
  
  /**
   * Print the interface
   */
  void display();
  
  /**
   * Clear the interface
   */
  void clear();
  
  /**
   * Close the interface
   */
  void close();
  
  /**
   * Display a menu as the game mode selection menu.
   * (The game menu)
   * for given choices
   * @return an optional keybindedChoice
   */
  Optional<KeybindedChoice> gameModeMenu(Set<KeybindedChoice> choices);
  
  /**
   * Display a menu with choices and a title
   * @param title
   * @param choices
   * @return
   */
  Optional<KeybindedChoice> simpleMenu(String title, Set<KeybindedChoice> choices);
  
  /**
   * Display a menu as the game mode selection menu.
   * (The game menu)
   * for given choices
   * @return an optional keybindedChoice
   */
  
  /**
   * Display a menu as the end game selection menu.
   * for given choices.
   * @return an optional keybindedChoice
   */
  Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices);
  
  /**
   * Display a menu as the menu for the turn.
   * for given choices
   * @param choices the available choices
   * @return an optional keybindedChoice
   */
  Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices);
  
  /**
   * Make the user chose a manipulation over a patch for given availables
   * manipulations
   * @param choices Available manipulations
   * @return
   */
  Optional<KeybindedChoice> manipulatePatch(Set<KeybindedChoice> choices);
  
  /**
   * Get a user input with the key of the key binded choices
   * @param choices Available choices
   * @return
   */
  Optional<KeybindedChoice> getInput(Set<KeybindedChoice> choices);
  
  /**
   * Make the user select a patch a given patch list
   * @param patches
   * @return the selected patch or null
   */
  Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager);
}
