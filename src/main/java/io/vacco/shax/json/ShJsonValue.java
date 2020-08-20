package io.vacco.shax.json;

import java.io.IOException;
import java.io.Serializable;

import static io.vacco.shax.json.ShReflectionUtil.*;
import static io.vacco.shax.json.ShJsonLiteral.*;

public abstract class ShJsonValue implements Serializable {

  private static final String numError = "Infinite and NaN values not permitted in JSON";

  public abstract void write(ShJsonWriter writer) throws IOException;

  private static String cutOffPointZero(String string) {
    if (string.endsWith(".0")) {
      return string.substring(0, string.length() - 2);
    }
    return string;
  }

  public static ShJsonValue fromValue(Object o) {

    if (isBoolean(o)) return ((Boolean) o) ? TRUE : FALSE;
    if (isTextual(o) || isEnum(o)) return new ShJsonString(o.toString());
    if (isInteger(o)) {
      if (o instanceof Integer) return new ShJsonNumber(Integer.toString((Integer) o, 10));
      return new ShJsonNumber(Long.toString((Long) o, 10));
    }

    if (isRational(o)) {
      if (o instanceof Float) {
        float f = (Float) o;
        if (Float.isInfinite(f) || Float.isNaN(f)) {
          throw new IllegalArgumentException(numError);
        }
        return new ShJsonNumber(cutOffPointZero(Float.toString(f)));
      }
      if (o instanceof Double) {
        double d = (Double) o;
        if (Double.isInfinite(d) || Double.isNaN(d)) {
          throw new IllegalArgumentException(numError);
        }
        return new ShJsonNumber(cutOffPointZero(Double.toString(d)));
      }
    }

    throw new IllegalArgumentException(String.format("Not a value type: [%s]", o));
  }
}
