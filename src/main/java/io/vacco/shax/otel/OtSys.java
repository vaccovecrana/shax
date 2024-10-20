package io.vacco.shax.otel;

import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.*;
import static java.net.InetAddress.getLocalHost;

public class OtSys {

  public static final String
    Ot = "opentelemetry", Java = "java",

    OtHostName = "host.name",
    OsName = "os.name",
    OtOsType = "os.type", OtOsDescription = "os.description",
    OtOsVersion = "os.version", OtOsArch = "os.arch",

    OtProcessPid = "process.pid", OtProcessExecutableName = "process.executable.name",
    OtProcessRuntimeName = "process.runtime.name",
    OtProcessRuntimeVersion = "process.runtime.version",
    OtProcessRuntimeDescription = "process.runtime.description",
    OtProcessRuntimeVendor = "process.runtime.vendor",

    OtTelemetrySdkName = "telemetry.sdk.name",
    OtTelemetrySdkLanguage = "telemetry.sdk.language"
  ;

  public static final String OtPrefix = "OT_";
  public static final Map<String, String> otSysIdx = new LinkedHashMap<>();

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
    var runtimeName = runtimeMXBean.getName(); // Format: pid@hostname
    var pid = runtimeName.split("@")[0];
    jvmIdx.put(OtProcessPid, pid);

    var javaCommand = getProperty("sun.java.command");
    if (javaCommand != null) {
      var commandParts = javaCommand.split(" ");
      jvmIdx.put(OtProcessExecutableName, commandParts[0]);
    } else {
      jvmIdx.put(OtProcessExecutableName, "unknown");
    }

    jvmIdx.put(OtProcessRuntimeName, getProperty("java.runtime.name"));
    jvmIdx.put(OtProcessRuntimeVersion, getProperty("java.runtime.version"));
    jvmIdx.put(OtProcessRuntimeDescription, getProperty("java.vm.name") + " " + getProperty("java.vm.version"));
    jvmIdx.put(OtProcessRuntimeVendor, getProperty("java.vm.vendor"));

    jvmIdx.put(OtTelemetrySdkName, Ot);
    jvmIdx.put(OtTelemetrySdkLanguage, Java);

    return jvmIdx;
  }

}
