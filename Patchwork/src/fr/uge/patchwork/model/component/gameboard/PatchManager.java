package fr.uge.patchwork.model.component.gameboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import fr.uge.patchwork.model.component.patch.RegularPatch;

class PatchManager {

  private int neutralToken;
  private int nextPatchesRange;
  private final ArrayList<RegularPatch> patches;
  private final ArrayList<RegularPatch> availablePatches = new ArrayList<>();
  private RegularPatch selected;

  public PatchManager(int nextPatchesRange, List<RegularPatch> patches) {
    Objects.requireNonNull(patches, "The list of patch can't be null");
    if (nextPatchesRange < 1) {
      throw new IllegalArgumentException("The number of possible next patches available can't be less than 1");
    }
    this.nextPatchesRange = nextPatchesRange;
    this.patches = new ArrayList<>(patches);
    Collections.shuffle(this.patches);
    this.neutralToken = minPatch(this.patches);
    loadNextPatches();
  }

  /**
   * Get the selected Patch
   * @return an optional of the selected patch, otherwise an empty Optional
   */
  public Optional<RegularPatch> selected() {
    return Optional.ofNullable(selected);
  }

  /**
   * Extract the previously selected patch
   * @throws AssertionError - if no patch is selected
   * @return the extracted Patch
   */
  public RegularPatch extractSelected() {
    var patch = selected();
    if(patch.isEmpty()) {
      throw new AssertionError("Can't extract a patch if not patch is selected");
    }
    var index = availablePatches.indexOf(patch.get());
    var extractedPatch = patches.remove((neutralToken + 1 + index) % patches.size());
    move(index + 1);
    unselect();
    return extractedPatch;
  }
  
  /**
   * Return a list of the availablePatches
   * @return
   */
  public List<RegularPatch> availablePatches() {
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
   * @throws AssertionError - iT the given patch does not exists in the available patches
   * @param patch
   */
  public void select(RegularPatch patch) {
    Objects.requireNonNull(patch, "Can't select with null patch");
    if(!availablePatches.contains(patch)) {
      throw new IllegalArgumentException("This patch is not present in those available");
    }
    selected = patch;
    
  }
  
  /**
   * Unselect the selected patch if exists
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
  private RegularPatch loopAndGetPatch(int index) {
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
  
  /**
   * Return index of the smallest patch in a list
   * @param patches
   * @return index
   */
  public static int minPatch(List<RegularPatch> patches) {
    Objects.requireNonNull(patches, "Can't find smallest in null obj");
    if(patches.isEmpty()) {
      throw new IllegalArgumentException("Empty list of patches");
    }
    
    return IntStream.range(0, patches.size())
        .boxed()
        .min(
            (i, j) -> 
            Integer.compare(patches.get(i).countCells(), 
                patches.get(j).countCells()))
        .get();
  }

}
