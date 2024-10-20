package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtResourceSpan {

  public OtResource         resource;
  public List<OtScopeSpan>  scopeSpans;

  public OtResourceSpan scopeSpan(OtScopeSpan span) {
    if (scopeSpans == null) {
      scopeSpans = new ArrayList<>();
    }
    scopeSpans.add(span);
    return this;
  }

  public static OtResourceSpan otResourceSpan(OtResource resource) {
    var s = new OtResourceSpan();
    s.resource = resource;
    return s;
  }

}
