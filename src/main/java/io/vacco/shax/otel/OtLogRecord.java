package io.vacco.shax.otel;

import java.util.ArrayList;
import java.util.List;

public class OtLogRecord {

  public long               timeUnixNano;
  public int                severityNumber;
  public String             severityText;
  public OtValue            body;
  public List<OtAttribute>  attributes;

  public OtLogRecord body(OtValue body) {
    this.body = body;
    return this;
  }

  public OtLogRecord att(OtAttribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
    return this;
  }

  public static OtLogRecord otLogRecord(long timeUnixNano, int severityNumber, String severityText) {
    var r = new OtLogRecord();
    r.timeUnixNano = timeUnixNano;
    r.severityNumber = severityNumber;
    r.severityText = severityText;
    return r;
  }

}
