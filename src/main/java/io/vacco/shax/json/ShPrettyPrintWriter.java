package io.vacco.shax.json;

import java.io.IOException;
import java.io.Writer;

public class ShPrettyPrintWriter extends ShJsonWriter {

  private final char[] indentChars;
  private int indent;

  public ShPrettyPrintWriter(Writer writer, char[] indentChars) {
    super(writer);
    this.indentChars = indentChars;
  }

  @Override
  protected void writeArrayOpen() throws IOException {
    indent++;
    writer.write('[');
  }

  @Override
  protected void writeArrayClose() throws IOException {
    indent--;
    writer.write(']');
  }

  @Override
  protected void writeArraySeparator() throws IOException {
    writer.write(',');
    writer.write(' ');
  }

  @Override
  protected void writeObjectOpen() throws IOException {
    indent++;
    writer.write('{');
    writeNewLine();
  }

  @Override
  protected void writeObjectClose() throws IOException {
    indent--;
    writeNewLine();
    writer.write('}');
  }

  @Override
  protected void writeMemberSeparator() throws IOException {
    writer.write(':');
    writer.write(' ');
  }

  @Override
  protected void writeObjectSeparator() throws IOException {
    writer.write(',');
    if (!writeNewLine()) {
      writer.write(' ');
    }
  }

  private boolean writeNewLine() throws IOException {
    if (indentChars == null) {
      return false;
    }
    writer.write('\n');
    for (int i = 0; i < indent; i++) {
      writer.write(indentChars);
    }
    return true;
  }
}