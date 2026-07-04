package io.vacco.shax.otel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtSchema {

  public static final String
    Shax = "shax", Java = "java",

    OtHostName      = "host.name",
    OsName          = "os.name",
    OtOsType        = "os.type",
    OtOsDescription = "os.description",
    OtOsVersion     = "os.version",
    OtOsArch        = "os.arch",

    OtProcessPid                = "process.pid",
    OtProcessExecutableName     = "process.executable.name",
    OtProcessRuntimeName        = "process.runtime.name",
    OtProcessRuntimeVersion     = "process.runtime.version",
    OtProcessRuntimeDescription = "process.runtime.description",
    OtProcessRuntimeVendor      = "process.runtime.vendor",

    OtTelemetrySdkName      = "telemetry.sdk.name",
    OtTelemetrySdkVersion   = "telemetry.sdk.version",
    OtTelemetrySdkLanguage  = "telemetry.sdk.language";

  public static final String
    OtServiceName       = "service.name",
    OtServiceNamespace  = "service.namespace",
    OtServiceInstanceId = "service.instance.id";

  public static final String
    OtLoggerName          = "logger.name",
    OtThreadName          = "thread.name",
    OtThreadId            = "thread.id",
    OtExceptionMessage    = "exception.message",
    OtExceptionStacktrace = "exception.stacktrace",
    OtExceptionType       = "exception.type";

  public static class Attribute {
    public String   key;
    public Value value;

    public static Attribute att(String key, Value value) {
      var a = new Attribute();
      a.key = Objects.requireNonNull(key);
      a.value = value;
      return a;
    }
  }

  public static class Batch {
    public List<ResourceLog> resourceLogs;
    public List<ResourceSpan> resourceSpans;

    public Batch resourceLog(ResourceLog rl) {
      if (resourceLogs == null) {
        resourceLogs = new ArrayList<>();
      }
      resourceLogs.add(rl);
      return this;
    }

    public Batch resourceSpan(ResourceSpan rs) {
      if (resourceSpans == null) {
        resourceSpans = new ArrayList<>();
      }
      resourceSpans.add(rs);
      return this;
    }

    public static Batch otBatch() {
      return new Batch();
    }
  }

  public static class Resource {
    public List<Attribute> attributes;

    public Resource att(Attribute a) {
      if (attributes == null) {
        attributes = new ArrayList<>();
      }
      attributes.add(a);
      return this;
    }

    public static Resource otResource() {
      return new Resource();
    }
  }

  public static class ResourceLog {
    public Resource resource;
    public List<ScopeLog> scopeLogs;

    public ResourceLog scopeLog(ScopeLog log) {
      if (scopeLogs == null) {
        scopeLogs = new ArrayList<>();
      }
      scopeLogs.add(log);
      return this;
    }

    public static ResourceLog otResourceLog(Resource resource) {
      var r = new ResourceLog();
      r.resource = resource;
      return r;
    }
  }

  public static class ResourceSpan {
    public Resource resource;
    public List<ScopeSpan>  scopeSpans;

    public ResourceSpan scopeSpan(ScopeSpan span) {
      if (scopeSpans == null) {
        scopeSpans = new ArrayList<>();
      }
      scopeSpans.add(span);
      return this;
    }

    public static ResourceSpan otResourceSpan(Resource resource) {
      var s = new ResourceSpan();
      s.resource = resource;
      return s;
    }
  }

  public static class Scope {
    public String name, version;
    public List<Attribute> attributes;

    public Scope att(Attribute a) {
      if (attributes == null) {
        attributes = new ArrayList<>();
      }
      attributes.add(a);
      return this;
    }

    public static Scope otScope(String name, String version) {
      var s = new Scope();
      s.name = name;
      s.version = version;
      return s;
    }
  }

  public static class ScopeLog {
    public Scope scope;
    public List<LogRecord>  logRecords;

    public ScopeLog logRecord(LogRecord lr) {
      if (logRecords == null) {
        logRecords = new ArrayList<>();
      }
      logRecords.add(lr);
      return this;
    }

    public static ScopeLog otScopeLog(Scope scope) {
      var sl = new ScopeLog();
      sl.scope = scope;
      return sl;
    }
  }

  public static class ScopeSpan {
    public Scope scope;
    public List<Span<?>> spans;

    public ScopeSpan span(Span<?> span) {
      if (spans == null) {
        spans = new ArrayList<>();
      }
      spans.add(span);
      return this;
    }

    public static ScopeSpan otScopeSpan(Scope scope) {
      var ss = new ScopeSpan();
      ss.scope = scope;
      return ss;
    }
  }

  public enum StatusCode {
    STATUS_CODE_UNSET, STATUS_CODE_OK, STATUS_CODE_ERROR
  }

  public static class Status {
    public StatusCode code;
    public String message;

    public Status message(String message) {
      this.message = message;
      return this;
    }

    public static Status otStatus(StatusCode code) {
      var s = new Status();
      s.code = code;
      return s;
    }
  }

  public static class Value { // TODO improve this if there's enough demand.
    public String   stringValue;
    public Boolean  boolValue;
    public Long     intValue; // I know... I know...
    public Double   doubleValue;

    public static Value val(String vs, Boolean bv, Integer iv, Long lv, Float fv, Double dv) {
      var v = new Value();
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

    public static Value val(String vs) {
      return val(vs, null, null, null, null, null);
    }
  }

  public static class LogRecord {
    public long               timeUnixNano;
    public String             severityNumber; // why Otel? SEVERITY_NUMBER_INFO? Really Otel? Really?
    public String             severityText;
    public Value body;
    public List<Attribute>  attributes;

    public LogRecord body(Value body) {
      this.body = body;
      return this;
    }

    public LogRecord att(Attribute attribute) {
      if (attributes == null) {
        attributes = new ArrayList<>();
      }
      attributes.add(attribute);
      return this;
    }

    public LogRecord att(String key, String val) {
      att(Attribute.att(key, Value.val(val)));
      return this;
    }

    public LogRecord fail(Throwable t) {
      att(OtExceptionType, t.getClass().getCanonicalName());
      att(OtExceptionMessage, t.getMessage());
      att(OtExceptionStacktrace, Span.stackTraceOf(t));
      return this;
    }

    public static LogRecord otLogRecord(long timeUnixNano, String severityNumber, String severityText) {
      var r = new LogRecord();
      r.timeUnixNano = timeUnixNano;
      r.severityNumber = severityNumber;
      r.severityText = severityText;
      return r;
    }
  }

  public enum SpanKind {
    SPAN_KIND_INTERNAL,
    SPAN_KIND_SERVER, SPAN_KIND_CLIENT,
    SPAN_KIND_PRODUCER, SPAN_KIND_CONSUMER
  }

  public static class Span<T> {
    public long               startTimeUnixNano, endTimeUnixNano;
    public String             traceId, spanId, parentSpanId;
    public String             name;
    public SpanKind           kind;
    public List<Attribute>    attributes;
    public Status             status;

    public transient T          result;
    public transient Throwable  error;

    public Span<T> parentSpanId(String parentSpanId) {
      this.parentSpanId = parentSpanId;
      return this;
    }

    public Span<T> start(long startTimeUnixNano) {
      this.startTimeUnixNano = startTimeUnixNano;
      return this;
    }

    public Span<T> end(long endTimeUnixNano) {
      this.endTimeUnixNano = endTimeUnixNano;
      return this;
    }

    public Span<T> att(Attribute attribute) {
      if (attributes == null) {
        attributes = new ArrayList<>();
      }
      attributes.add(attribute);
      return this;
    }

    public Span<T> att(String key, String val) {
      att(Attribute.att(key, Value.val(val)));
      return this;
    }

    public Span<T> att(String key, int val) {
      att(Attribute.att(key, Value.val(null, null, val, null, null, null)));
      return this;
    }

    public Span<T> fail(Throwable t) {
      if (t != null) {
        error = t;
        status = Status.otStatus(StatusCode.STATUS_CODE_ERROR);
        att(OtExceptionType, t.getClass().getCanonicalName());
        att(OtExceptionMessage, t.getMessage());
        att(OtExceptionStacktrace, stackTraceOf(t));
      }
      return this;
    }

    public Span<T> ok(T result) {
      this.result = result;
      status = Status.otStatus(StatusCode.STATUS_CODE_OK);
      return this;
    }

    public static <K> Span<K> otSpan(String traceId, String spanId,
                                     String name, SpanKind kind) {
      var r = new Span<K>();
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

}