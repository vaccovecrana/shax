package io.vacco.shax.otel;

import io.vacco.shax.logging.*;
import java.net.URI;
import java.time.Instant;
import java.util.Random;

import static java.lang.String.format;

public class OtContext {

  private static final Random r = new Random();
  public static OtSink sink;

  public static void init(URI otUrl, String otScopeName, String otScopeVersion) {
    if (otUrl != null) {
      if (otScopeName == null || otScopeVersion == null) {
        throw new IllegalArgumentException(format(
          "%s environment property was configured as [%s], "
            + "but no scope name or version has been defined. Please define "
            + "%s and %s.",
          ShOption.OTEL_COLLECTOR_URL, otUrl,
          ShOption.OTEL_SCOPE_NAME, ShOption.OTEL_SCOPE_VERSION
        ));
      }
      sink = new OtHttpSink(otUrl);
    }
  }

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
