package io.vacco.shax.otel;

import io.vacco.shax.otel.schema.OtLogRecord;
import io.vacco.shax.otel.schema.OtSpan;

public interface OtSink {
  void accept(OtLogRecord lr);
  void accept(OtSpan<?> sp);
}
