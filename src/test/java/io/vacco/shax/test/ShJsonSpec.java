package io.vacco.shax.test;

import io.vacco.shax.json.*;
import io.vacco.shax.logging.ShOption;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ShJsonSpec {

  static {

    final ShObjectWriter w = new ShObjectWriter(false, true);

    describe("JSON serialization", () -> {
      it("can emit primitive values", () -> {
        System.out.println(w.apply(1));
        System.out.println(w.apply(2L));
        System.out.println(w.apply(3.0f));
        System.out.println(w.apply(4.0));
      });
      it("can emit value types", () -> {
        System.out.println(w.apply("Hello world"));
        System.out.println(w.apply(ShOption.IO_VACCO_SHAX_LOGGER));
        System.out.println(w.apply(true));
        System.out.println(w.apply(null));
      });
      it("can emit collection value types", () -> {
        System.out.println(w.apply(Arrays.asList(1, 2, 3, 4)));
        System.out.println(w.apply(new TreeSet<>(Arrays.asList("dog", "cat", "bird", "fish"))));
        Map<String, Object> foos = new TreeMap<>();
        foos.put("one", null);
        foos.put("two", "2");
        foos.put("three", 3);
        foos.put("four", new Object[] {4});
        System.out.println(w.apply(foos));
      });
      it("can extract object values from an input", () -> {
        MyPojo p = MyPojo.getInstance();
        System.out.println(new ShObjectWriter(true, true).apply(p));
        System.out.println(new ShObjectWriter(false, false).apply(p));
      });
    });
  }
}
