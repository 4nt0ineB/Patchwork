package fr.uge.patchwork.model.component.patch;

import java.util.Set;

import fr.uge.patchwork.model.component.Coordinates;

public sealed interface Patch permits Patch2D, RegularPatch, LeatherPatch {
  
  Form form();

  /**
   * 90° left rotation
   */
  public void rotateLeft();
  /**
   * 90° right rotation
   */
  public void rotateRight();
  /**
   * Decrement by one the absolute coordinates along y axis
   */
  public void moveUp();
  
  /**
   * Increment by one the absolute coordinates along y axis
   */
  public void moveDown();
  
  /**
   * Decrement by one the absolute coordinates along x axis
   */
  public void moveLeft();
  
  /**
   * Increment by one the absolute coordinates along x axis
   */
  public void moveRight();
  
  /**
   * Says if it's possible for the patch to move up
   * @return
   */
  public boolean canMoveUp(int miny);
  
  /**
   * Says if it's possible for the patch to move down
   * @return
   */
  public boolean canMoveDown(int maxY);
  /**
   * Says if it's possible for the patch to move left
   * @return
   */
  public boolean canMoveLeft(int minX);
  
  /**
   * Says if it's possible for the patch to move right
   * @return
   */
  public boolean canMoveRight(int maxX);
  
  
  /**
   * Return a set of the absolute positions of the patch cells
   * @return
   */
  public Set<Coordinates> absoluteCoordinates();
  
  /**
   * Return true if patch overlap an other patch
   * @param patch
   * @return true if overlap, else false 
   */
  public boolean overlap(RegularPatch patch);
  
  /**
   * check if the absolute coordinates of the 
   * patch fits in a defined rectangle<br>
   * 
   * [0;width[<br>
   * [0;height[
   * 
   * @param width
   * @param height
   * @return false if its not fitting, else true
   */
  public boolean fits(int width, int height);
  
  /**
   * Check if any coordinates in a list
   * match a coordinates of the patch
   * @param coordinates
   * @return
   */
  public boolean meets(Coordinates coordinates);
   
  /**
   * Move the absolute origin of the patch to the given coordinates
   * @param coordinates
   */
  public void absoluteMoveTo(Coordinates coordinates);
}
