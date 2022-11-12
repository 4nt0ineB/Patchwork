package util.xml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XMLElement extends ArrayList<XMLElement> {
  private static final long serialVersionUID = 9004215052565055553L;
  private final String tag;
  private String content = "";
  
  /**
   * Constructor of XMLElement
   * @param tag the tag the the record
   * @param content The text content fetched from {@link Object#toString}methods of the given Object
   * The text content is hidden while the record contains other record
   * 
   */
  public XMLElement(String tag, Object content) {
    if(!tag.matches("\\w+")) {
      throw new IllegalArgumentException("The given tag does not respect the pattern [a-zA-Z_0-9]+ : " + tag);
    }
    this.tag = Objects.requireNonNull(tag, "The tag can't be null");
    this.content = Objects.requireNonNull(content, "The content can't be null").toString();
  }
  
  /**
   * Constructor of XMLElement with content defaulted to empty string
   * @param tag
   */
  public XMLElement(String tag) {
    this(tag, "");
  }
  
  public String content() {
    if(!isEmpty()) {
      return "";
    }
    return content;
  }
  
  public String tag() {
    return tag;
  }
  
  public XMLElement getElementByTagName(String tag){
    return this.stream().filter(e -> e.tag.equals(tag)).findFirst().get();
  }
  
  public List<XMLElement> getAllElementsByTagName(String tag){
    return this.stream().filter(e -> e.tag().equals(tag)).toList();
  }
  
  public void setContent(Object content) {
    Objects.requireNonNull(content);
    this.content = content.toString();
  }
  
  private String toStringRecursive(StringBuilder builder, int depth) {
    for(var i = 0; i < depth; i++) {
      builder.append("  ");
    }
    builder.append("<")
    .append(tag)
    .append(">");
    if(!isEmpty()) {
      builder.append("\n");
      this.stream().forEach(element -> {
        builder.append(element.toStringRecursive(new StringBuilder(), depth + 1));
      });
      for(var i = 0; i < depth; i++) {
        builder.append("  ");
      }
    }else {
      builder.append(content);
    }
    builder.append("</")
    .append(tag)
    .append(">\n");
    return builder.toString();
  }
  
  @Override
  public String toString() {
    return toStringRecursive(new StringBuilder(), 0);
  }
}
