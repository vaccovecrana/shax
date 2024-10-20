package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtScopeLog {

  public OtScope            scope;
  public List<OtLogRecord>  logRecords;

  public OtScopeLog logRecord(OtLogRecord lr) {
    if (logRecords == null) {
      logRecords = new ArrayList<>();
    }
    logRecords.add(lr);
    return this;
  }

  public static OtScopeLog otScopeLog(OtScope scope) {
    var sl = new OtScopeLog();
    sl.scope = scope;
    return sl;
  }

}
