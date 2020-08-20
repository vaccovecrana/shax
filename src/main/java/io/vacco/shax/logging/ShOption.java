package io.vacco.shax.logging;

public enum ShOption {

  IO_VACCO_SHAX_SHOWDATETIME,
  IO_VACCO_SHAX_LOGLEVEL,
  IO_VACCO_SHAX_PRETTYPRINT,
  IO_VACCO_SHAX_LOGGER;

  public String asSysProp() {
    return this.name().toLowerCase().replace("_", ".");
  }
}
