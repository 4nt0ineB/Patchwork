package model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import view.cli.CommandLineInterface;
import view.cli.DrawableOnCLI;

public class Menu implements DrawableOnCLI {

	private final CommandLineInterface cli;
	private Set<MenuOption> menuOptions = new HashSet<MenuOption>();
	
	public Menu(CommandLineInterface cli) {
    // var actions = List.of(new Action(""));
		Objects.requireNonNull(cli, "can't create Menu with null ui");
		this.cli = cli;
		this.menuOptions = addAllMenuOptions();
  }
	
	private Set<MenuOption> addAllMenuOptions(){
		var setMenuOption = new HashSet<MenuOption>();
		setMenuOption.add(MenuOption.BASIC);
		setMenuOption.add(MenuOption.COMPLETE);
		return setMenuOption;
	}
	
	@Override
	public void drawOnCLI(CommandLineInterface cli) {
		cli.builder().append("Game Mode Available :\n");
		this.menuOptions.forEach(option -> cli.builder().append("\n").append(option.toString()).append("\n"));
		cli.builder().append("\n").append("Choose your Game Mode : ");
	}
	
	public Set<MenuOption> getMenuOptions(){
		return this.menuOptions;
	}

}
