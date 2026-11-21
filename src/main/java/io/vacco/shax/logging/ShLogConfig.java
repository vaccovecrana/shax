package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.System.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

public class ShLogConfig {

  public String otUrl, otHeaders;
  public Integer otTimeoutMs;

  public volatile ShLogLevel defaultLogLevel;
  public Map<String, ShLogLevel> logLevels = new ConcurrentHashMap<>();

  public boolean showDateTime = true;
  public boolean prettyPrint  = false;
  public boolean devMode      = false;

  private static <T> T loadProp(ShOption o, Function<String, T> onLoad) {
    var v = getenv(o.name());
    if (v == null) v = System.getProperty(o.asSysProp());
    return onLoad.apply(v);
  }

  public static String normalize(String loggerName) {
    return loggerName == null ? "" : loggerName.trim().toLowerCase(Locale.ROOT);
  }

  public static ShLogConfig load() {
    var c = new ShLogConfig();

    c.showDateTime = loadProp(ShOption.IO_VACCO_SHAX_SHOWDATETIME, v -> v == null || parseBoolean(v));
    c.defaultLogLevel = loadProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, ShLogLevel::fromString);
    c.prettyPrint = loadProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, Boolean::parseBoolean);
    c.devMode = loadProp(ShOption.IO_VACCO_SHAX_DEVMODE, Boolean::parseBoolean);
    c.otUrl = loadProp(ShOption.OTEL_EXPORTER_OTLP_ENDPOINT, Function.identity());
    c.otHeaders = loadProp(ShOption.OTEL_EXPORTER_OTLP_HEADERS, Function.identity());
    c.otTimeoutMs = loadProp(ShOption.OTEL_EXPORTER_OTLP_TIMEOUT, v -> v == null ? OtHttpSink.TimeoutDefaultMs : parseInt(v));

    var envPrefix = ShOption.IO_VACCO_SHAX_LOGGER.name();
    getenv().forEach((k, v) -> {
      if (k.startsWith(envPrefix) && k.length() > envPrefix.length()) {
        var logName = k.substring(envPrefix.length());
        if (logName.startsWith("_")) {
          logName = logName.substring(1);
        }
        c.logLevels.put(normalize(logName.replace("_", ".")), ShLogLevel.fromString(v));
      }
    });

    var sysPrefix = ShOption.IO_VACCO_SHAX_LOGGER.asSysProp();
    getProperties().forEach((k0, v0) -> {
      var k = k0.toString().trim();
      if (k.startsWith(sysPrefix) && k.length() > sysPrefix.length()) {
        var logName = k.substring(sysPrefix.length());
        if (logName.startsWith(".")) {
          logName = logName.substring(1);
        }
        c.logLevels.put(normalize(logName), ShLogLevel.fromString(v0.toString()));
      }
    });

    OtContext.init(c.otUrl, c.otHeaders, c.otTimeoutMs);
    return c;
  }

  @Override public String toString() {
    return "ShLogConfig " + new ShObjectWriter(false, true).apply(this);
  }

}
