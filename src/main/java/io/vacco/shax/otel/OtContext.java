package io.vacco.shax.otel;

import io.vacco.shax.logging.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.util.*;

import static io.vacco.shax.logging.ShLogger.messageFormat;
import static io.vacco.shax.otel.OtSchema.*;
import static io.vacco.shax.otel.OtSchema.Batch.otBatch;
import static io.vacco.shax.otel.OtSchema.Resource.otResource;
import static io.vacco.shax.otel.OtSchema.ResourceLog.otResourceLog;
import static io.vacco.shax.otel.OtSchema.ResourceSpan.otResourceSpan;
import static io.vacco.shax.otel.OtSchema.Scope.otScope;
import static io.vacco.shax.otel.OtSchema.ScopeLog.otScopeLog;
import static io.vacco.shax.otel.OtSchema.ScopeSpan.otScopeSpan;
import static io.vacco.shax.otel.OtSchema.Span.otSpan;
import static io.vacco.shax.otel.OtUtil.*;
import static io.vacco.shax.otel.OtSchema.Value.val;
import static io.vacco.shax.otel.OtSchema.Attribute.att;
import static io.vacco.shax.otel.OtSchema.LogRecord.otLogRecord;
import static java.lang.System.*;
import static java.lang.Thread.currentThread;
import static java.net.InetAddress.getLocalHost;
import static java.util.Objects.requireNonNull;

public class OtContext {

  public static final String OtPrefix = "OT_";
  public static final Map<String, String> otSysIdx = new LinkedHashMap<>();
  public static final Resource processResource;
  public static final Scope processScope;

  public static OtSink sink;

  static {
    otSysIdx.putAll(getJvmAttributes());
    otSysIdx.putAll(getEnvAttributes());
    processResource = otResource();
    processScope = otScope(otSysIdx.get(OtTelemetrySdkName), otSysIdx.get(OtTelemetrySdkVersion));
    for (var e : otSysIdx.entrySet()) {
      processResource.att(att(e.getKey(), val(e.getValue())));
    }
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
    jvmIdx.put(OtServiceInstanceId, Integer.toHexString(runtimeName.hashCode()));

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

  public static void init(String otUrl, String otHeaders, Integer otTimeoutMs) {
    if (otUrl != null && sink == null) {
      sink = new OtHttpSink(
        URI.create(otUrl),
        OtUtil.otHeadersOf(otHeaders),
        otTimeoutMs
      ).start();
    }
  }

  public static LogRecord mapFrom(ShLogRecord lr) {
    var ns = msToNs(lr.utcMs());
    var lvn = String.format("SEVERITY_NUMBER_%s", lr.level);
    var otLr = otLogRecord(ns, lvn, lr.level.name())
        .body(val(lr.message, null, null, null, null, null))
        .att(att(OtLoggerName, valueOf(lr.logName)))
        .att(att(OtThreadName, valueOf(lr.threadName)))
        .att(att(OtThreadId, valueOf(lr.threadId)));
    if (lr.throwable != null) {
      otLr.fail(lr.throwable);
    }
    for (var arg : lr.kvArgs) {
      otLr.att(att(arg.key, valueOf(arg.value)));
    }
    return otLr;
  }

  public static Batch logBatchOf(List<LogRecord> logs) {
    var scopeLog = otScopeLog(processScope);
    for (var lr : logs) {
      scopeLog.logRecord(lr);
    }
    return otBatch().resourceLog(otResourceLog(processResource).scopeLog(scopeLog));
  }

  public static Batch spanBatchOf(List<Span<?>> spans) {
    var scopeSpan = otScopeSpan(processScope);
    for (var sr : spans) {
      scopeSpan.span(sr);
    }
    return otBatch().resourceSpan(otResourceSpan(processResource).scopeSpan(scopeSpan));
  }

  public static <K> Span<K> span(Span<?> parent, SpanKind kind, OtFn<Span<K>, Span<K>> op) {
    var m = "?";
    var stackTrace = Thread.currentThread().getStackTrace();
    for (int i = 1; i < stackTrace.length; i++) {
      if (!stackTrace[i].getMethodName().equals("span")) {
        m = stackTrace[i].getMethodName();
        break;
      }
    }
    var t = parent != null ? parent.traceId : traceId();
    Span<K> s = otSpan(t, spanId(), m, requireNonNull(kind));
    try {
      if (parent != null) {
        s.parentSpanId(parent.spanId);
      }
      s.start(nowNs());
      return op.apply(s);
    } catch (Exception e) {
      err.println(messageFormat(
          ShLogLevel.ERROR, currentTimeMillis(), currentThread().getName(),
          String.format("Unhandled span exception at method [%s]", m)
      ));
      e.printStackTrace(err);
      return s.fail(e);
    } finally {
      s.end(nowNs());
      if (s.status == null) {
        s.status = Status.otStatus(StatusCode.STATUS_CODE_UNSET);
      }
      if (sink != null) {
        sink.accept(s);
      }
    }
  }

  public static <K> Span<K> span(SpanKind kind, OtFn<Span<K>, Span<K>> op) {
    return span(null, kind, op);
  }

}
