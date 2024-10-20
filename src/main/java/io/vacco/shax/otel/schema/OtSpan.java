package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtSpan {

  public String             traceId, spanId, parentSpanId;
  public String             name;
  public OtSpanKind         kind;
  public long               startTimeUnixNano, endTimeUnixNano;
  public List<OtAttribute>  attributes;
  public OtStatus           status;

  public OtSpan parentSpanId(String parentSpanId) {
    this.parentSpanId = parentSpanId;
    return this;
  }

  public OtSpan start(long startTimeUnixNano) {
    this.startTimeUnixNano = startTimeUnixNano;
    return this;
  }

  public OtSpan end(long endTimeUnixNano) {
    this.endTimeUnixNano = endTimeUnixNano;
    return this;
  }

  public OtSpan status(OtStatus status) {
    this.status = status;
    return this;
  }

  public OtSpan att(OtAttribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
    return this;
  }

  public static OtSpan otSpan(String traceId, String spanId, String name, OtSpanKind kind) {
    var r = new OtSpan();
    r.traceId = traceId;
    r.spanId = spanId;
    r.name = name;
    r.kind = kind;
    return r;
  }

}
