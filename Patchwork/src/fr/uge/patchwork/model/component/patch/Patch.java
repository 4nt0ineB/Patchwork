package fr.uge.patchwork.model.component.patch;

import java.util.Set;

public sealed interface Patch permits Patch2D, RegularPatch, LeatherPatch {
  
  Form form();

  void flip();
  
  /**
   * 90° left rotation
   */
  void rotateLeft();
  
  /**
   * 90° right rotation
   */
  void rotateRight();
  
  /**
   * Decrement by one the absolute coordinates along y axis
   */
  void moveUp();
  
  /**
   * Increment by one the absolute coordinates along y axis
   */
  void moveDown();
  
  /**
   * Decrement by one the absolute coordinates along x axis
   */
  void moveLeft();
  
  /**
   * Increment by one the absolute coordinates along x axis
   */
  void moveRight();
  
  /**
   * Says if it's possible for the patch to move up
   * @return
   */
  boolean canMoveUp(int miny);
  
  /**
   * Says if it's possible for the patch to move down
   * @return
   */
  boolean canMoveDown(int maxY);
  
  /**
   * Says if it's possible for the patch to move left
   * @return
   */
  boolean canMoveLeft(int minX);
  
  /**
   * Says if it's possible for the patch to move right
   * @return
   */
  boolean canMoveRight(int maxX);
  
  
  /**
   * Return a set of the absolute positions of the patch cells
   * @return
   */
  Set<Coordinates> absoluteCoordinates();
  
  /**
   * Return true if patch overlap an other patch
   * @param patch
   * @return true if overlap, else false 
   */
  boolean overlap(Patch patch);
  
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
  boolean fits(int width, int height);
  
  /**
   * Check if any coordinates in a list
   * match a coordinates of the patch
   * @param coordinates
   * @return
   */
  boolean meets(Coordinates coordinates);
   
  /**
   * Move the absolute origin of the patch to the given coordinates
   * @param coordinates
   */
  void absoluteMoveTo(Coordinates coordinates);

}
