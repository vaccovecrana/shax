package io.vacco.shax.otel.schema;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static io.vacco.shax.otel.schema.OtConstants.*;

public class OtSpan<T> {

  public long               startTimeUnixNano, endTimeUnixNano;
  public String             traceId, spanId, parentSpanId;
  public String             name;
  public OtSpanKind         kind;
  public List<OtAttribute>  attributes;
  public OtStatus           status;

  public transient T          result;
  public transient Throwable  error;

  public OtSpan<T> parentSpanId(String parentSpanId) {
    this.parentSpanId = parentSpanId;
    return this;
  }

  public OtSpan<T> start(long startTimeUnixNano) {
    this.startTimeUnixNano = startTimeUnixNano;
    return this;
  }

  public OtSpan<T> end(long endTimeUnixNano) {
    this.endTimeUnixNano = endTimeUnixNano;
    return this;
  }

  public OtSpan<T> att(OtAttribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
    return this;
  }

  public OtSpan<T> att(String key, String val) {
    att(OtAttribute.att(key, OtValue.val(val)));
    return this;
  }

  public OtSpan<T> att(String key, int val) {
    att(OtAttribute.att(key, OtValue.val(null, null, val, null, null, null)));
    return this;
  }

  public OtSpan<T> fail(Throwable t) {
    if (t != null) {
      error = t;
      status = OtStatus.otStatus(OtStatusCode.STATUS_CODE_ERROR);
      att(OtExceptionType, t.getClass().getCanonicalName());
      att(OtExceptionMessage, t.getMessage());
      att(OtExceptionStacktrace, stackTraceOf(t));
    }
    return this;
  }

  public OtSpan<T> ok(T result) {
    this.result = result;
    status = OtStatus.otStatus(OtStatusCode.STATUS_CODE_OK);
    return this;
  }

  public static <K> OtSpan<K> otSpan(String traceId, String spanId,
                                     String name, OtSpanKind kind) {
    var r = new OtSpan<K>();
    r.traceId = traceId;
    r.spanId = spanId;
    r.name = name;
    r.kind = kind;
    return r;
  }

  public static String stackTraceOf(Throwable t) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

}
