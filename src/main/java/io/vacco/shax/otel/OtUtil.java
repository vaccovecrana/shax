package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.schema.OtValue;
import java.time.Instant;
import java.util.*;

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

  public static String[] argOf(String arg) {
    var sep = arg.indexOf("=");
    if (sep == -1) {
      return new String[] { arg };
    }
    var a0 = new String[2];
    a0[0] = arg.substring(0, sep);
    a0[1] = arg.substring(sep + 1);
    return a0;
  }

  public static Map<String, String> otHeadersOf(String raw) {
    if (raw == null || raw.trim().isEmpty()) {
      return new HashMap<>();
    }
    var args = raw.split(",");
    return Arrays.stream(args)
      .map(OtUtil::argOf)
      .collect(
        HashMap::new,
        (m, v) -> m.put(v[0], v.length == 1 ? null : v[1]),
        HashMap::putAll
      );
  }

}
