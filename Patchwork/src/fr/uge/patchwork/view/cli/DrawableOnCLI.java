package fr.uge.patchwork.view.cli;

import fr.uge.patchwork.view.Drawable;

public interface DrawableOnCLI extends Drawable {
  void drawOnCLI(CommandLineInterface ui);
}
