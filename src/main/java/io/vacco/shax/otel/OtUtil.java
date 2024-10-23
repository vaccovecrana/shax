package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.schema.OtValue;
import java.time.Instant;
import java.util.Random;

import static io.vacco.shax.json.ShReflect.toWrapperClass;
import static io.vacco.shax.otel.schema.OtValue.val;

public class OtUtil {

  private static final Random r = new Random();
  private static final ShObjectWriter ow = new ShObjectWriter(true, true);

  public static long nowNs() {
    var now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  }

  public static long msToNs(long millis) {
    return millis * 1_000_000L;
  }

  public static String traceId() {
    return String.format("%016x%016x", r.nextLong(), r.nextLong());
  }

  public static String spanId() {
    return String.format("%016x", r.nextLong());
  }

  public static OtValue valueOf(Object o) {
    if (o == null) {
      return val(null, null, null, null, null, null);
    }
    var cl = toWrapperClass(o.getClass());
    if (String.class.isAssignableFrom(cl)) {
      return val((String) o, null, null, null, null, null);
    } else if (Boolean.class.isAssignableFrom(cl)) {
      return val(null, (boolean) o, null, null, null, null);
    } else if (Integer.class.isAssignableFrom(cl)) {
      return val(null, null, (Integer) o, null, null, null);
    } else if (Long.class.isAssignableFrom(cl)) {
      return val(null, null, null, (Long) o, null, null);
    } else if (Float.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, (Float) o, null);
    } else if (Double.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, null, (Double) o);
    } else {
      return val(ow.apply(o), null, null, null, null, null);
    }
  }

}
