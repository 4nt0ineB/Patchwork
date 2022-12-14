package fr.uge.patchwork.view.gui;

import static java.util.Comparator.comparing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import fr.uge.patchwork.controller.KeybindedChoice;
import fr.uge.patchwork.model.component.Player;
import fr.uge.patchwork.model.component.gameboard.PatchManager;
import fr.uge.patchwork.model.component.gameboard.TrackBoard;
import fr.uge.patchwork.model.component.patch.Patch;
import fr.uge.patchwork.model.component.patch.RegularPatch;
import fr.uge.patchwork.view.UserInterface;
import fr.umlv.zen5.ApplicationContext;
import fr.umlv.zen5.Event;
import fr.umlv.zen5.Event.Action;

public class GraphicalUserInterface implements UserInterface {

  private final ApplicationContext context;
  private final float width;
  private final float height;
  private final int fps = 20;
  private final Color backgroundColor = new Color(201, 153, 68);
  private BufferedImage img;
  
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

  public void addDrawingAction(Consumer<Graphics2D> action) {
    Objects.requireNonNull(action, "the drawing action can't be null");
    drawingActions.add(action);
  }
  
  @Override
  public void init() throws IOException {
    drawingActions.add(graphics -> {
      graphics.setColor(Color.ORANGE);
      graphics.fill(new  Rectangle2D.Float(0, 0, 500, 500));
    });
    var path = Path.of("resources/patchwork/img/img.jpg");
    try(var reader = Files.newInputStream(path)) {
      img = ImageIO.read(reader);
    }
    
  }
  
  @Override
  public void display() {
    var currentTime = Instant.now().toEpochMilli();
    // cap the refresh rate
    if(currentTime - time > 1000 / fps) {
      drawingActions.forEach(context::renderFrame);
      time = Instant.now().toEpochMilli();
    }
  }

  @Override
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
  
  private void drawPlayerInfo(Player player, int x, int y, int fontSize) {
    var buttonColor = new Color(47, 115, 138);
    addDrawingAction(g2 -> {
      g2.setFont(new Font("", Font.BOLD, fontSize));
      g2.drawString(player.name(), x,y );
      g2.setColor(buttonColor);
      g2.drawString(player.buttons() + " buttons", (int) x, (int) y + fontSize);
    });
  }
  
  private void drawPlayers(List<Player> players, int x, int y, int w, int h) {
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
  public void draw(TrackBoard trackBoard) {
    Objects.requireNonNull(trackBoard, "the track board can't be null");
    var sortedPlayers = trackBoard.players().stream().sorted(comparing(Player::name)).toList();
    drawPlayers(sortedPlayers, (int) (width - width / 3), 0, (int) width / 3, (int) height);
    new GraphicalTrackBoard((int) width / 4, (int) height / 4, 600, trackBoard).draw(this);
  }
  
  @Override
  public void draw(PatchManager manager) {
    Objects.requireNonNull(manager, "the patch manager can't be null");
    new GraphicalPatchManager(manager, 9, 0, (int) 20, (int) (width / 7), (int) (height - (height/10)*2) ).draw(this);
  }

  @Override
  public Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the list of choices can't be null");
    var choiceList = List.copyOf(choices);
    renderChoices(choiceList, 
        (int) (width * 0.02) , 
        height - (height * 0.07) - (choices.size() * 95) / 2, 
        400, 75, 20, 30);
    return menu(choiceList);
  }
  
  @Override
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
  public void drawDummyQuilt(Player player, Patch patch) {
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
    throw new AssertionError("todo");
    
  }

  @Override
  public Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices, "the choices can't be null");
    return Optional.empty();
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
