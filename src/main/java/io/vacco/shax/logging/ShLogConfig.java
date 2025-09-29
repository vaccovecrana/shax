package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.*;
import java.util.*;
import java.util.function.Function;

import static java.lang.System.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

public class ShLogConfig {

  public String otUrl, otHeaders;
  public Integer otTimeoutMs;

  public ShLogLevel defaultLogLevel;
  public Map<String, ShLogLevel> logLevels = new HashMap<>();

  public boolean showDateTime = true;
  public boolean prettyPrint  = false;
  public boolean devMode      = false;

  private static <T> T loadProp(ShOption o, Function<String, T> onLoad) {
    var v = getenv(o.name());
    if (v == null) v = System.getProperty(o.asSysProp());
    return onLoad.apply(v);
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

    getenv().forEach((k, v) -> {
      if (k.startsWith(ShOption.IO_VACCO_SHAX_LOGGER.name())) {
        var logName = k
            .replace(ShOption.IO_VACCO_SHAX_LOGGER.name(), "")
            .substring(1).toLowerCase().replace("_", ".");
        c.logLevels.put(logName, ShLogLevel.fromString(v));
      }
    });

    getProperties().forEach((k0, v0) -> {
      var k = k0.toString().trim();
      if (k.startsWith(ShOption.IO_VACCO_SHAX_LOGGER.asSysProp())) {
        var logName = k
          .replace(ShOption.IO_VACCO_SHAX_LOGGER.asSysProp(), "")
          .substring(1);
        c.logLevels.put(logName, ShLogLevel.fromString(v0.toString()));
      }
    });

    OtContext.init(c.otUrl, c.otHeaders, c.otTimeoutMs);
    return c;
  }

  @Override public String toString() {
    return "ShLogConfig " + new ShObjectWriter(false, true).apply(this);
  }

}
