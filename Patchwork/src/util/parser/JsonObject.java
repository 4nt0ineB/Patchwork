package util.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class JsonObject extends LinkedHashMap<String, JsonObject> {
 
  private static final long serialVersionUID = 6897858190612012976L;
  
  private final String id;
  private final LinkedHashSet<JsonObject> values = new LinkedHashSet<>();
  
  public JsonObject(String id, JsonObject jsonObject){
    this.id = id;
  }
 
  private static JsonObject fromStream(Scanner scanner) {
    var json = new JsonObject(".", null);
    scanner.useDelimiter(Pattern.compile(","));
    var jsonValuePattern = "(\\S+:\\s+.*,)";
    jsonValuePattern = "\\w*:\\h*\\w*";
    var jsonListPattern = " \\S+:\\s*\\[.*\\](?:,?)";
    // (\w+:.*,)
    //
    
    if(scanner.hasNext()) {
      System.out.println(scanner.next());
      System.out.println(scanner.next());
      System.out.println(scanner.next());
      System.out.println(scanner.next());
    }
    
//    while(
//        scanner.hasNext(jsonValuePattern)
//        || scanner.hasNext(jsonListPattern)
//        ) {
//      if(scanner.hasNext(jsonValuePattern)) {
//        System.out.println(scanner.next(jsonValuePattern));
//      }
//      
//    }
    
    
    return json;
  }
  
  public static JsonObject fromFile(Path path) throws IOException {
    JsonObject o = null;
    try(var reader = Files.newBufferedReader(path)){
      var scanner = new Scanner(reader);
      if(scanner.hasNext("\\{")) {
        scanner.next("\\{");
        o = fromStream(scanner);
      }
      // scanner.next("\\}");
    }
    return o;
  }
  



  // ,(?=[^"]*(?:"[^"]*"[^"]*)*$)
  // ,(?=[^\"]*(?:\"[^\"]*\"[^\"]*)*$)
  // ,(?![^{]*\})
  // ,(?![^{]*\}|\])
  // ,(?![^\[\]]*\])
}
