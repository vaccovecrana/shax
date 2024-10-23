package io.vacco.shax.logging;

import java.util.Objects;

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
    return String.format("{%s=%s}",
        key, value != null ? value.getClass().getCanonicalName() : "null"
    );
  }

}
