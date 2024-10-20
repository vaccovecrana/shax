package io.vacco.shax.otel;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.logging.*;
import io.vacco.shax.otel.schema.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.time.Instant;
import java.util.*;

import static io.vacco.shax.json.ShReflect.*;
import static io.vacco.shax.otel.schema.OtConstants.*;
import static io.vacco.shax.otel.schema.OtBatch.otBatch;
import static io.vacco.shax.otel.schema.OtResource.otResource;
import static io.vacco.shax.otel.schema.OtResourceLog.otResourceLog;
import static io.vacco.shax.otel.schema.OtScope.otScope;
import static io.vacco.shax.otel.schema.OtScopeLog.otScopeLog;
import static io.vacco.shax.otel.schema.OtValue.val;
import static io.vacco.shax.otel.schema.OtAttribute.att;
import static io.vacco.shax.otel.schema.OtLogRecord.otLogRecord;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.net.InetAddress.getLocalHost;
import static java.util.Objects.requireNonNull;

public class OtContext {

  private static final Random r = new Random();
  private static final ShObjectWriter ow = new ShObjectWriter(true, true);

  public static final String OtPrefix = "OT_";
  public static final Map<String, String> otSysIdx = new LinkedHashMap<>();

  public static OtSink sink;

  static {
    otSysIdx.putAll(getJvmAttributes());
    otSysIdx.putAll(getEnvAttributes());
  }

  public static Map<String, String> envAttributesOf(Map<String, String> env) {
    var envIdx = new LinkedHashMap<String, String>();
    env.forEach((k, v) -> {
      if (k.startsWith(OtPrefix)) {
        var key = k
          .replace(OtPrefix, "")
          .replace("_", ".")
          .toLowerCase();
        envIdx.put(key, v);
      }
    });
    return envIdx;
  }

  public static Map<String, String> getEnvAttributes() {
    return envAttributesOf(getenv());
  }

  public static Map<String, String> getJvmAttributes() {
    var jvmIdx = new LinkedHashMap<String, String>();

    try {
      var hostname = getLocalHost().getHostName();
      jvmIdx.put(OtHostName, hostname);
    } catch (UnknownHostException e) {
      jvmIdx.put(OtHostName, "unknown");
    }

    jvmIdx.put(OtOsType, getProperty(OsName));
    jvmIdx.put(OtOsDescription, getProperty(OsName) + " " + getProperty(OtOsVersion));
    jvmIdx.put(OtOsVersion, getProperty(OtOsVersion));
    jvmIdx.put(OtOsArch, getProperty(OtOsArch));

    var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    var runtimeName = runtimeMXBean.getName(); // pid@hostname
    var pid = runtimeName.split("@")[0];

    jvmIdx.put(OtProcessPid, pid);
    jvmIdx.put(OtServiceInstanceId, Integer.toHexString(Integer.parseInt(pid)));

    var javaCommand = getProperty("sun.java.command");
    if (javaCommand != null) {
      var commandParts = javaCommand.split(" ");
      jvmIdx.put(OtProcessExecutableName, commandParts[0]);
      if (commandParts[0].contains(".")) {
        jvmIdx.put(OtServiceName, commandParts[0].substring(commandParts[0].lastIndexOf(".") + 1));
        jvmIdx.put(OtServiceNamespace, commandParts[0].substring(0, commandParts[0].lastIndexOf(".")));
      }
    } else {
      jvmIdx.put(OtProcessExecutableName, "unknown");
    }

    jvmIdx.put(OtProcessRuntimeName, getProperty("java.runtime.name"));
    jvmIdx.put(OtProcessRuntimeVersion, getProperty("java.runtime.version"));
    jvmIdx.put(OtProcessRuntimeDescription, getProperty("java.vm.name") + " " + getProperty("java.vm.version"));
    jvmIdx.put(OtProcessRuntimeVendor, getProperty("java.vm.vendor"));

    jvmIdx.put(OtTelemetrySdkName, Shax);
    jvmIdx.put(OtTelemetrySdkLanguage, Java);

    try (var is = OtContext.class.getResourceAsStream("/io/vacco/shax/version")) {
      var version = new String(requireNonNull(is).readAllBytes()).trim();
      jvmIdx.put(OtTelemetrySdkVersion, version);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return jvmIdx;
  }

  public static void init(String otUrl) {
    if (otUrl != null && sink == null) {
      sink = new OtHttpSink(URI.create(otUrl)).start();
    }
  }

  public static long nowNs() {
    var now = Instant.now();
    return now.getEpochSecond() * 1_000_000_000L + now.getNano();
  }

  public static String traceId() {
    return Long.toHexString(r.nextLong());
  }

  public static String spanId() {
    return Integer.toHexString(r.nextInt());
  }

  public static long msToNs(long millis) {
    return millis * 1_000_000L;
  }

  public static OtValue valueOf(Object o) {
    if (o == null) {
      return val(null, null, null, null, null, null);
    }
    var cl = toWrapperClass(o.getClass());
    if (String.class.isAssignableFrom(cl)) {
      return val((String) o, null, null, null, null, null);
    } else if (Boolean.class.isAssignableFrom(cl)) {
      return val(null, (boolean) o, null, null, null, null);
    } else if (Integer.class.isAssignableFrom(cl)) {
      return val(null, null, (Integer) o, null, null, null);
    } else if (Long.class.isAssignableFrom(cl)) {
      return val(null, null, null, (Long) o, null, null);
    } else if (Float.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, (Float) o, null);
    } else if (Double.class.isAssignableFrom(cl)) {
      return val(null, null, null, null, null, (Double) o);
    } else {
      return val(ow.apply(o), null, null, null, null, null);
    }
  }

  public static OtLogRecord mapFrom(ShLogRecord lr) {
    var ns = msToNs(lr.utcMs());
    var lvn = String.format("SEVERITY_NUMBER_%s", lr.level);
    var otLr = otLogRecord(ns, lvn, lr.level.name())
        .body(val(lr.message, null, null, null, null, null))
        .att(att(OtLoggerName, valueOf(lr.logName)))
        .att(att(OtThreadName, valueOf(lr.threadName)))
        .att(att(OtThreadId, valueOf(lr.threadId)));
    if (lr.throwable != null) {
      var t = lr.throwable;
      otLr.att(att(OtExceptionType, valueOf(t.getClass().getCanonicalName())));
      otLr.att(att(OtExceptionMessage, valueOf(t.getMessage())));
      otLr.att(att(OtExceptionStacktrace, valueOf(ShLogRecord.stackTraceOf(t))));
    }
    for (var arg : lr.kvArgs) {
      otLr.att(att(arg.key, valueOf(arg.value)));
    }
    return otLr;
  }

  public static OtBatch logBatchOf(List<OtLogRecord> logs) {
    var res = otResource();
    var scopeLog = otScopeLog(otScope(otSysIdx.get(OtTelemetrySdkName), otSysIdx.get(OtTelemetrySdkVersion)));
    for (var e : otSysIdx.entrySet()) {
      res.att(att(e.getKey(), val(e.getValue())));
    }
    for (var lr : logs) {
      scopeLog.logRecord(lr);
    }
    return otBatch()
      .resourceLog(
        otResourceLog(res)
          .scopeLog(scopeLog)
      );
  }

}
