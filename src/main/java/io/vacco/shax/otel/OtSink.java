package io.vacco.shax.otel;

public interface OtSink {
  void accept(OtLogRecord lr);
  void accept(OtSpan sp);
}
