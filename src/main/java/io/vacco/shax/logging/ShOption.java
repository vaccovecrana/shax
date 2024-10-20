package io.vacco.shax.logging;

import static java.util.Objects.requireNonNull;

public enum ShOption {

  IO_VACCO_SHAX_DEVMODE,
  IO_VACCO_SHAX_SHOWDATETIME,
  IO_VACCO_SHAX_LOGLEVEL,
  IO_VACCO_SHAX_PRETTYPRINT,
  IO_VACCO_SHAX_LOGGER,

  OTEL_COLLECTOR_URL;

  public String asSysProp() {
    return this.name().toLowerCase().replace("_", ".");
  }

  public static void setSysProp(ShOption sh, String value) {
    System.setProperty(sh.asSysProp(), requireNonNull(value));
  }

  public static void setLoggerSysProp(String loggerName, ShLogLevel level) {
    var logNameProp = String.format(
        "%s.%s", ShOption.IO_VACCO_SHAX_LOGGER.asSysProp(),
        requireNonNull(loggerName)
    );
    System.setProperty(logNameProp, requireNonNull(level).name());
  }

}
