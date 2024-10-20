package io.vacco.shax.json;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShReflect {

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

  public static Class<?> toWrapperClass(Class<?> type) {
    if (!type.isPrimitive()) return type;
    else if (int.class.equals(type)) { return Integer.class; }
    else if (double.class.equals(type)) { return Double.class; }
    else if (char.class.equals(type)) { return Character.class; }
    else if (boolean.class.equals(type)) { return Boolean.class; }
    else if (long.class.equals(type)) { return Long.class; }
    else if (float.class.equals(type)) { return Float.class; }
    else if (short.class.equals(type)) { return Short.class; }
    else if (byte.class.equals(type)) { return Byte.class; }
    return type;
  }

  public static Object[] wrap(Object o) {
    var type = o.getClass();
    if (Object[].class.isAssignableFrom(type)) return (Object[]) o;
    int al;
    var oa = new Object[] {};
    if (int[].class.equals(type))     { al = ((int[]) o).length;      oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((int[]) o)[i]; } }
    if (double[].class.equals(type))  { al = ((double[]) o).length;   oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((double[]) o)[i]; } }
    if (char[].class.equals(type))    { al = ((char[]) o).length;     oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((char[]) o)[i]; } }
    if (boolean[].class.equals(type)) { al = ((boolean[]) o).length;  oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((boolean[]) o)[i]; } }
    if (long[].class.equals(type))    { al = ((long[]) o).length;     oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((long[]) o)[i]; } }
    if (float[].class.equals(type))   { al = ((float[]) o).length;    oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((float[]) o)[i]; } }
    if (short[].class.equals(type))   { al = ((short[]) o).length;    oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((short[]) o)[i]; } }
    if (byte[].class.equals(type))    { al = ((byte[]) o).length;     oa = new Object[al]; for (int i = 0; i < al; i++) { oa[i] = ((byte[]) o)[i]; } }
    return oa;
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
