package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

import static io.vacco.shax.otel.schema.OtConstants.*;

public class OtLogRecord {

  public long               timeUnixNano;
  public String             severityNumber; // why Otel? SEVERITY_NUMBER_INFO? Really Otel? Really?
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

  public OtLogRecord att(String key, String val) {
    att(OtAttribute.att(key, OtValue.val(val)));
    return this;
  }

  public OtLogRecord fail(Throwable t) {
    att(OtExceptionType, t.getClass().getCanonicalName());
    att(OtExceptionMessage, t.getMessage());
    att(OtExceptionStacktrace, OtSpan.stackTraceOf(t));
    return this;
  }

  public static OtLogRecord otLogRecord(long timeUnixNano, String severityNumber, String severityText) {
    var r = new OtLogRecord();
    r.timeUnixNano = timeUnixNano;
    r.severityNumber = severityNumber;
    r.severityText = severityText;
    return r;
  }

}
