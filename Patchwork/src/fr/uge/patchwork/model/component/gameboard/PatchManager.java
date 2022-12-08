package fr.uge.patchwork.model.component.gameboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;

public class PatchManager {

  private int neutralToken;
  private final ArrayList<RegularPatch> patches;

  public PatchManager(List<RegularPatch> patches) {
    Objects.requireNonNull(patches, "The list of patch can't be null");
    this.patches = new ArrayList<>(patches);
    Collections.shuffle(this.patches);
    this.neutralToken = minPatch(this.patches);
  }

  /**
   * Move the token by looping trough the patches, when arrives at the end.
   * @param n
   */
  public void moveNeutralToken(int moves) {
    neutralToken = (neutralToken + moves) % patches.size();
  }

  public void removeAtToken() {
    patches.remove((neutralToken + 1) % patches.size());
  }
  
  /**
   * Return a list of the availablePatches
   * @return
   */
  public List<RegularPatch> patches(int n) {
    var availablePatches = new ArrayList<RegularPatch>();
    // NEXTPATCHES *after* the token, so i = 1
    for (var i = 1; i <= n; i++) {
      var patch = loopAndGetPatch(neutralToken + i);
      availablePatches.add(patch);
    }
    return List.copyOf(availablePatches);
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
            Integer.compare(patches.get(i).form().countCoordinates(), 
                patches.get(j).form().countCoordinates()))
        .get();
  }

}
