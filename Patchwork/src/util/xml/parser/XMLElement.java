package util.xml.parser;

import java.util.ArrayList;
import java.util.Objects;

public class XMLElement extends ArrayList<XMLElement> {
  private static final long serialVersionUID = 9004215052565055553L;
  private final String tag;
  private String content = "";
  
  public XMLElement(String tag, Object content) {
    if(!tag.matches("\\w+")) {
      throw new IllegalArgumentException("The given tag does not respect the pattern [a-zA-Z_0-9]+ : " + tag);
    }
    this.tag = Objects.requireNonNull(tag, "The tag can't be null");
    this.content = Objects.requireNonNull(content, "The content can't be null").toString();
  }
  
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
