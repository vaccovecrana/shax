package io.vacco.shax.otel.schema;

import java.util.ArrayList;
import java.util.List;

public class OtBatch {

  public List<OtResourceLog> resourceLogs;
  public List<OtResourceSpan> resourceSpans;

  public OtBatch resourceLog(OtResourceLog rl) {
    if (resourceLogs == null) {
      resourceLogs = new ArrayList<>();
    }
    resourceLogs.add(rl);
    return this;
  }

  public OtBatch resourceSpan(OtResourceSpan rs) {
    if (resourceSpans == null) {
      resourceSpans = new ArrayList<>();
    }
    resourceSpans.add(rs);
    return this;
  }

  public static OtBatch otBatch() {
    return new OtBatch();
  }

}
