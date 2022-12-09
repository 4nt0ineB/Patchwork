package fr.uge.patchwork.view;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.QuiltBoard;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.Patch2D;
import fr.uge.patchwork.model.component.patch.RegularPatch;

public interface UserInterface {
  
  /**
   * Draw the game splash screen
   */
  void drawSplashScreen();
  
  void draw(Player player);
  
  void draw(TrackBoard trackboard);
  
  void draw(PatchManager patchmanager);
  
  void draw(Patch2D patch);
  
  void draw(RegularPatch patch);
  
  void drawScoreBoard(TrackBoard trackboard);
  
  void drawMessage(String txt, Color color);
  
  void drawMessage(String txt);
  
  /**
   * Make the user select a patch a given patch list
   * @param patches
   * @return the selected patch or null
   */
  Optional<RegularPatch> selectPatch(List<RegularPatch> patches);
  
  /**
   * Make the user select a choice among a list of choices
   * @param choices
   * @return the binded key, otherwise -1
   */
  int getPlayerChoice(Set<KeybindedChoice> choices);

  /**
   * Draw a dummy quilt with a given patch to 
   * show if the patch could fit in
   * @param quilt
   * @param patch
   */
  void drawDummyQuilt(QuiltBoard quilt, Patch patch);
  
  /**
   * 
   */
  void drawMessages();
  
  /**
   * 
   */
  void clearMessages();
  
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

}
