package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.logging.*;
import java.net.URI;
import java.time.Instant;
import java.util.Random;

import static io.vacco.shax.json.ShReflect.*;
import static io.vacco.shax.otel.OtValue.val;
import static io.vacco.shax.otel.OtAttribute.att;
import static io.vacco.shax.otel.OtLogRecord.otLogRecord;
import static java.lang.String.format;

public class OtContext {

  public static final String
    OtServiceName = "service.name"

    /*
    ': 'app.js',
    'service.namespace': 'tutorial',
    'service.version': '1.0',
    'service.instance.id'
     */
  ;

  public static final String
      OtLoggerName = "logger.name",
      OtThreadName = "thread.name",
      OtThreadId   = "thread.id",
      OtExceptionMessage = "exception.message",
      OtExceptionStacktrace = "exception.stacktrace",
      OtExceptionType = "exception.type";

  private static final Random r = new Random();
  private static final ShObjectWriter ow = new ShObjectWriter(true, true);

  public static OtSink sink;

  public static void init(URI otUrl, String otScopeName, String otScopeVersion) {
    if (otUrl != null) {
      if (otScopeName == null || otScopeVersion == null) {
        throw new IllegalArgumentException(format(
          "%s environment property was configured as [%s], "
            + "but no scope name or version has been defined. Please define "
            + "%s and %s.",
          ShOption.OTEL_COLLECTOR_URL, otUrl,
          ShOption.OTEL_SCOPE_NAME, ShOption.OTEL_SCOPE_VERSION
        ));
      }
      sink = new OtHttpSink(otUrl);
    }
  }

  public static long nowNs() {
    var now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  }

  public static String traceId() {
    return Long.toHexString(r.nextLong());
  }

  public static String spanId() {
    return Integer.toHexString(r.nextInt());
  }

  public static long msToNs(long millis) {
    return millis * 1_000_000L;
  }

  public static OtValue valueOf(Object o) {
    if (o == null) {
      return val(null, null, null, null, null, null);
    }
    var cl = toWrapperClass(o.getClass());
    if (String.class.isAssignableFrom(cl)) {
      return val((String) o, null, null, null, null, null);
    } else if (Boolean.class.isAssignableFrom(cl)) {
      return val(null, (boolean) o, null, null, null, null);
    } else if (Integer.class.isAssignableFrom(cl)) {
      return val(null, null, (Integer) o, null, null, null);
    } else if (Long.class.isAssignableFrom(cl)) {
      return val(null, null, null, (Long) o, null, null);
    } else if (Float.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, (Float) o, null);
    } else if (Double.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, null, (Double) o);
    } else {
      return val(ow.apply(o), null, null, null, null, null);
    }
  }

  public static OtLogRecord mapFrom(ShLogRecord lr) {
    var ns = msToNs(lr.utcMs());
    var lvn = String.format("SEVERITY_NUMBER_%s", lr.level);
    var otLr = otLogRecord(ns, lvn, lr.level.name())
        .body(val(lr.message, null, null, null, null, null))
        .att(att(OtLoggerName, valueOf(lr.logName)))
        .att(att(OtThreadName, valueOf(lr.threadName)))
        .att(att(OtThreadId, valueOf(lr.threadId)));
    if (lr.throwable != null) {
      var t = lr.throwable;
      otLr.att(att(OtExceptionType, valueOf(t.getClass().getCanonicalName())));
      otLr.att(att(OtExceptionMessage, valueOf(t.getMessage())));
      otLr.att(att(OtExceptionStacktrace, valueOf(ShLogRecord.stackTraceOf(t))));
    }
    for (var arg : lr.kvArgs) {
      otLr.att(att(arg.key, valueOf(arg.value)));
    }
    return otLr;
  }

}
