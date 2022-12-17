package fr.uge.patchwork.view.gui;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.model.component.player.HumanPlayer;
import fr.uge.patchwork.model.component.player.Player;
import fr.uge.patchwork.view.UserInterface;
import fr.umlv.zen5.ApplicationContext;
import fr.umlv.zen5.Event;
import fr.umlv.zen5.Event.Action;

/**
 * 
 * A graphical user interface for the patchwork game
 *
 */
public class GraphicalUserInterface implements UserInterface {

  private final ApplicationContext context;
  private final float width;
  private final float height;
  private final int fps = 20;
  private final Color backgroundColor = new Color(201, 153, 68);
  
  private final LinkedList<Consumer<Graphics2D>> drawingActions = new LinkedList<>();
  private KeybindedChoice choice;
  private RegularPatch selectedPatch;
  
  private long time;
  
  public GraphicalUserInterface(ApplicationContext context) {
    this.context = Objects.requireNonNull(context);
    var screenInfo = context.getScreenInfo();
    this.width = screenInfo.getWidth();
    this.height = screenInfo.getHeight();
    time = Instant.now().toEpochMilli();
  }
  
  /**
   * Add a drawing action that will be executed 
   * at the next displayed frame
   * @param action
   */
  public void addDrawingAction(Consumer<Graphics2D> action) {
    Objects.requireNonNull(action, "the drawing action can't be null");
    drawingActions.add(action);
  }
  
  @Override
  /**
   * Init the view
   */
  public void init() throws IOException {
    drawingActions.add(graphics -> {
      graphics.setColor(Color.ORANGE);
      graphics.fill(new  Rectangle2D.Float(0, 0, 500, 500));
    });
//    var path = Path.of("resources/patchwork/img/img.jpg");
//    try(var reader = Files.newInputStream(path)) {
//      img = ImageIO.read(reader);
//    }
  }
  
  @Override
  /**
   * Run all drawing actions.
   * with a defined refresh rate
   * 
   */
  public void display() {
    var currentTime = Instant.now().toEpochMilli();
    // cap the refresh rate
    if(currentTime - time > 1000 / fps) {
      drawingActions.forEach(context::renderFrame);
      time = Instant.now().toEpochMilli();
    }
  }

  @Override
  /**
   * clear the drawing actions queue
   * and clear the window with a background
   */
  public void clear() {
    drawingActions.clear();
    addDrawingAction(g2 -> {
      g2.setColor(backgroundColor);
      g2.fill(new Rectangle2D.Float(0, 0, width, height));
    });
  }

  @Override
  public void close() {
    context.exit(0);
  }
  
  @Override
  /**
   * Display a menu as the game mode selection menu.
   * (The game menu)
   * for given choices
   * @return an optional keybindedChoice
   */
  public Optional<KeybindedChoice> gameModeMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the choies can't be null");
    var choiceList = List.copyOf(choices);
    drawSplashScreen((int) width / 2 - 400, (int) (height / 2 - (choices.size() * 95) / 2) - 160, 80);
    renderChoices(choiceList, 
        width / 2 - 400, 
        height / 2 - (choices.size() * 95) / 2, 
        400, 75, 20, 35);
    return menu(choiceList);
  }
  
  @Override
  public Optional<KeybindedChoice> difficultyMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the choies can't be null");
    var choiceList = List.copyOf(choices);
    addDrawingAction(g2 -> {
      g2.setFont(new Font("", Font.BOLD, 80));
     g2.drawString("Difficulty", width / 2, height /2); 
    });
    renderChoices(choiceList, 
        width / 2 - 400, 
        height / 2 - (choices.size() * 95) / 2, 
        400, 75, 20, 35);
    return menu(choiceList);
  }
  
  /**
   * Return the next power of two greater or equal to n
   * @throws new IllegalArgumentException if n equal zero
   * @return
   */
  private double nextPower2(int n) {
    if(n == 0) {
      throw new IllegalArgumentException("n can't be equal to zero");
    }
    return 32 - Integer.numberOfLeadingZeros(n - 1);
  }
  
  /**
   * Draw the player info at given position
   * @param player
   * @param x
   * @param y
   * @param fontSize
   */
  private void drawPlayerInfo(HumanPlayer player, int x, int y, int fontSize) {
    var buttonColor = new Color(47, 115, 138);
    addDrawingAction(g2 -> {
      g2.setFont(new Font("", Font.BOLD, fontSize));
      g2.drawString(player.name(), x,y );
      g2.setColor(buttonColor);
      g2.drawString(player.buttons() + " buttons", (int) x, (int) y + fontSize);
    });
  }
  
  /**
   * Draw given players in a given area
   * @param players
   * @param x
   * @param y
   * @param w
   * @param h
   */
  private void drawPlayers(List<HumanPlayer> players, int x, int y, int w, int h) {
    var zones = new ArrayList<Rectangle2D.Double>();
    var firstZone = new Rectangle2D.Double(x, y, w, h);
    zones.add(firstZone);
    var numberOfDivision = nextPower2(players.size());
    for(var i = 1; i <= numberOfDivision; i++) {
      var start = zones.size();
      for(var j = 0; j < Math.pow(2, i - 1); j++) {
        var rect = zones.get(start - 1 - j);
        Rectangle2D.Double r1;
        Rectangle2D.Double r2;
        if(i % 2 == 0) {
          r1 = new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height / 2);
          r2 = new Rectangle2D.Double(rect.x, rect.y + rect.height / 2, rect.width, rect.height / 2);
        }else {
          r1 = new Rectangle2D.Double(rect.x, rect.y, rect.width / 2, rect.height);
          r2 = new Rectangle2D.Double(rect.x + rect.width / 2, rect.y, rect.width / 2, rect.height);
        }
        zones.add(r1);
        zones.add(r2);
      }
    }
    var numberOfZones = Math.pow(2, numberOfDivision);
    for(var i = 0; i < players.size(); i++) { // draw the quilt of each players in associated area
      var zone = zones.get((int) (zones.size() - numberOfZones + i));
      var side = (int) Math.min(zone.width, zone.height);
      var player = players.get(i);
      var zy = zone.y + side / 4;
      new GraphicalQuiltBoard(player.quilt(), 
          (int) zone.x - 20, 
          (int) zy, 
          side
          ).draw(this);
      var fontSize = (int) (side * 0.05);
      // draw the quilt
      drawPlayerInfo(player, (int) zone.x, (int) (zy + side + side * 0.1), fontSize);
    }
  }
  
  @Override
  /**
   * Draw the track board
   */
  public void draw(TrackBoard trackBoard) {
    Objects.requireNonNull(trackBoard, "the track board can't be null");
    var sortedPlayers = trackBoard.players().stream()
        .filter(HumanPlayer.class::isInstance)
        .map(HumanPlayer.class::cast)
        .sorted(comparing(Player::name))
        .toList();
    drawPlayers(sortedPlayers, (int) (width - width / 3), 0, (int) width / 3, (int) height);
    new GraphicalTrackBoard((int) width / 4, (int) height / 4, 600, trackBoard).draw(this);
  }
  
  @Override
  /**
   * Draw the patch manager
   */
  public void draw(PatchManager manager) {
    Objects.requireNonNull(manager, "the patch manager can't be null");
    new GraphicalPatchManager(manager, 9, 0, (int) 20, (int) (width / 7), (int) (height - (height/10)*2) ).draw(this);
  }

  @Override
  public Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the list of choices can't be null");
    var choiceList = List.copyOf(choices);
    var y = (int) (height - (height/10)*2) + 20;
    renderChoices(choiceList, 
        (int) (width * 0.02) ,
        (int) y, 
        0 ,
        0, 
        2, 
        30);
    return menu(choiceList);
  }
  
  
  
  
  @Override
  /**
   * Draw the menu of patch selection
   * with a given restricted list of patches used as available choices
   * and a patch manager
   */
  public Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager) {
    Objects.requireNonNull(patches, "the list of choices can't be null");
    Objects.requireNonNull(manager, "the list of choices can't be null");
    var i = 0;
    if(selectedPatch != null) {
      i = Math.max(0, patches.indexOf(selectedPatch));
    }
    var gmanager = new GraphicalPatchManager(manager, 9, 0, (int) 20, (int) (width / 7), (int) (height - (height/10)*2));
    gmanager.enhance(patches.get(i));
    gmanager.draw(this);
    Event event = context.pollOrWaitEvent(10);
    if(event != null) {
      Action action = event.getAction();
      if (action == Action.KEY_PRESSED) {
        var keyname = event.getKey().toString();
          switch(keyname) {
            case "UP" -> selectedPatch = patches.get(Integer.min(patches.size() - 1, i + 1));
            case "DOWN" -> selectedPatch = patches.get(Integer.max(i - 1, 0));
            case "SPACE" -> {
              choice = null;
              return Optional.of(patches.get(i));
            }
          }
      }
    }
    return Optional.empty();
  }
  
  /**
   * Graphically render choices as menu
   * @param choices
   * @param posx
   * @param posy
   * @param w
   * @param h
   * @param margin
   * @param fontsize
   */
  public void renderChoices(List<KeybindedChoice> choices, double posx, double posy, 
      double w, double h,  int margin, int fontsize) {
    Objects.requireNonNull(choices);
    Consumer<Graphics2D> runnable = graphics -> {
      graphics.setFont(new Font("Arial", Font.TRUETYPE_FONT, fontsize));
      var x = posx;
      var y = posy;
      Color color;
      // check index of previously selected choice
      int i = 0;
      if(choice != null) {
        i = choices.indexOf(choice);
      }
      for(var j = 0; j < choices.size(); j++) {
        color = Color.BLACK;
        if(choice != null && j == i) {
          // enhance choice
          color = new Color(193, 86, 78);
          graphics.setColor(color);
          graphics.fill(new Rectangle2D.Double(x - margin, y + 4, 8, fontsize));
        }
        graphics.setColor(color);
        // print description
        graphics.drawString(choices.get(j).description(), (int) x , (int) y + fontsize);
        y += 70 + margin;
      }
    };
    addDrawingAction(runnable);
  }
  
  /**
   * Process choices as a menu with UP, DOWN SPACE (validate)
   * keys
   * @param choices
   * @return
   */
  public Optional<KeybindedChoice> menu(List<KeybindedChoice> choices){
    Objects.requireNonNull(choices);
    int i = 0;
    if(choice != null) {
      i = choices.indexOf(choice);
    }
    Event event = context.pollOrWaitEvent(10);
    if(event != null) {
      Action action = event.getAction();
      if (action == Action.KEY_PRESSED) {
        var keyname = event.getKey().toString();
          switch(keyname) {
            case "UP" -> choice = choices.get(Integer.max(0, i - 1));
            case "DOWN" -> choice = choices.get(Integer.min(i + 1, choices.size() - 1));
            case "SPACE" -> {
              choice = null;
              return Optional.of(choices.get(i));
            }
          }
      }
    }
    return Optional.empty();
  }
  
  @Override
  public void drawDummyQuilt(HumanPlayer player, Patch patch) {
    var quiltSide = height / 2;
    var x = (int) ((width / 2) - quiltSide / 2);
    var y = (int) ((height / 2) - quiltSide / 2);
    var quilt = new GraphicalQuiltBoard(player.quilt()
        , x
        , y
        , (int) quiltSide);
    quilt.draw(this);
    quilt.drawWithPatchAsDummy(this, patch);
    var fontSize = 35;
    drawPlayerInfo(player, (int) x, (int) y - fontSize * 2, fontSize);
    drawOptions(this, quilt);

  }
  
  private void drawOption(GraphicalUserInterface ui, KeybindedChoice info, int x, int y) {
  	ui.addDrawingAction(g2 -> {
			var stringRect = g2.getFontMetrics().getStringBounds(info.toString(), g2);
	    g2.setColor(Color.BLACK);
	    g2.setFont(new Font("Arial", Font.BOLD, 30));
	    g2.drawString(info.toString(), x - (int) stringRect.getCenterX(), y + (int) stringRect.getCenterY());
  	}); 
  }
  
  private void drawOptions(GraphicalUserInterface ui, GraphicalQuiltBoard quilt) {
  	var optionY = quilt.coords().y();
   	var optionX = (quilt.coords().x()) / 2;
   	var offsetY = quilt.width() / (quilt.infos().size() + 1);
   	for (var info : quilt.infos()) {
   		optionY += offsetY;
   		drawOption(ui, info, (int)optionX, optionY);
   	}
  }
  
  public void drawSplashScreen(int x, int y, int fontsize) {
    addDrawingAction(g2 -> {
      g2.setColor(new Color(104, 107, 107));
      g2.setFont(new Font("Arial", Font.BOLD, fontsize));
      g2.drawString("Patchwork", x, y);
    });
  }

  @Override
  public void drawScoreBoard(TrackBoard trackBoard) {
    Objects.requireNonNull(trackBoard, "the track board can't be null");
    var sortedPlayers = trackBoard.players().stream().sorted(reverseOrder()).toList();
    addDrawingAction(g2 -> {
      var x = width / 2;
      var y = height / 6;
      var margin = 10;
      var fontSize =  (int) (height * 0.04);
      g2.setFont(new Font("", Font.CENTER_BASELINE, fontSize));
      var fontMetrics = g2.getFontMetrics();
      for(var player: sortedPlayers) {
        var txt = player.name() + " " + player.score();
        var txtWidth = fontMetrics.stringWidth(txt);
        g2.drawString(txt, x - txtWidth / 2, y);
        y += fontSize + margin;
      }
      g2.setColor(new Color(134, 123, 189));
      var txt = sortedPlayers.get(0).name() + " Wins !";
      g2.drawString(txt, x - (fontMetrics.stringWidth(txt) / 2), y + fontSize + margin);
    });
  }

  @Override
  public Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the choices can't be null");
    var choiceList = List.copyOf(choices);
    renderChoices(choiceList, 
        width / 2 - 400, 
        height - height / 4 - (choices.size() * (35 + 20 )) / 2, 
        0, 0, 20, 35);
    return menu(choiceList);
  }

  @Override
  public Optional<KeybindedChoice> manipulatePatch(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the choices can't be null");
    return getInput(choices);
  }

  @Override
  public Optional<KeybindedChoice> getInput(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices);
    Event event = context.pollOrWaitEvent(10);
    if(event != null) {
      Action action = event.getAction();
      if (action == Action.KEY_PRESSED) {
        var keyname = event.getKey().toString();
        if(keyname.length() == 1) {
          var key = keyname.toLowerCase(Locale.ROOT).charAt(0);
          return choices.stream().filter(c -> c.key() == key).findFirst();
        }
      }
    }
    return Optional.empty();
  }

}
