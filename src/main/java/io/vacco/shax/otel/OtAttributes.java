package io.vacco.shax.otel;

import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.*;
import static java.net.InetAddress.getLocalHost;

public class OtAttributes {

  public static final String OtPrefix = "OT_";
  public static final Map<String, String> otIdx = new LinkedHashMap<>();

  static {
    otIdx.putAll(getJvmAttributes());
    otIdx.putAll(getEnvAttributes());
  }

  public static Map<String, String> getEnvAttributes() {
    var envIdx = new LinkedHashMap<String, String>();
    getenv().forEach((k, v) -> {
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

  public static Map<String, String> getJvmAttributes() {
    var jvmIdx = new LinkedHashMap<String, String>();

    try {
      var hostname = getLocalHost().getHostName();
      jvmIdx.put("host.name", hostname);
    } catch (UnknownHostException e) {
      jvmIdx.put("host.name", "unknown");
    }

    jvmIdx.put("os.type", getProperty("os.name"));
    jvmIdx.put("os.description", getProperty("os.name") + " " + getProperty("os.version"));
    jvmIdx.put("os.version", getProperty("os.version"));
    jvmIdx.put("os.arch", getProperty("os.arch"));

    var runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    var runtimeName = runtimeMXBean.getName(); // Format: pid@hostname
    var pid = runtimeName.split("@")[0];
    jvmIdx.put("process.pid", pid);

    var javaCommand = getProperty("sun.java.command");
    if (javaCommand != null) {
      var commandParts = javaCommand.split(" ");
      jvmIdx.put("process.executable.name", commandParts[0]);
    } else {
      jvmIdx.put("process.executable.name", "unknown");
    }

    jvmIdx.put("process.runtime.name", getProperty("java.runtime.name"));
    jvmIdx.put("process.runtime.version", getProperty("java.runtime.version"));
    jvmIdx.put("process.runtime.description", getProperty("java.vm.name") + " " + getProperty("java.vm.version"));
    jvmIdx.put("process.runtime.vendor", getProperty("java.vm.vendor"));

    jvmIdx.put("telemetry.sdk.name", "opentelemetry");
    jvmIdx.put("telemetry.sdk.language", "java");

    return jvmIdx;
  }

}
