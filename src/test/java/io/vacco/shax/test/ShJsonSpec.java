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
        var foos = new TreeMap<String, Object>();
        foos.put("one", null);
        foos.put("two", "2");
        foos.put("three", 3);
        foos.put("four", new Object[] {4});
        foos.put("five", new double[] {0.0, 2.0, 3.1415});
        System.out.println(w.apply(foos));

        System.out.println(w.apply(new boolean[] {true, false, true}));
        System.out.println(w.apply(new int[]   {0, 1, 2, 3, 4, 5, 6}));
        System.out.println(w.apply(new char[]  {'a', 'b', 'c', 'd', 'e'}));
        System.out.println(w.apply(new long[]  {0, 1, 2, 3, 4, 5, 6}));
        System.out.println(w.apply(new float[] {0, 1, 2, 3, 4, 5, 6}));
        System.out.println(w.apply(new short[] {0, 1, 2, 3, 4, 5, 6}));
        System.out.println(w.apply(new byte[]  {0, 1, 2, 3, 4, 5, 6}));
      });
      it("can extract object values from an input", () -> {
        var p = MyPojo.getInstance();
        System.out.println(new ShObjectWriter(true, true).apply(p));
        System.out.println(new ShObjectWriter(false, false).apply(p));
      });
    });
  }
}
