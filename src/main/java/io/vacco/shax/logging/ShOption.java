package io.vacco.shax.logging;

public enum ShOption {

  IO_VACCO_SHAX_LOG_LEVEL,
  IO_VACCO_SHAX_PRETTY_PRINT,
  IO_VACCO_SHAX_LOGGER;

  public String asSysProp() {
    return this.name().toLowerCase().replace("_", ".");
  }
}
