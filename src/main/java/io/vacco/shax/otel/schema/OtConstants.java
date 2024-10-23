package io.vacco.shax.otel.schema;

public class OtConstants {

  public static final String
    Shax = "shax", Java = "java",

    OtHostName      = "host.name",
    OsName          = "os.name",
    OtOsType        = "os.type",
    OtOsDescription = "os.description",
    OtOsVersion     = "os.version",
    OtOsArch        = "os.arch",

    OtProcessPid                = "process.pid",
    OtProcessExecutableName     = "process.executable.name",
    OtProcessRuntimeName        = "process.runtime.name",
    OtProcessRuntimeVersion     = "process.runtime.version",
    OtProcessRuntimeDescription = "process.runtime.description",
    OtProcessRuntimeVendor      = "process.runtime.vendor",

    OtTelemetrySdkName      = "telemetry.sdk.name",
    OtTelemetrySdkVersion   = "telemetry.sdk.version",
    OtTelemetrySdkLanguage  = "telemetry.sdk.language";

  public static final String
    OtServiceName       = "service.name",
    OtServiceNamespace  = "service.namespace",
    OtServiceInstanceId = "service.instance.id";

  public static final String
    OtLoggerName          = "logger.name",
    OtThreadName          = "thread.name",
    OtThreadId            = "thread.id",
    OtExceptionMessage    = "exception.message",
    OtExceptionStacktrace = "exception.stacktrace",
    OtExceptionType       = "exception.type";

}
