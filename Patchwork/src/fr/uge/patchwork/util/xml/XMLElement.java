package fr.uge.patchwork.util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class XMLElement {
  private final ArrayList<XMLElement> elements = new ArrayList<>();
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
    if(!elements.isEmpty()) {
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
   * @exception NoSuchElementException If no record with the given tag could be found
   * @return 
   */
  public XMLElement getByTagName(String tag){
    var element = elements.stream().filter(e -> e.tag.equals(tag)).findFirst();
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
    return elements.stream().filter(e -> e.tag().equals(tag)).toList();
  }
  
  public void setContent(Object content) {
    Objects.requireNonNull(content);
    this.content = content.toString();
  }
  
  /**
   * Build a nested string representation of the XMLElement, 
   * recursively, in a {#link StringBuilder}
   * @param builder
   * @param depth
   * @return
   */
  private String toStringImpl(StringBuilder builder, int depth) {
    for(var i = 0; i < depth; i++) {
      builder.append("  ");
    }
    builder.append("<")
    .append(tag)
    .append(">");
    if(!elements.isEmpty()) {
      builder.append("\n");
      elements.forEach(element -> {
        builder.append(element.toStringImpl(new StringBuilder(), depth + 1));
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
    return toStringImpl(new StringBuilder(), 0);
  }
  
  public boolean isEmpty() {
    return elements.isEmpty() && content.isEmpty();
  }
  
  /**
   * Require the XMLElement to be non null with {@link Objects#requireNonNull(Object)}
   * and to contain nested elements,
   * or a not empty string for {@link XMLElement#content}
   * 
   * @param element
   * @param msg Message for to pass if element is empty
   * @throws NullPointerException if {@code element} is {@code null}
   * @exception IllegalStateException If element is empty, with the given message.
   * @return
   */
  public static XMLElement requireNotEmpty(XMLElement element, String msg) {
    Objects.requireNonNull(element, "xml element can't be null");
    if(element.isEmpty()) {
      throw new IllegalArgumentException(msg);
    }
    return element;
  }
  
  /**
   * See
   * {@link XMLElement#requireNotEmpty(XMLElement, String) }
   * passing default message <br>"The xml element can't be empty".
   */
  public static XMLElement requireNotEmpty(XMLElement element) {
    return requireNotEmpty(element, "The xml element can't be empty");
  }

  public void add(XMLElement element) {
    Objects.requireNonNull(element, "xml element can't be null");
    elements.add(element);
  }

  public void addAll(List<XMLElement> of) {
    Objects.requireNonNull(of, "the list can't be null");
    elements.addAll(of);
  }
  
}
