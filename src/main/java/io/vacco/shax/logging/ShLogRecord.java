package io.vacco.shax.logging;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ShLogRecord {

  public String utc;
  public long utcMs;

  public String thread;
  public String message;
  public String logName;

  public ShLogLevel logLevel;

  public Map<String, Object> ext;
  public Throwable error;

  public static ShLogRecord from(String message, String logName,
                                 ShLogLevel logLevel, Throwable t,
                                 ShArgument ... args) {
    ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    ShLogRecord r = new ShLogRecord();

    r.utc = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(nowUtc);
    r.utcMs = nowUtc.toInstant().toEpochMilli();
    r.thread = Thread.currentThread().getName();
    r.message = Objects.requireNonNull(message);
    r.logName = Objects.requireNonNull(logName);
    r.logLevel = Objects.requireNonNull(logLevel);
    r.error = t; // TODO serialize error to stack trace string perhaps?

    if (args != null) {
      r.ext = new TreeMap<>();
      for (ShArgument arg : args) {
        r.ext.put(arg.key, arg.value);
      }
    }

    return r;
  }
}
