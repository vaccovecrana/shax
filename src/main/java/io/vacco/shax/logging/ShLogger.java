package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.OtContext;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;
import java.util.function.Function;

import static io.vacco.shax.logging.ShLogLevel.*;
import static io.vacco.shax.logging.ShColor.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class ShLogger extends LegacyAbstractLogger {

  private static boolean initialized = false;
  private static ShLogConfig logConfig;

  protected ShLogLevel explicitLogLevel;
  protected Function<ShLogRecord, ShLogRecord> recordTransformer;

  private final ShObjectWriter objectWriter;

  protected static void lazyInit() {
    if (initialized) { return; }
    initialized = true;
    logConfig = ShLogConfig.load();
    System.err.println(magentaBoldBright("Shax!"));
    System.err.println(new ShObjectWriter(false, true).apply(logConfig));
  }

  protected ShLogger(String name) {
    this.name = name;
    var tempName = name;
    var level = (ShLogLevel) null;
    int indexOfLastDot = tempName.length();

    while ((level == null) && (indexOfLastDot > -1)) {
      tempName = tempName.substring(0, indexOfLastDot);
      level = logConfig.logLevels.get(tempName);
      indexOfLastDot = tempName.lastIndexOf(".");
    }

    this.explicitLogLevel = level;
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
    var shLevel = switch (level) {
      case TRACE -> TRACE;
      case DEBUG -> DEBUG;
      case INFO  -> INFO;
      case WARN  -> WARN;
      case ERROR -> ERROR;
    };
    var r = ShLogRecord.from(
      logConfig, tp.getMessage(), this.name, shLevel,
      throwable != null ? throwable : tp.getThrowable(), kvArgs
    );
    if (this.recordTransformer != null) {
      r = this.recordTransformer.apply(r);
    }
    if (logConfig.devMode) {
      System.err.println(messageFormat(
        shLevel,
        (Long) r.get(ShField.utc_ms.name()),
        r.get(ShField.thread_name.name()).toString(),
        r.get(ShField.message.name()).toString()
      ));
      for (var kvArg : kvArgs) {
        System.err.println(objectWriter.apply(kvArg.value));
      }
      if (tp.getThrowable() != null) {
        tp.getThrowable().printStackTrace(System.err);
      }
    } else {
      var json = objectWriter.apply(r);
      System.err.println(json);
    }
    System.err.flush();

    if (OtContext.sink != null) {
      OtContext.sink.accept(OtContext.mapFrom(r));
    }
  }

  protected boolean isLevelEnabled(ShLogLevel logLevel) {
    var effective = explicitLogLevel != null ? explicitLogLevel : logConfig.defaultLogLevel;
    return logLevel.getRawLevel() >= effective.getRawLevel();
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

}
