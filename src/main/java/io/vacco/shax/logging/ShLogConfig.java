package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;
import java.util.*;
import java.util.function.Function;

public class ShLogConfig {

  public String environment;
  public ShLogLevel defaultLogLevel;
  public Map<String, ShLogLevel> logLevels = new HashMap<>();

  public boolean showDateTime = true;
  public boolean prettyPrint  = false;
  public boolean devMode      = false;
  public boolean julOutput    = false;

  private static <T> T loadProp(ShOption o, Function<String, T> onLoad) {
    String v = System.getenv(o.name());
    if (v == null) v = System.getProperty(o.asSysProp());
    return onLoad.apply(v);
  }

  public static ShLogConfig load() {
    ShLogConfig c = new ShLogConfig();

    c.environment = loadProp(ShOption.IO_VACCO_SHAX_ENVIRONMENT, v -> v == null ? "dev" : v);
    c.showDateTime = loadProp(ShOption.IO_VACCO_SHAX_SHOWDATETIME, v -> v == null || Boolean.parseBoolean(v));
    c.defaultLogLevel = loadProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, ShLogLevel::fromString);
    c.prettyPrint = loadProp(ShOption.IO_VACCO_SHAX_PRETTYPRINT, Boolean::parseBoolean);
    c.devMode = loadProp(ShOption.IO_VACCO_SHAX_DEVMODE, Boolean::parseBoolean);
    c.julOutput = loadProp(ShOption.IO_VACCO_SHAX_JULOUTPUT, Boolean::parseBoolean);

    System.getenv().forEach((k, v) -> {
      if (k.startsWith(ShOption.IO_VACCO_SHAX_LOGGER.name())) {
        String logName = k.replace(ShOption.IO_VACCO_SHAX_LOGGER.name(), "")
            .substring(1).toLowerCase().replace("_", ".");
        c.logLevels.put(logName, ShLogLevel.fromString(v));
      }
    });

    System.getProperties().forEach((k0, v0) -> {
      String k = k0.toString().toLowerCase().trim();
      if (k.startsWith(ShOption.IO_VACCO_SHAX_LOGGER.asSysProp())) {
        String logName = k.replace(ShOption.IO_VACCO_SHAX_LOGGER.asSysProp(), "")
            .substring(1);
        c.logLevels.put(logName, ShLogLevel.fromString(v0.toString()));
      }
    });

    return c;
  }

  @Override
  public String toString() {
    return "ShLogConfig " + new ShObjectWriter(false, true).apply(this);
  }

}
