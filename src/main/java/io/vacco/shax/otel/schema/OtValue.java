package io.vacco.shax.otel.schema;

public class OtValue { // TODO improve this if there's enough demand.

  public String   stringValue;
  public Boolean  boolValue;
  public Long     intValue; // I know... I know...
  public Double   doubleValue;

  public static OtValue val(String vs, Boolean bv, Integer iv, Long lv, Float fv, Double dv) {
    var v = new OtValue();
    if (vs != null) {
      v.stringValue = vs;
    } else if (bv != null) {
      v.boolValue = bv;
    } else if (iv != null) {
      v.intValue = (long) iv;
    } else if (lv != null) {
      v.intValue = lv;
    } else if (fv != null) {
      v.doubleValue = (double) fv;
    } else if (dv != null) {
      v.doubleValue = dv;
    } else {
      v.stringValue = "null";
    }
    return v;
  }

  public static OtValue val(String vs) {
    return val(vs, null, null, null, null, null);
  }

}
