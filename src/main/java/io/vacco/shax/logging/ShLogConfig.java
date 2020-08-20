package io.vacco.shax.logging;

import io.vacco.shax.json.ShObjectWriter;

import java.util.*;

public class ShLogConfig {

  public ShLogLevel defaultLogLevel;
  public Map<String, ShLogLevel> logLevels = new HashMap<>();

  public boolean prettyPrint = false;

  private static String loadProp(ShOption o) {
    String v = System.getenv(o.name());
    if (v == null) v = System.getProperty(o.asSysProp());
    return v;
  }

  public static ShLogConfig load() {
    ShLogConfig c = new ShLogConfig();

    c.defaultLogLevel = ShLogLevel.fromString(loadProp(ShOption.IO_VACCO_SHAX_LOG_LEVEL));
    c.prettyPrint = Boolean.parseBoolean(loadProp(ShOption.IO_VACCO_SHAX_PRETTY_PRINT));

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
    return "ShLogConfig " + new ShObjectWriter().apply(this, true, false);
  }
}
