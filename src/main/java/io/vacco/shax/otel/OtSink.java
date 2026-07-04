package io.vacco.shax.otel;

public interface OtSink {
  void accept(OtSchema.LogRecord lr);
  void accept(OtSchema.Span<?> sp);
}
