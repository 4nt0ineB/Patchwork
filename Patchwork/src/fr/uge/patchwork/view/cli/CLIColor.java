package fr.uge.patchwork.view.cli;

import java.util.Objects;

import fr.uge.patchwork.view.Color;

/**
 * Provides ANSI colors
 */
public enum CLIColor {
  ANSI_ITALIC("\u001B[3m"), 
  ANSI_BOLD("\u001B[1m"), 
  ANSI_RESET("\u001B[0m"),
  ANSI_RESET2("\033[39m\\033[49m"),
  ANSI_GREY("\033[38;5;59m"),
  ANSI_BLACK("\u001B[30m"), 
  ANSI_RED("\u001B[31m"), 
  ANSI_GREEN("\u001B[32m"), 
  ANSI_YELLOW("\u001B[33m"),
  ANSI_BLUE("\u001B[34m"), 
  ANSI_PURPLE("\u001B[35m"), 
  ANSI_CYAN("\u001B[36m"), 
  ANSI_WHITE("\u001B[37m"),
  ANSI_ORANGE("\033[38;5;202m"), 
  ANSI_BBLUE("\033[38;5;33m"), 
  ANSI_BLACK_BACKGROUND("\u001B[40m"),
  ANSI_RED_BACKGROUND("\u001B[41m"), 
  ANSI_GREEN_BACKGROUND("\u001B[42m"), 
  ANSI_YELLOW_BACKGROUND("\u001B[43m"),
  ANSI_BLUE_BACKGROUND("\u001B[44m"), 
  ANSI_PURPLE_BACKGROUND("\u001B[45m"), 
  ANSI_CYAN_BACKGROUND("\u001B[46m"),
  ANSI_WHITE_BACKGROUND("\u001B[47m");
  
  private final String str;
  
  CLIColor(String str) {
      this.str = str;
  }
  
  @Override
  public String toString() {
      return str;
  }
  
  /**
   * Make a 8-bit color ANSI escape sequence for 
   * given rgb values
   * @param r red
   * @param g green 
   * @param b blue
   * @return an ansi escape squence
   */
  public static String rgb(int r, int g, int b) {
    return "\033[38;2;"+ (r & 0xFF) +";"+ (g & 0xFF) +";"+ (b & 0xFF) +"m";
  }
  
  public static String fromColor(Color color) {
    Objects.requireNonNull(color);
    return rgb(color.r(),color.g(), color.b());
  }
  
}