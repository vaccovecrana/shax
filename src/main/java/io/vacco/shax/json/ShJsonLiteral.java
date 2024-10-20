package io.vacco.shax.json;

import java.io.IOException;

@SuppressWarnings("serial")
public class ShJsonLiteral extends ShJsonValue {

  public static final ShJsonLiteral NULL = new ShJsonLiteral("null");
  public static final ShJsonLiteral TRUE = new ShJsonLiteral("true");
  public static final ShJsonLiteral FALSE = new ShJsonLiteral("false");

  private final String value;

  ShJsonLiteral(String value) {
    this.value = value;
  }

  @Override
  public void write(ShJsonWriter writer) throws IOException {
    writer.writeLiteral(value);
  }

}
