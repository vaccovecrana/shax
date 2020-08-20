package io.vacco.shax.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShReflectionUtil {

  public static boolean isInteger(Object o) {
    return o instanceof Integer
        || o instanceof Byte
        || o instanceof Short
        || o instanceof Long;
  }

  public static boolean isRational(Object o) {
    return o instanceof Double || o instanceof Float;
  }

  public static boolean isBoolean(Object o) {
    return o instanceof Boolean;
  }

  public static boolean isEnum(Object o) { return o instanceof Enum<?>; }

  public static boolean isTextual(Object o) {
    return o instanceof String
        || o instanceof Character;
  }

  public static boolean isCollection(Object o) {
    return o instanceof List<?>
        || o instanceof Map<?, ?>
        || o instanceof Set<?>
        || o != null && o.getClass().isArray();
  }

  public static boolean isBaseType(Object o) {
    return isInteger(o)
        || isRational(o)
        || isTextual(o)
        || isEnum(o)
        || isBoolean(o)
        || isCollection(o);
  }
}
