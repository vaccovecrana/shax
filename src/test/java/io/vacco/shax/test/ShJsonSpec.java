package io.vacco.shax.test;

import io.vacco.shax.json.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import static j8spec.J8Spec.describe;
import static j8spec.J8Spec.it;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ShJsonSpec {

  static {
    describe("Object traversal", () ->
        it("can extract object values from an input", () -> {
          MyPojo p = MyPojo.getInstance();
          System.out.println(new ShObjectWriter().apply(p, true, true));
          System.out.println(new ShObjectWriter().apply(p, false, false));
        })
    );
  }
}
