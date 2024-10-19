package io.vacco.shax.test;

import io.vacco.shax.otel.OtAttributes;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtSpec {
  static {
    it("Loads standard OTEL attributes", () -> {
      var otIdx = OtAttributes.otIdx;
      otIdx.forEach((k, v) -> {
        System.out.printf("%s -> %s%n", k, v);
      });
    });
  }
}
