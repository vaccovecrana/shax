package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.OtContext;
import org.slf4j.Logger;
import org.slf4j.helpers.*;
import java.util.*;
import java.util.function.Function;

import static io.vacco.shax.logging.ShLogLevel.*;
import static io.vacco.shax.logging.ShColor.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class ShLogger extends MarkerIgnoringBase {

  private static boolean initialized = false;
  private static ShLogConfig logConfig;

  protected ShLogLevel currentLogLevel;
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

    this.currentLogLevel = level == null ? logConfig.defaultLogLevel : level;
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

  private void log(ShLogLevel level, FormattingTuple tp) {
    if (!isLevelEnabled(level)) {
      return;
    }

    var kvArgs = Arrays.stream(tp.getArgArray() != null ? tp.getArgArray() : new Object[]{})
      .filter(o -> o instanceof ShArgument)
      .toArray(ShArgument[]::new);
    var r = ShLogRecord.from(logConfig, tp.getMessage(), this.name, level, tp.getThrowable(), kvArgs);

    if (this.recordTransformer != null) {
      r = this.recordTransformer.apply(r);
    }
    if (logConfig.devMode) {
      System.err.println(messageFormat(
        level,
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

  private void formatAndLog(ShLogLevel level, String format, Object arg1, Object arg2) {
    if (!isLevelEnabled(level)) { return; }
    log(level, MessageFormatter.format(format, arg1, arg2));
  }

  private void formatAndLog(ShLogLevel level, String format, Object... arguments) {
    if (!isLevelEnabled(level)) { return; }
    log(level, MessageFormatter.arrayFormat(format, arguments));
  }

  protected boolean isLevelEnabled(ShLogLevel logLevel) {
    return (logLevel.getRawLevel() >= currentLogLevel.getRawLevel());
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
  public void trace(String msg) { log(TRACE, new FormattingTuple(msg, null, null)); }
  public void trace(String format, Object param1) { formatAndLog(TRACE, format, param1, null); }
  public void trace(String format, Object param1, Object param2) { formatAndLog(TRACE, format, param1, param2); }
  public void trace(String format, Object... argArray) { formatAndLog(TRACE, format, argArray); }
  public void trace(String msg, Throwable t) { log(TRACE, new FormattingTuple(msg, null, t)); }

  public boolean isDebugEnabled() { return isLevelEnabled(DEBUG); }
  public void debug(String msg) { log(DEBUG, new FormattingTuple(msg, null, null)); }
  public void debug(String format, Object param1) { formatAndLog(DEBUG, format, param1, null); }
  public void debug(String format, Object param1, Object param2) { formatAndLog(DEBUG, format, param1, param2); }
  public void debug(String format, Object... argArray) { formatAndLog(DEBUG, format, argArray); }
  public void debug(String msg, Throwable t) { log(DEBUG, new FormattingTuple(msg, null, t)); }

  public boolean isInfoEnabled() { return isLevelEnabled(INFO); }
  public void info(String msg) { log(INFO, new FormattingTuple(msg, null, null)); }
  public void info(String format, Object arg) { formatAndLog(INFO, format, arg, null); }
  public void info(String format, Object arg1, Object arg2) { formatAndLog(INFO, format, arg1, arg2); }
  public void info(String format, Object... argArray) { formatAndLog(INFO, format, argArray); }
  public void info(String msg, Throwable t) { log(INFO, new FormattingTuple(msg, null, t)); }

  public boolean isWarnEnabled() { return isLevelEnabled(WARN); }
  public void warn(String msg) { log(WARN, new FormattingTuple(msg, null, null)); }
  public void warn(String format, Object arg) { formatAndLog(WARN, format, arg, null); }
  public void warn(String format, Object arg1, Object arg2) { formatAndLog(WARN, format, arg1, arg2); }
  public void warn(String format, Object... argArray) { formatAndLog(WARN, format, argArray); }
  public void warn(String msg, Throwable t) { log(WARN, new FormattingTuple(msg, null, t)); }

  public boolean isErrorEnabled() { return isLevelEnabled(ERROR); }
  public void error(String msg) { log(ERROR, new FormattingTuple(msg, null, null)); }
  public void error(String format, Object arg) { formatAndLog(ERROR, format, arg, null); }
  public void error(String format, Object arg1, Object arg2) { formatAndLog(ERROR, format, arg1, arg2); }
  public void error(String format, Object... argArray) { formatAndLog(ERROR, format, argArray); }
  public void error(String msg, Throwable t) { log(ERROR, new FormattingTuple(msg, null, t)); }

}
