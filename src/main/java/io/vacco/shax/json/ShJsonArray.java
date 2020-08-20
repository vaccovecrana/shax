package io.vacco.shax.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShJsonArray extends ShJsonValue implements Iterable<ShJsonValue> {

  private final List<ShJsonValue> values = new ArrayList<>();

  public ShJsonArray add(Stream<ShJsonValue> vs) {
    this.values.addAll(vs.collect(Collectors.toList()));
    return this;
  }

  public Iterator<ShJsonValue> iterator() {
    final Iterator<ShJsonValue> iterator = values.iterator();
    return new Iterator<ShJsonValue>() {
      public boolean hasNext() {
        return iterator.hasNext();
      }
      public ShJsonValue next() {
        return iterator.next();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeArrayOpen();
    Iterator<ShJsonValue> iterator = iterator();
    if (iterator.hasNext()) {
      iterator.next().write(writer);
      while (iterator.hasNext()) {
        writer.writeArraySeparator();
        iterator.next().write(writer);
      }
    }
    writer.writeArrayClose();
  }
}
