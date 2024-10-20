package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtResource {

  public List<OtAttribute> attributes;

  public OtResource att(OtAttribute a) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(a);
    return this;
  }

  public static OtResource otResource() {
    return new OtResource();
  }

}
