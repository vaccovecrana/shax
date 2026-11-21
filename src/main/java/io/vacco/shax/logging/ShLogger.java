package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.OtContext;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;

import static io.vacco.shax.logging.ShLogLevel.*;
import static io.vacco.shax.logging.ShColor.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class ShLogger extends LegacyAbstractLogger {

  public static final int MAX_LOG_RECORD_SIZE = 8192;

  private static volatile boolean initialized = false;
  private static volatile ShLogConfig logConfig;
  private static PrintStream ps = System.err;

  protected Function<ShLogRecord, ShLogRecord> recordTransformer;

  private final String normalizedName;
  private final ShObjectWriter objectWriter;

  protected static synchronized void lazyInit() {
    if (initialized) { return; }
    logConfig = ShLogConfig.load();
    ps.println(magentaBoldBright("Shax!"));
    ps.println(new ShObjectWriter(false, true).apply(logConfig));
    initialized = true;
  }

  protected ShLogger(String name) {
    lazyInit();
    this.name = name;
    this.normalizedName = ShLogConfig.normalize(name);
    this.objectWriter = new ShObjectWriter(true, logConfig.prettyPrint);
  }

  public static String messageFormat(ShLogLevel level, Long utcMs, String threadName, String message) {
    return format("%s %s%s %s",
      labelFor(level),
      logConfig.showDateTime ?
        blackBoldBright(
          format("[%s] ", utcMs == null ? System.currentTimeMillis() : utcMs)
        ) : "",
      bluePale(format("(%s)", threadName)),
      message
    );
  }

  @Override
  protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern,
                                             Object[] arguments, Throwable throwable) {
    var tp = MessageFormatter.arrayFormat(messagePattern, arguments);
    var kvArgs = Arrays.stream(tp.getArgArray() != null ? tp.getArgArray() : new Object[]{})
      .filter(o -> o instanceof ShArgument)
      .toArray(ShArgument[]::new);
    var shLevel = (ShLogLevel) null;
    switch (level) {
      case TRACE: shLevel = TRACE; break;
      case DEBUG: shLevel = DEBUG; break;
      case INFO:  shLevel = INFO;  break;
      case WARN:  shLevel = WARN;  break;
      case ERROR: shLevel = ERROR; break;
    };
    var r = ShLogRecord.from(
      logConfig, tp.getMessage(), this.name, shLevel,
      throwable != null ? throwable : tp.getThrowable(), kvArgs
    );
    if (this.recordTransformer != null) {
      r = this.recordTransformer.apply(r);
    }
    if (logConfig.devMode) {
      ps.println(messageFormat(
        shLevel,
        (Long) r.get(ShField.utc_ms.name()),
        r.get(ShField.thread_name.name()).toString(),
        r.get(ShField.message.name()).toString()
      ));
      for (var kvArg : kvArgs) {
        ps.println(objectWriter.apply(kvArg.value));
      }
      if (throwable != null) {
        throwable.printStackTrace(ps);
      }
    } else {
      var json = objectWriter.apply(r);
      var dropped = json.length() > MAX_LOG_RECORD_SIZE;
      if (dropped) {
        var summary = format(
          "Log entry dropped [logger=%s, level=%s] - payload exceeds %dKB limit",
          this.name, shLevel, MAX_LOG_RECORD_SIZE
        );
        r = ShLogRecord.from(logConfig, summary, this.name, shLevel, null);
        json = objectWriter.apply(r);
      }
      ps.println(json);
    }
    ps.flush();

    if (OtContext.sink != null) {
      OtContext.sink.accept(OtContext.mapFrom(r));
    }
  }

  private static ShLogLevel resolveLevel(ShLogConfig config, String normalizedLoggerName) {
    var candidate = normalizedLoggerName;
    while (true) {
      var level = config.logLevels.get(candidate);
      if (level != null) {
        return level;
      }
      var indexOfLastDot = candidate.lastIndexOf(".");
      if (indexOfLastDot < 0) {
        break;
      }
      candidate = candidate.substring(0, indexOfLastDot);
    }
    return config.defaultLogLevel != null ? config.defaultLogLevel : INFO;
  }

  protected boolean isLevelEnabled(ShLogLevel logLevel) {
    var effective = resolveLevel(logConfig, normalizedName);
    return logLevel.getRawLevel() >= effective.getRawLevel();
  }

  public static ShLogLevel getLoggerLevel(String loggerName) {
    requireNonNull(loggerName);
    lazyInit();
    return resolveLevel(logConfig, ShLogConfig.normalize(loggerName));
  }

  public static void setLoggerLevel(String loggerName, ShLogLevel level) {
    requireNonNull(loggerName);
    requireNonNull(level);
    lazyInit();
    logConfig.logLevels.put(ShLogConfig.normalize(loggerName), level);
  }

  public static void clearLoggerLevel(String loggerName) {
    requireNonNull(loggerName);
    lazyInit();
    logConfig.logLevels.remove(ShLogConfig.normalize(loggerName));
  }

  static void setLoggerLevelIfInitialized(String loggerName, ShLogLevel level) {
    requireNonNull(loggerName);
    requireNonNull(level);
    if (initialized && logConfig != null) {
      logConfig.logLevels.put(ShLogConfig.normalize(loggerName), level);
    }
  }

  public static Logger withTransformer(Logger log, Function<ShLogRecord, ShLogRecord> fn) {
    requireNonNull(log);
    requireNonNull(fn);
    if (log instanceof ShLogger) {
      var l = (ShLogger) log;
      if (l.recordTransformer != null) {
        throw new IllegalArgumentException(
            format("logger already defines a transform function: [%s]", l.recordTransformer)
        );
      }
      l.recordTransformer = fn;
      return l;
    }
    throw new IllegalArgumentException(format("Not a shax logger: [%s]", log));
  }

  public boolean isTraceEnabled() { return isLevelEnabled(TRACE); }
  public boolean isDebugEnabled() { return isLevelEnabled(DEBUG); }
  public boolean isInfoEnabled() { return isLevelEnabled(INFO); }
  public boolean isWarnEnabled() { return isLevelEnabled(WARN); }
  public boolean isErrorEnabled() { return isLevelEnabled(ERROR); }

  @Override
  protected String getFullyQualifiedCallerName() {
    return ShLogger.class.getName();
  }

  public static void setRootLoggerLevel(ShLogLevel level) {
    lazyInit();
    if (logConfig != null) {
      logConfig.defaultLogLevel = level != null ? level : INFO;
    }
  }

  public static void setPrintStream(PrintStream out) {
    ShLogger.ps = Objects.requireNonNull(out);
  }

}
