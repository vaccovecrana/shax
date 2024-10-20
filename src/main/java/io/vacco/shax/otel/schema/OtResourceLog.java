package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtResourceLog {

  public OtResource       resource;
  public List<OtScopeLog> scopeLogs;

  public OtResourceLog scopeLog(OtScopeLog log) {
    if (scopeLogs == null) {
      scopeLogs = new ArrayList<>();
    }
    scopeLogs.add(log);
    return this;
  }

  public static OtResourceLog otResourceLog(OtResource resource) {
    var r = new OtResourceLog();
    r.resource = resource;
    return r;
  }

}
