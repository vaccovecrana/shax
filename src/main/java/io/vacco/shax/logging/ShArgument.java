package io.vacco.shax.logging;

public class ShArgument {

  public String key;
  public Object value;

  public static ShArgument kv(String key, Object value) {
    ShArgument a = new ShArgument();
    a.key = key;
    a.value = value;
    return a;
  }
}
