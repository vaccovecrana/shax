package io.vacco.shax.test;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.OtContext;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.util.HashMap;

import static io.vacco.shax.otel.OtSchema.*;
import static io.vacco.shax.otel.OtUtil.*;
import static io.vacco.shax.otel.OtSchema.Attribute.att;
import static io.vacco.shax.otel.OtSchema.LogRecord.otLogRecord;
import static io.vacco.shax.otel.OtSchema.ResourceLog.otResourceLog;
import static io.vacco.shax.otel.OtSchema.ResourceSpan.otResourceSpan;
import static io.vacco.shax.otel.OtSchema.Resource.otResource;
import static io.vacco.shax.otel.OtSchema.ScopeLog.otScopeLog;
import static io.vacco.shax.otel.OtSchema.ScopeSpan.otScopeSpan;
import static io.vacco.shax.otel.OtSchema.Scope.otScope;
import static io.vacco.shax.otel.OtSchema.Span.otSpan;
import static io.vacco.shax.otel.OtSchema.SpanKind.*;
import static io.vacco.shax.otel.OtSchema.Batch.otBatch;
import static io.vacco.shax.otel.OtSchema.Value.val;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;


/* ==================================================================== */
/* TODO: Add a test loopback HTTP server that acts as a dummy OTEL sink */
/* ==================================================================== */

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtSpec {

  private static final ShObjectWriter ow = new ShObjectWriter(true, true);

  static {
    it("Loads standard OTEL attributes", () -> {
      OtContext.otSysIdx.forEach((k, v) -> System.out.printf("%s -> %s%n", k, v));
    });
    it("Transforms OTEL environment attributes", () -> {
      var env = new HashMap<String, String>();
      env.put("OT_DEPLOYMENT_ENVIRONMENT", "test");
      var otIdx = OtContext.envAttributesOf(env);
      assertTrue(otIdx.containsKey("deployment.environment"));
    });
    it("Creates log batches", () -> {
      var logBatch = otBatch()
        .resourceLog(
          otResourceLog(otResource().att(att(OtServiceName, val("my-service"))))
            .scopeLog(
              otScopeLog(otScope("my-logger", "1.0.0"))
                .logRecord(
                  otLogRecord(nowNs(), "SEVERITY_NUMBER_INFO", "INFO")
                    .body(val("Application started successfully"))
                    .att(att(OtThreadId, val(null, null, 1, null, null, null)))
                )
            )
        );
      System.out.println(ow.apply(logBatch));
    });
    it("Creates trace batches", () -> {
      var traceBatch = otBatch()
        .resourceSpan(
          otResourceSpan(otResource().att(att(OtServiceName, val("iot-api-server"))))
            .scopeSpan(
              otScopeSpan(otScope("com.example.iot", "1.0.0"))
                .span(
                  otSpan(traceId(), spanId(), "Receive audio data", SPAN_KIND_SERVER)
                    .parentSpanId(spanId())
                    .start(nowNs())
                    .end(nowNs())
                    .ok("MOMO")
                    .att(att("device.id", val("device-5678")))
                    .att(att("audio.length", val(null, null, 1024, null, null, null)))
                )
            )
        );
      System.out.println(ow.apply(traceBatch));
    });
  }
}
