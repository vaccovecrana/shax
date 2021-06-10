package io.vacco.shax.json;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ShObjectScanner {

  private final Map<Object, Object> refs = new IdentityHashMap<>();

  protected Object mark(Object o) {
    if (o == null) return null;
    if (!ShReflect.isBaseType(o)) {
      if (!refs.containsKey(o)) {
        refs.put(o, o);
        return o;
      }
      return null;
    }
    return o;
  }
}
