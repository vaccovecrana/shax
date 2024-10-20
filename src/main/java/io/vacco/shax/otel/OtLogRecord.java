package io.vacco.shax.otel;

import io.vacco.shax.logging.ShLogRecord;
import java.util.ArrayList;
import java.util.List;

import static io.vacco.shax.otel.OtValue.val;

public class OtLogRecord {

  public static final String
    OtLoggerName = "logger.name",
    OtThreadName = "thread.name",
    OtThreadId   = "thread.id";

  public long               timeUnixNano;
  public String             severityNumber; // why Otel? SEVERITY_NUMBER_INFO? Really Otel? Really?
  public String             severityText;
  public OtValue            body;
  public List<OtAttribute>  attributes;

  public OtLogRecord body(OtValue body) {
    this.body = body;
    return this;
  }

  public OtLogRecord att(OtAttribute attribute) {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    attributes.add(attribute);
    return this;
  }

  public static OtLogRecord otLogRecord(long timeUnixNano, String severityNumber, String severityText) {
    var r = new OtLogRecord();
    r.timeUnixNano = timeUnixNano;
    r.severityNumber = severityNumber;
    r.severityText = severityText;
    return r;
  }

  public static long msToNs(long millis) {
    return millis * 1_000_000L;
  }

  public static OtLogRecord mapFrom(ShLogRecord lr) {
    var ns = msToNs(lr.utcMs());
    var lvn = String.format("SEVERITY_NUMBER_%s", lr.level);
    var otLr = otLogRecord(ns, lvn, lr.level.name())
      .body(val(lr.message))
      .att(OtAttribute.att(OtLoggerName, lr.logName))
      .att(OtAttribute.att(OtThreadName, lr.threadName))
      .att(OtAttribute.att(OtThreadId, lr.threadId));
    for (var arg : lr.kvArgs) {
      // otLr.att(att(arg.key, arg.value));
    }
    return otLr;
  }

}
