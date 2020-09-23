package io.vacco.shax.json;

import java.io.IOException;
import java.util.*;
import java.util.stream.*;

public class ShJsonArray extends ShJsonValue {

  private List<ShJsonValue> values = new ArrayList<>();

  public ShJsonArray add(Stream<ShJsonValue> vs) {
    this.values = vs.collect(Collectors.toList());
    return this;
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeArrayOpen();
    for (int k = 0; k < values.size(); k++) {
      ShJsonValue v = values.get(k);
      if (v != null) {
        v.write(writer);
      } else {
        ShJsonLiteral.NULL.write(writer);
      }
      if (k < values.size() - 1) {
        writer.writeArraySeparator();
      }
    }
    writer.writeArrayClose();
  }
}
