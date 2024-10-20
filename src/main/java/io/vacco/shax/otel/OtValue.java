package io.vacco.shax.otel;

public class OtValue {

  public String   stringValue;
  public Integer  intValue;

  public static OtValue val(String v) {
    var ov = new OtValue();
    ov.stringValue = v;
    return ov;
  }

  public static OtValue val(Integer v) {
    var ov = new OtValue();
    ov.intValue = v;
    return ov;
  }

}
