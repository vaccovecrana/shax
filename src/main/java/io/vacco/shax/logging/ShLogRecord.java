package io.vacco.shax.logging;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("serial")
public final class ShLogRecord extends LinkedHashMap<String, Object> {

  public transient Throwable throwable;

  public enum ShLrField {
    utc, utc_ms, thread_name, message,
    logger_name, level, level_value, stack_trace
  }

  public static String toString(Throwable t) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  public static ShLogRecord from(ShLogConfig config, String message, String logName,
                                 ShLogLevel logLevel, Throwable t, ShArgument... args) {
    var nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    var r = new ShLogRecord();

    if (config.showDateTime) {
      r.put(ShLrField.utc.name(), DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(nowUtc));
      r.put(ShLrField.utc_ms.name(), nowUtc.toInstant().toEpochMilli());
    }

    r.put(ShLrField.level.name(), Objects.requireNonNull(logLevel));
    r.put(ShLrField.level_value.name(), logLevel.getRawLevel());
    r.put(ShLrField.logger_name.name(), Objects.requireNonNull(logName));
    r.put(ShLrField.thread_name.name(), Thread.currentThread().getName());
    r.put(ShLrField.message.name(), Objects.requireNonNull(message));

    if (t != null) {
      r.throwable = t;
      r.put(ShLrField.stack_trace.name(), toString(t));
    }
    if (args != null) {
      for (ShArgument arg : args) {
        r.put(arg.key, arg.value);
      }
    }

    return r;
  }

}
