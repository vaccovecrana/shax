package io.vacco.shax.json;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("serial")
class ShJsonString extends ShJsonValue {

  private final String string;

  ShJsonString(String string) {
    this.string = Objects.requireNonNull(string);
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeString(string);
  }

}
