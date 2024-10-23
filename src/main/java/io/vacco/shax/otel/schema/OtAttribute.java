package io.vacco.shax.otel.schema;

import java.util.Objects;

public class OtAttribute {

  public String   key;
  public OtValue  value;

  public static OtAttribute att(String key, OtValue value) {
    var a = new OtAttribute();
    a.key = Objects.requireNonNull(key);
    a.value = value;
    return a;
  }

}
