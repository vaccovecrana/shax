package io.vacco.shax.logging;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ShLogRecord {

  public enum ShLrField {
    utc, utcMs, thread, message, logName, logLevel, stackTrace
  }

  public static String toString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  public static Map<String, Object> from(ShLogConfig config, String message, String logName,
                                         ShLogLevel logLevel, Throwable t, ShArgument... args) {
    ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    Map<String, Object> r = new LinkedHashMap<>();

    if (config.showDateTime) {
      r.put(ShLrField.utc.name(), DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(nowUtc));
      r.put(ShLrField.utcMs.name(), nowUtc.toInstant().toEpochMilli());
    }

    r.put(ShLrField.thread.name(), Thread.currentThread().getName());
    r.put(ShLrField.message.name(), Objects.requireNonNull(message));
    r.put(ShLrField.logName.name(), Objects.requireNonNull(logName));
    r.put(ShLrField.logLevel.name(), Objects.requireNonNull(logLevel));

    if (t != null) {
      r.put(ShLrField.stackTrace.name(), toString(t));
    }
    if (args != null) {
      for (ShArgument arg : args) {
        r.put(arg.key, arg.value);
      }
    }

    return r;
  }
}
