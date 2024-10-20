package io.vacco.shax.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("serial")
public class ShJsonObject extends ShJsonValue {

  private final List<String> names;
  private final List<ShJsonValue> values;

  public ShJsonObject() {
    names = new ArrayList<>();
    values = new ArrayList<>();
  }

  public ShJsonObject set(String name, ShJsonValue value) {
    names.add(Objects.requireNonNull(name));
    values.add(Objects.requireNonNull(value));
    return this;
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeObjectOpen();
    var namesIterator = names.iterator();
    var valuesIterator = values.iterator();
    if (namesIterator.hasNext()) {
      writer.writeMemberName(namesIterator.next());
      writer.writeMemberSeparator();
      valuesIterator.next().write(writer);
      while (namesIterator.hasNext()) {
        writer.writeObjectSeparator();
        writer.writeMemberName(namesIterator.next());
        writer.writeMemberSeparator();
        valuesIterator.next().write(writer);
      }
    }
    writer.writeObjectClose();
  }

}
