package util.parser;

import java.util.ArrayList;
import java.util.Objects;

public class JsonAtomic {
  
  public final ArrayList<JsonObject> values = new ArrayList<>();
  
  public JsonAtomic(String id, String value){
    Objects.requireNonNull(id);
    Objects.requireNonNull(value);
  }
}
