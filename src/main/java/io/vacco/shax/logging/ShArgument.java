package io.vacco.shax.logging;

import java.util.Objects;
import static io.vacco.shax.json.ShReflect.*;

public class ShArgument {

  public String key;
  public Object value;

  public static ShArgument kv(String key, Object value) {
    var a = new ShArgument();
    a.key = Objects.requireNonNull(key);
    a.value = value;
    return a;
  }

  @Override public String toString() {
    String v = "null";
    if (value != null) {
      if (isBaseType(value) && !isCollection(value)) {
        v = value.toString();
      } else {
        v = value.getClass().getCanonicalName();
      }
    }
    return String.format("{%s=%s}", key, v);
  }

}
