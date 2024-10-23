package io.vacco.shax.logging;

import java.util.Arrays;
import java.util.Optional;

import static org.slf4j.spi.LocationAwareLogger.*;

public enum ShLogLevel {

  TRACE(TRACE_INT),
  DEBUG(DEBUG_INT),
  INFO(INFO_INT),
  WARN(WARN_INT),
  ERROR(ERROR_INT),
  OFF(ERROR_INT + 10);

  private final int rawLevel;

  ShLogLevel(int rawLevel) {
    this.rawLevel = rawLevel;
  }

  public int getRawLevel() { return rawLevel; }

  public static ShLogLevel fromString(String levelStr) {
    if (levelStr == null) return ShLogLevel.INFO;
    Optional<ShLogLevel> l = Arrays.stream(ShLogLevel.values())
        .filter(lvl -> lvl.name().equalsIgnoreCase(levelStr.trim()))
        .findFirst();
    return l.orElse(ShLogLevel.INFO);
  }

}