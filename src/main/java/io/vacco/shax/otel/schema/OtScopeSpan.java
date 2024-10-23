package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtScopeSpan {

  public OtScope scope;
  public List<OtSpan<?>> spans;

  public OtScopeSpan span(OtSpan<?> span) {
    if (spans == null) {
      spans = new ArrayList<>();
    }
    spans.add(span);
    return this;
  }

  public static OtScopeSpan otScopeSpan(OtScope scope) {
    var ss = new OtScopeSpan();
    ss.scope = scope;
    return ss;
  }

}
