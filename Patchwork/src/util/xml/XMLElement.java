package util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
  
  /**
   * Get the content of the record
   * @return
   */
  public String content() {
    if(!isEmpty()) {
      return "";
    }
    return content;
  }
  
  /**
   * Get the tag of the record
   * @return
   */
  public String tag() {
    return tag;
  }
  
  /**
   * Return the first record of the XMLElement having the given tag
   * @param tag
   * @exception NoSuchElementException If a no record with the given tag could be found
   * @return 
   */
  public XMLElement getByTagName(String tag){
    var element = this.stream().filter(e -> e.tag.equals(tag)).findFirst();
    if(element.isEmpty()) {
      throw new NoSuchElementException("No tagged record \""+ tag +"\" found");
    }
    return element.get();
  }
  
  /**
   * Return all records of the XMLElement having the given tag
   * @param tag
   * @return
   */
  public List<XMLElement> getAllByTagName(String tag){
    return this.stream().filter(e -> e.tag().equals(tag)).toList();
  }
  
  public void setContent(Object content) {
    Objects.requireNonNull(content);
    this.content = content.toString();
  }
  
  /**
   * Build a nested string representation of the XMLElement recursively in a {#link StringBuilder}
   * @param builder
   * @param depth
   * @return
   */
  private String toStringRecursive(StringBuilder builder, int depth) {
    for(var i = 0; i < depth; i++) {
      builder.append("  ");
    }
    builder.append("<")
    .append(tag)
    .append(">");
    if(!isEmpty()) {
      builder.append("\n");
      this.forEach(element -> {
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
  
  /**
   * Require the XMLElement to be non null with {@link Objects#requireNonNull(Object)
   * and to contain nested elements or a not empty string for {@link XMLElement#content}
   * 
   * @param element
   * @param msg
   * @exception IllegalStateException If element is empty, with the given message.
   * @return
   */
  public static XMLElement requireNotEmpty(XMLElement element, String msg) {
    Objects.requireNonNull(element, "xml can't be null");
    if(element.isEmpty() && element.content.isEmpty()) {
      throw new IllegalStateException(msg);
    }
    return element;
  }
  
  /**
   * @see XMLElement#requireNotEmpty(XMLElement, String)
   */
  public static XMLElement requireNotEmpty(XMLElement element) {
    return requireNotEmpty(element, "The xml element is empty");
  }
  
}
