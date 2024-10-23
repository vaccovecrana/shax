package io.vacco.shax.otel;

/**
 * An unsafe function wrapper.
 */
public interface OtFn<I, O> {
  O apply(I i) throws Exception;
}
