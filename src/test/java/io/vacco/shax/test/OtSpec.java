package io.vacco.shax.test;

import io.vacco.shax.json.ShObjectWriter;
import io.vacco.shax.otel.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import java.util.HashMap;

import static io.vacco.shax.otel.Otel.*;
import static io.vacco.shax.otel.OtAttribute.att;
import static io.vacco.shax.otel.OtLogRecord.otLogRecord;
import static io.vacco.shax.otel.OtResourceLog.otResourceLog;
import static io.vacco.shax.otel.OtResourceSpan.otResourceSpan;
import static io.vacco.shax.otel.OtResource.otResource;
import static io.vacco.shax.otel.OtScopeLog.otScopeLog;
import static io.vacco.shax.otel.OtScopeSpan.otScopeSpan;
import static io.vacco.shax.otel.OtScope.otScope;
import static io.vacco.shax.otel.OtSpan.otSpan;
import static io.vacco.shax.otel.OtSpanKind.*;
import static io.vacco.shax.otel.OtStatus.otStatus;
import static io.vacco.shax.otel.OtBatch.otBatch;
import static io.vacco.shax.otel.OtValue.val;
import static j8spec.J8Spec.*;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtSpec {

  private static final ShObjectWriter or = new ShObjectWriter(true, true);

  static {
    it("Loads standard OTEL attributes", () -> {
      var otIdx = OtSys.otSysIdx;
      otIdx.forEach((k, v) -> {
        System.out.printf("%s -> %s%n", k, v);
      });
    });
    it("Transforms OTEL environment attributes", () -> {
      var env = new HashMap<String, String>();
      env.put("OT_DEPLOYMENT_ENVIRONMENT", "test");
      var otIdx = OtSys.envAttributesOf(env);
      assertTrue(otIdx.containsKey("deployment.environment"));
    });
    it("Creates log batches", () -> {
      var logBatch = otBatch()
        .resourceLog(
          otResourceLog(otResource().att(att("service.name", "my-service")))
            .scopeLog(
              otScopeLog(otScope("my-logger", "1.0.0"))
                .logRecord(
                  otLogRecord(nowNs(), 9, "Info")
                    .body(val("Application started successfully"))
                    .att(att("thread.id", 1))
                )
            )
        );
      System.out.println(or.apply(logBatch));
    });
    it("Creates trace batches", () -> {
      var traceBatch = otBatch()
        .resourceSpan(
          otResourceSpan(otResource().att(att("service.name", "iot-api-server")))
            .scopeSpan(
              otScopeSpan(otScope("com.example.iot", "1.0.0"))
                .span(
                  otSpan(traceId(), spanId(), "Receive audio data", SPAN_KIND_SERVER)
                    .parentSpanId(spanId())
                    .start(nowNs())
                    .end(nowNs())
                    .status(otStatus(OtStatusCode.STATUS_CODE_OK))
                    .att(att("device.id", "device-5678"))
                    .att(att("audio.length", 1024))
                )
            )
        );
      System.out.println(or.apply(traceBatch));
    });
  }
}
