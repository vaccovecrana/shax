package io.vacco.shax.otel;

import java.util.Objects;

public class OtAttribute {

  public String   key;
  public OtValue  value;

  public static OtAttribute att(String key, String value) {
    var a = new OtAttribute();
    a.key = Objects.requireNonNull(key);
    a.value = OtValue.val(value);
    return a;
  }

  public static OtAttribute att(String key, Integer value) {
    var a = new OtAttribute();
    a.key = Objects.requireNonNull(key);
    a.value = OtValue.val(value);
    return a;
  }

}
