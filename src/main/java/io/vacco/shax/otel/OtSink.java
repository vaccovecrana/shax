package io.vacco.shax.otel;

import io.vacco.shax.logging.ShLogRecord;

public interface OtSink {
  void accept(ShLogRecord lr);
  void accept(OtSpan sp);
}
