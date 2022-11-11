package util.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class JsonObject extends LinkedHashMap<String, ArrayList<JsonObject>> {
 
  private static final long serialVersionUID = 6897858190612012976L;
 


  // ,(?=[^"]*(?:"[^"]*"[^"]*)*$)
  // ,(?=[^\"]*(?:\"[^\"]*\"[^\"]*)*$)
  // ,(?![^{]*\})
  // ,(?![^{]*\}|\])
  // ,(?![^\[\]]*\])
}
