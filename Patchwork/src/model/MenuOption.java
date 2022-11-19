package model;

import java.util.Objects;

public enum MenuOption {
	BASIC(1, "Basic Game Mode"),
	COMPLETE(2, "Complete Game Mode");
	
	private final int bind;
	private final String description;
	
	private MenuOption(int bind, String description) {
		Objects.requireNonNull(bind, "No null bind accepted to create an MenuOption");
		Objects.requireNonNull(description, "No null description accepted to create an MenuOption");
		this.bind = bind;
		this.description = description;
	}
	
	public String bind() {
    return "" + bind;
  }
	
	public int getBind() {
		return bind;
	}
  
  public String description() {
    return description;
  }
  
  @Override
  public String toString() {
    return "[" + bind + "] " + description ;
  }

}
