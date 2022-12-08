package fr.uge.patchwork.model.component.patch;

import java.util.Set;

import fr.uge.patchwork.view.cli.CommandLineInterface;
import fr.uge.patchwork.view.cli.DrawableOnCLI;

public final class LeatherPatch implements Patch, DrawableOnCLI {
  
  private final Patch2D patch;
  
  public LeatherPatch() {
    patch = new Patch2D(new Form(Set.of(new Coordinates(0,0))));
  }
  
  @Override
  public String toString() {
    return "LeatherPatch " + patch;
  }
  
  public Form form() {
    return patch.form();
  }
  

  @Override
  public void flip() {
    patch.flip(); 
  }

  @Override
  public void rotateLeft() {
    patch.rotateLeft();
  }

  @Override
  public void rotateRight() {
    patch.rotateRight();
  }

  @Override
  public void moveUp() {
    patch.moveUp();
  }

  @Override
  public void moveDown() {
    patch.moveDown();
  }

  @Override
  public void moveLeft() {
    patch.moveLeft();
  }

  @Override
  public void moveRight() {
    patch.moveRight();
  }

  @Override
  public boolean canMoveUp(int miny) {
    return patch.canMoveUp(miny);
  }

  @Override
  public boolean canMoveDown(int maxY) {
    return patch.canMoveDown(maxY);
  }

  @Override
  public boolean canMoveLeft(int minX) {
    return patch.canMoveLeft(minX);
  }

  @Override
  public boolean canMoveRight(int maxX) {
    return patch.canMoveRight(maxX);
  }

  @Override
  public Set<Coordinates> absoluteCoordinates() {
    return patch.absoluteCoordinates();
  }

  @Override
  public boolean overlap(Patch patch) {
    return this.patch.overlap(patch);
  }

  @Override
  public boolean fits(int width, int height) {
    return patch.fits(width, height);
  }

  @Override
  public boolean meets(Coordinates coordinates) {
    return patch.meets(coordinates);
  }

  @Override
  public void absoluteMoveTo(Coordinates coordinates) {
    patch.absoluteMoveTo(coordinates);
  }

  @Override
  public void drawOnCLI(CommandLineInterface ui) {
    patch.drawOnCLI(ui);
  }

  
}