package io.vacco.shax.logging;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ShLogRecord extends LinkedHashMap<String, Object> {

  public enum ShLrField {
    environment, utc, utc_ms, thread_name, message,
    logger_name, level, level_value, stack_trace
  }

  public static String toString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  public static ShLogRecord from(ShLogConfig config, String message, String logName,
                                 ShLogLevel logLevel, Throwable t, ShArgument... args) {
    ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    ShLogRecord r = new ShLogRecord();

    if (config.environment != null) {
      r.put(ShLrField.environment.name(), config.environment);
    }

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
