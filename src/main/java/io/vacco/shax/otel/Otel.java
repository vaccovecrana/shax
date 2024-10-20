package io.vacco.shax.otel;

import java.time.Instant;
import java.util.Random;

public class Otel {

  private static final Random r = new Random();

  public static long nowNs() {
    var now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  }

  public static String traceId() {
    return Long.toHexString(r.nextLong());
  }

  public static String spanId() {
    return Integer.toHexString(r.nextInt());
  }

}
