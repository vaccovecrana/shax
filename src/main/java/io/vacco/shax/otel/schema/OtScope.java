package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtScope {

  public String name, version;
  public List<OtAttribute> attributes;

  public OtScope att(OtAttribute a) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(a);
    return this;
  }

  public static OtScope otScope(String name, String version) {
    var s = new OtScope();
    s.name = name;
    s.version = version;
    return s;
  }

}
