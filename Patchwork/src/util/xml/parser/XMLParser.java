package util.xml.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

public class XMLParser {
  
  int i = 0;
  private final char[] buffer = new char[512];
  private String text = "";
  
 
  public XMLElement parse(Path path) throws IOException {
    clear();
    try(var reader = Files.newBufferedReader(path)){
      eatBufferUntil(reader, (i) -> (char) i == '>');
      var root = new XMLElement("root");
      fromBufferedReader(reader, root);
      return root;
    }
  }
  
  public XMLElement parse(String text) {
    clear();
    this.text = text;
    return fromString(null);
  }
  
  /**
   * Recursive parsing shared text field
   * @param element
   * @return
   */
  private XMLElement fromString(XMLElement element) {
    var read = readFromString("\\s*<\\w+>");
    // new record
    if(read != null) {
      var xmlElement = new XMLElement(read.strip().replaceAll("[<>]", ""));
      fromString(xmlElement);
      read = readFromString("</\\w+>");
      if(read == null) {
        throw new AssertionError("XML format error. No end-tag found");
      }
      if(element == null) {
        return xmlElement;
      }
      element.add(xmlElement);
      fromString(element);
      return null;
    }
    // Plain text inside record
    read = readFromString("^[^<]+");
    if(read == null) {
      read = "";
    }
    if(element == null) {
      throw new AssertionError("XML format error missing previous tag before" + read.strip());
    }
    element.setContent(read);
    return null;
  }
  
  private String readFromString(String pattern) {
    var matcher = Pattern.compile(pattern).matcher(text);
    if(matcher.lookingAt()) {
      text = text.substring(matcher.group().length(), text.length());
      return matcher.group();
    }
    return null;
  }
 
  
  private void eatBufferUntil(BufferedReader reader, IntPredicate predicate) throws IOException {
    int c = reader.read();
    if(c == -1) {
      return;
    }
    if(predicate.test(c)) {
      buffer[i] = (char) c;
      i++;
      return;
    }
    eatBufferUntil(reader, predicate);
  }
  
  private void readBufferUntil(BufferedReader  reader, IntPredicate predicate) throws IOException {
    int c = reader.read();
    if(c == -1) {
      return;
    }
    if(predicate.test(c)) {
      return;
    }
    buffer[i] = (char) c;
    i++;
    readBufferUntil(reader, predicate);
  }
  
  private void fromBufferedReader(BufferedReader reader, XMLElement element) throws IOException {
    readBufferUntil(reader, (i) -> (char) i == '<');
    var content = new String(Arrays.copyOfRange(buffer, 1, i));
    i = 0;
    readBufferUntil(reader, (i) -> (char) i == '>');
    var tag = new String(Arrays.copyOfRange(buffer, 0, i));
    i = 1;
    if(tag.startsWith("/")) { // closing tag
      element.setContent(content);
    }else { 
      // not a closing tag so its a new tag
      // child of the given element
      var xmlElement = new XMLElement(tag);
      fromBufferedReader(reader, xmlElement);
      element.add(xmlElement);
      fromBufferedReader(reader, element);
    }
  }
  
  private void clear() {
    i = 0;
    text = "";
  }
  
}