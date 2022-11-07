package model.gameboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import model.Patch;

public class PatchManager {

  private int neutralToken;
  private int nextPatchesRange;
  private final ArrayList<Patch> patches;
  private final ArrayList<Patch> availablePatches = new ArrayList<>();
  private Patch selected;

  public PatchManager(int nextPatchesRange, List<Patch> patches) {
    Objects.requireNonNull(patches, "The list of patch can't be null");
    if (nextPatchesRange < 1) {
      throw new IllegalArgumentException("The number of possible next patches available can't be less than 1");
    }
    this.nextPatchesRange = nextPatchesRange;
    this.patches = new ArrayList<>(patches);
    this.neutralToken = Patch.minPatch(this.patches);
    Collections.shuffle(this.patches);
    loadNextPatches();
  }

  /**
   * @return the selected patch, or null
   */
  public Patch selected() {
    return selected;
  }

  /**
   * Extract the previously selected patch
   * @return 
   */
  public Patch extractSelected() {
    var indexInAvailable = availablePatches.indexOf(selected());
    var extractedPatch = patches.remove(neutralToken + 1 + indexInAvailable);
    move(indexInAvailable + 1);
    unselect();
    return extractedPatch;
  }
  
  /**
   * 
   * @return
   */
  public List<Patch> availablePatches() {
    return List.copyOf(availablePatches);
  }
  
  /**
   * Move the token position by looping trough the patches, when arrives at the end.
   * Then load the new available patches.
   * @param n
   */
  public void move(int n) {
    neutralToken = (neutralToken + n) % patches.size();
    loadNextPatches();
  }

  /**
   * Select the given patch
   * must be in the available patches
   * the comparison is made with the address
   * @throws AssertionError - if the given patch does not exists in the available patches
   * @param patch
   */
  public void select(Patch patch) {
    Objects.requireNonNull(patch, "Can't select with null patch");
    for (var availablePatch : availablePatches) {
      // We want to test on addresses here
      if (patch.equals(availablePatch)) {
        selected = availablePatch;
        return;
      }
    }
    throw new AssertionError("This patch isn't present in the available ones.");
  }
  
  /**
   * Unselect the selected patch
   */
  public void unselect() {
    selected = null;
  }

  /**
   * Return the next patch after the token position by using modulo on the number
   * of patches (loop trough patches)
   * 
   * @param index
   * @return the patch or null
   */
  private Patch loopAndGetPatch(int index) {
    return patches.get(index % patches.size());
  }

  /**
   * Clear and fill the list of available patches based on the position of the
   * token
   * 
   * @return
   */
  private void loadNextPatches() {
    availablePatches.clear();
    // NEXTPATCHES *after* the token, so i = 1
    for (var i = 1; i <= nextPatchesRange; i++) {
      var patch = loopAndGetPatch(neutralToken + i);
      availablePatches.add(patch);
    }
  }
  
  public int numberOfPatches() {
    return patches.size();
  }

}
