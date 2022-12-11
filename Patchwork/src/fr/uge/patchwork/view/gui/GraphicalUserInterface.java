package fr.uge.patchwork.view.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
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
  private final int fps = 25;
  private final Color backgroundColor = new Color(201, 153, 68);
  private BufferedImage img;
  
  private final LinkedList<Consumer<Graphics2D>> drawingActions = new LinkedList<>();
  private KeybindedChoice choice;
  
  private long time;
  
  public GraphicalUserInterface(ApplicationContext context) {
    this.context = Objects.requireNonNull(context);
    var screenInfo = context.getScreenInfo();
    this.width = screenInfo.getWidth();
    this.height = screenInfo.getHeight();
    time = Instant.now().toEpochMilli();
  }

  public void addDrawingAction(Consumer<Graphics2D> action) {
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
    var choiceList = List.copyOf(choices);
    drawSplashScreen((int) width / 2 - 400, (int) (height / 2 - (choices.size() * 95) / 2) - 160, 80);
    renderChoices(choiceList, 
        width / 2 - 400, 
        height / 2 - (choices.size() * 95) / 2, 
        400, 75, 20, 35);
    return menu(choiceList);
  }
  
  @Override
  public void draw(TrackBoard trackBoard) {
     new GraphicalTrackBoard(200, 200, 800, trackBoard).draw(this);
  }

  @Override
  public Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices) {
    var choiceList = List.copyOf(choices);
    renderChoices(choiceList, 
        width / 5 - 400, 
        height - (height / 6) - (choices.size() * 95) / 2, 
        400, 75, 20, 35);
    return menu(choiceList);
  }
  
  @Override
  public Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager) {
    // TODO Auto-generated method stub
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
      int i = 0;
      Color color;
      // check index of previously selected choice
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
      if(action == Action.POINTER_MOVE) {
        System.out.println(event.getLocation());
      }
    }
    // add mouse click capability
    
    return Optional.empty();
  }
  
  @Override
  public void drawDummyQuilt(Player player, Patch patch) {
    // TODO Auto-generated method stub
    
  }
  
  public void drawSplashScreen(int x, int y, int fontsize) {
    addDrawingAction(g2 -> {
      g2.setColor(new Color(104, 107, 107));
      g2.setFont(new Font("Arial", Font.BOLD, fontsize));
      g2.drawString("Patchwork", x, y);
    });
  }


  @Override
  public void drawScoreBoard(TrackBoard trackboard) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Optional<KeybindedChoice> endGameMenu(Set<KeybindedChoice> choices) {
    // TODO Auto-generated method stub
    return Optional.empty();
  }

  @Override
  public Optional<KeybindedChoice> manipulatePatch(Set<KeybindedChoice> choices) {
    // TODO Auto-generated method stub
    return Optional.empty();
  }

  

}
