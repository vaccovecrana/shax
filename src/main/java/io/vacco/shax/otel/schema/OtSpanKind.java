package io.vacco.shax.otel.schema;

public enum OtSpanKind {
  SPAN_KIND_INTERNAL,
  SPAN_KIND_SERVER, SPAN_KIND_CLIENT,
  SPAN_KIND_PRODUCER, SPAN_KIND_CONSUMER
}
