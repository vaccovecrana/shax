package io.vacco.shax.logging;

import io.vacco.shax.otel.schema.OtSpan;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("serial")
public final class ShLogRecord extends LinkedHashMap<String, Object> {

  private static final ZoneId UTC = ZoneId.of("UTC");

  public transient Throwable      throwable;
  public transient ShLogLevel     level;
  public transient ZonedDateTime  utc;
  public transient ShArgument[]   kvArgs;
  public transient String         message, logName, threadName;
  public transient int            threadId;

  public static ShLogRecord from(ShLogConfig config, String message, String logName,
                                 ShLogLevel logLevel, Throwable t, ShArgument... args) {
    var r = new ShLogRecord();
    r.utc = ZonedDateTime.now(UTC);
    r.level = logLevel;
    r.kvArgs = args;
    r.message = message;
    r.logName = Objects.requireNonNull(logName);
    r.threadName = Thread.currentThread().getName();
    r.threadId = Thread.currentThread().getName().hashCode();

    if (config.showDateTime) {
      r.put(ShField.utc.name(), DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(r.utc));
      r.put(ShField.utc_ms.name(), r.utcMs());
    }

    r.put(ShField.level.name(), Objects.requireNonNull(logLevel));
    r.put(ShField.level_value.name(), logLevel.getRawLevel());
    r.put(ShField.logger_name.name(), r.logName);
    r.put(ShField.thread_name.name(), r.threadName);
    r.put(ShField.message.name(), Objects.requireNonNull(message));

    if (t != null) {
      r.throwable = t;
      r.put(ShField.stack_trace.name(), OtSpan.stackTraceOf(t));
    }
    if (args != null) {
      for (var arg : args) {
        r.put(arg.key, arg.value);
      }
    }

    return r;
  }

  public long utcMs() {
    return utc.toInstant().toEpochMilli();
  }

}
