package io.vacco.shax.otel;

import java.time.Instant;

public class Otel {

  public static long nowNs() {
    var now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  }

}
