package fr.uge.patchwork.view.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

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
  private final int fps = 60;
  private final Color backgroundColor = new Color(237, 220, 174);
  private final ArrayList<Consumer<Graphics2D>> drawingActions = new ArrayList<>();
  
  private long time;
  
  public GraphicalUserInterface(ApplicationContext context) {
    this.context = Objects.requireNonNull(context);
    var screenInfo = context.getScreenInfo();
    this.width = screenInfo.getWidth();
    this.height = screenInfo.getHeight();
    time = Instant.now().toEpochMilli();
  }

  @Override
  public void init() {
    drawingActions.add(graphics -> {
      graphics.setColor(Color.ORANGE);
      graphics.fill(new  Rectangle2D.Float(0, 0, 500, 500));
    });
  }
  
  @Override
  public void display() {
    var currentTime = Instant.now().toEpochMilli();
    // cap the refresh rate
    if(currentTime - time <= 1000 / fps) { 
      drawingActions.forEach(context::renderFrame);
      time = currentTime;
      drawingActions.clear();
    }
  }

  @Override
  public void clear() {
    drawingActions.add(graphics -> {
      graphics.setColor(backgroundColor);
      graphics.fill(new Rectangle2D.Float(0, 0, width, height));
    });
    drawSplashScreen();
  }

  @Override
  public void close() {
    context.exit(0);
  }
  
  @Override
  public Optional<RegularPatch> selectPatch(List<RegularPatch> patches, PatchManager manager) {
    // TODO Auto-generated method stub
    return Optional.empty();
  }
  
  @Override
  public Optional<KeybindedChoice> gameModeMenu(Set<KeybindedChoice> choices) {
    Objects.requireNonNull(choices);
    var mousePosition = MouseInfo.getPointerInfo().getLocation();
    Event event = context.pollOrWaitEvent(10);
    if(event != null) {
      Action action = event.getAction();
      if (action == Action.KEY_PRESSED) {
        String keyname = event.getKey().toString();
          char key = keyname.toLowerCase(Locale.ROOT).charAt(0);
          return choices.stream().filter(c -> c.key() == key).findFirst();
      }
      if(action == Action.POINTER_MOVE) {
        System.out.println(event.getLocation());
      }
    }
    var rect = new Rectangle2D.Double(width / 2, height / 2, 200, 200);
    System.out.println(rect);
    if(rect.contains(mousePosition)) {
      System.out.println("hey");
    }
    context.renderFrame(graphics -> {
      Color fontColor;
      graphics.setFont(new Font("Arial", Font.ROMAN_BASELINE, 35));
      var x = 400;
      var y = 400;
      graphics.setColor(new Color(148, 148, 148));
      graphics.fill(rect);
//      for(var choice: choices) {
//        fontColor = new Color(148, 148, 148);
//       
//        graphics.setColor(fontColor);
//        graphics.drawString(choice.toString(), x, y);
//        y += 35;
//      }
      
    });
    return Optional.empty();    
  }

  @Override
  public Optional<KeybindedChoice> turnMenu(Set<KeybindedChoice> choices) {
    context.renderFrame(graphics -> {
      graphics.setColor(new Color(148, 148, 148));
      graphics.setFont(new Font("Arial", Font.ROMAN_BASELINE, 35));
      graphics.drawString("q to quit", 400, 400);
    });
    Event event = context.pollOrWaitEvent(10);
    if(event != null) {
      Action action = event.getAction();
      System.out.println(action);
      if (action == Action.KEY_PRESSED && event.getKey().toString().equals("Q")) {
        return Optional.of(new KeybindedChoice('q', ""));
      }
      System.out.println(event.getKey());
    }
    return Optional.empty();
  }

  @Override
  public void drawDummyQuilt(Player player, Patch patch) {
    // TODO Auto-generated method stub
    
  }
  
 
  
  public void drawSplashScreen() {
    drawingActions.add(graphics -> {
      graphics.setColor(new Color(148, 148, 148));
      graphics.setFont(new Font("Arial", Font.ROMAN_BASELINE, 35));
      graphics.drawString("Patchwork", 70, 70);
    });
  }

  @Override
  public void draw(TrackBoard trackboard) {
    // TODO Auto-generated method stub
    
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
