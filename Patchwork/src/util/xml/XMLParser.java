package util.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;

/**
 * Provides a XML parser tool to build an XML manipulable DOM
 *
 */
public class XMLParser {
  
  private int i = 0;
  private final char[] buffer = new char[512];
  private String text = "";
  
  /**
   * Parse a XML formated file to produce a nested XMLElement
   * @param path
   * @return
   * @throws IOException If an I/O error occurs
   */
  public XMLElement parse(Path path) throws IOException {
    clear();
    try(var reader = Files.newBufferedReader(path)){
      eatBufferUntil(reader, (i) -> (char) i == '>');
      var root = new XMLElement("root");
      fromBufferedReader(reader, root);
      return root;
    }
  }
  
  /**
   * Parse a XML formated string to produce a nested XMLElement
   * @param text
   * @return
   * @throws ParseException 
   */
  public XMLElement parse(String text) throws ParseException {
    clear();
    this.text = text;
    XMLElement xmlElement;
    try {
      xmlElement = fromString(null);
    }catch(IllegalArgumentException e) {
      System.err.println(e);
      throw new ParseException("XML format error. Expected tag not found at", 
          text.length() - this.text.length());
    }
    
    return xmlElement;
  }
  
  /**
   * Recursive parsing with shared {@link XMLParser#text} string
   * @param element
   * @throws IllegalArgumentException a tag could'nt be found where it should be
   * (XML not properly formated)
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
        throw new IllegalArgumentException("XML format error. No end-tag found");
      }
      if(element == null) {
        return xmlElement;
      }
      element.add(xmlElement);
      fromString(element);
      return null;
    }
    // Plain text inside record
    read = Objects.requireNonNullElse(readFromString("^[^<]+"), "");
    if(element == null) {
      throw new IllegalArgumentException("XML format error. Missing previous tag before" + read.strip());
    }
    element.setContent(read);
    return null;
  }
  
  /**
   * Extract from {@link XMLParser#text} a given pattern.
   * If pattern matches with {@link Matcher#lookingAt} the {@link XMLParser#text} is replaced
   * with the one where the pattern have been extracted
   * @param pattern 
   * @return the extracted part of {@link XMLParser#text} that matches the pattern
   */
  private String readFromString(String pattern) {
    var matcher = Pattern.compile(pattern).matcher(text);
    if(matcher.lookingAt()) {
      text = text.substring(matcher.group().length(), text.length());
      return matcher.group();
    }
    return null;
  }
 
  /**
   * Read and don't put any characters read in the {@link XMLParser#buffer} until the predicate is true.
   * The character read when the predicate is triggered is added to the buffer.
   * @param reader
   * @param predicate
   * @throws IOException If an I/O error occurs
   */
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
  
  /**
   * Add to the {@link XMLParser#buffer}, read characters until the given predicate is true. 
   * The character read when the predicate is triggered is not added to the buffer.
   * @param reader
   * @param predicate
   * @throws IOException If an I/O error occurs
   */
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
  
  /**
   * Recursive parsing from a {@link BufferedReader}.
   * @param reader
   * @param element The root of the xml formated stream of char must be given.
   * @throws IOException If an I/O error occurs
   */
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
  
  /**
   * Clear the buffers and temporary variables used 
   * by the parsing
   */
  private void clear() {
    i = 0;
    text = "";
  }
  
}