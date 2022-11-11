package util.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;
import java.util.function.IntPredicate;

public class JsonParser {
  
  private final Stack<ParsingMode> modeStack = new Stack<>();
  int i = 0;
  private final char[] buffer = new char[512];
  
  private boolean isPreviousMode(ParsingMode mode) {
    if(modeStack.size() < 2) {
      return false;
    }
    return modeStack.get(modeStack.size() - 2).equals(mode);
  }
  
  private boolean isMode(ParsingMode mode) {
    if(modeStack.isEmpty()) {
      return false;
    }
    return modeStack.peek().equals(mode);
  }
  
  private void eatUntil(BufferedReader  reader, IntPredicate predicate) throws IOException {
    int c = reader.read();
    if(c == -1) {
      return;
    }
    if(predicate.test(c)) {
      buffer[i] = (char) c;
      i++;
    }
    eatUntil(reader, predicate);
  }
  
  private void readUntil(BufferedReader  reader, IntPredicate predicate) throws IOException {
    int c = reader.read();
    if(c == -1) {
      return;
    }
    if(predicate.test(c)) {
      return;
    }
    buffer[i] = (char) c;
    i++;
    readUntil(reader, predicate);
  }
  
  private JsonAtomic readValue(BufferedReader reader) throws IOException {
    eatUntil(reader, (i) -> {
      return !Character.isWhitespace(0);
    });
    
  }
  
  private JsonObject readObject(BufferedReader reader) {
  
    reader.
  }
  
  
  
  private void fromStream(BufferedReader reader, JsonObject object) throws IOException {
    int c = reader.read();
    if(c == -1) {
      return;
    }
    
    buffer[i] = (char) c;
    i++;
    var id = ".";
    
    
    
    if(isMode(ParsingMode.IN_OBJECT)) {
      fromStream(reader, object);
      return;
    }
    if(isMode(ParsingMode.IN_STRING)) {
      if(isPreviousMode(ParsingMode.IN_OBJECT)) {
        
      }
    }
   
    if(isMode(ParsingMode.IN_STRING)) {
      
    } else if(isMode(ParsingMode.IN_VALUE)) {
      if(Character.compare(character, ',') == 0){
        modeStack.pop(); // exit value
      }
      if(!Character.isWhitespace(character)) {
        buffer[i] = character;
        i++;
      }
    }
    
    switch((char) c) {
    case '{' -> modeStack.add(ParsingMode.IN_OBJECT);
    case '[' -> modeStack.add(ParsingMode.IN_LIST);
    case '"' -> {
      if(isMode(ParsingMode.IN_STRING)) {
        // if current quote was not previously unspecialized
        if(buffer[i - 1] != '\\') {
          modeStack.pop(); // exit string mode
        }else {
          buffer[i] = (char) c;
          i++;
        }
      }else {
        modeStack.add(ParsingMode.IN_STRING);
      }
    }
    case ':' -> modeStack.add(ParsingMode.IN_VALUE);
    case '}',']' -> modeStack.pop();
    default -> { 
        if(!Character.isWhitespace((char) c)){
          buffer[i] = (char) c;
          i++; 
        }
      }
    }
    
    
  
  }
  
  public JsonObject fromFile(Path path) throws IOException {
    var root = new JsonObject();
    try(var reader = Files.newBufferedReader(path)){
      modeStack.clear();
      modeStack.add(ParsingMode.IN_OBJECT);
      fromStream(reader, root);
    }
    return o;
  }
  
}


//else if(isMode(ParsingMode.IN_LIST)) {
//  if(Character.compare(character, ',') == 0){
//    modeStack.pop(); // exit value
//  }
//  
//  readToken(reader, (x) -> { return Character.isWhitespace((char) x); });
//  



//
//"patches": [
//            {   
//              "price":2,
//              "moves":2,
//              "buttons":0,
//              "coordinates": [
//                {"y": 0, "x": -1}, 
//                {"y": 0, "x": 0}, 
//                {"y": 0, "x": 1}
//                ]
//            }
//        ]