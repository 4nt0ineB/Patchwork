package fr.uge.patchwork.view;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;

public interface UserInterface {
  
  void init() throws IOException;
  void draw(TrackBoard trackboard);
  void drawScoreBoard(TrackBoard trackboard);
  /**
   * Draw a dummy quilt with a given patch to 
   * show if the patch could fit in
   * @param quilt
   * @param patch
   */
  void drawDummyQuilt(Player player, Patch patch);
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
  Optional<KeybindedChoice> gameModeMenu(Set<KeybindedChoice> choices);
  Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices);
  Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices);
  Optional<KeybindedChoice> manipulatePatch(Set<KeybindedChoice> choices);
  //Optional<KeybindedChoice> getInput(Set<KeybindedChoice> choices);
  /**
   * Make the user select a patch a given patch list
   * @param patches
   * @return the selected patch or null
   */
  Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager);
}
