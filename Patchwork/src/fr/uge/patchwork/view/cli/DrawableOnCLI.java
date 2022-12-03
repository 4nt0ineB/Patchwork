package fr.uge.patchwork.view.cli;

import fr.uge.patchwork.view.Drawable;

/**
 * 
 * Provides API for drawables that can be drawn on a {@link CommandLineInterface}
 *
 */
public interface DrawableOnCLI extends Drawable {
  void drawOnCLI(CommandLineInterface ui);
}
