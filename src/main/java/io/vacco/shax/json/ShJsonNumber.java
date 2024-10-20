package io.vacco.shax.json;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("serial")
class ShJsonNumber extends ShJsonValue {

  private final String string;

  ShJsonNumber(String string) {
    this.string = Objects.requireNonNull(string);
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeNumber(string);
  }

}
