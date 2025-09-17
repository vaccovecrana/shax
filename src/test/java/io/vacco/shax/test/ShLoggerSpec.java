package io.vacco.shax.test;

import io.vacco.shax.logging.*;
import io.vacco.shax.otel.OtContext;
import io.vacco.shax.otel.schema.OtSpan;
import io.vacco.shax.otel.schema.OtSpanKind;
import io.vacco.shax.otel.schema.OtValue;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.*;
import java.awt.GraphicsEnvironment;

import static io.vacco.shax.json.ShMaps.*;
import static io.vacco.shax.logging.ShOption.*;
import static io.vacco.shax.logging.ShArgument.kv;
import static io.vacco.shax.otel.schema.OtAttribute.att;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ShLoggerSpec {

  static {
    if (!GraphicsEnvironment.isHeadless()) {
      setSysProp(OTEL_EXPORTER_OTLP_ENDPOINT, "http://172.16.2.70:4318");
      setSysProp(OTEL_EXPORTER_OTLP_HEADERS, "api-key=key,other-config-value=value");
      setSysProp(OTEL_EXPORTER_OTLP_TIMEOUT, "4000");
      OtContext.processResource.att(att("deployment.environment", OtValue.val("shax")));
    }
  }

  static {
    describe("SLF4J Binding", () -> {
      it("Can load configuration from the environment and system properties", () -> {
        setSysProp(IO_VACCO_SHAX_SHOWDATETIME, "true");
        setSysProp(IO_VACCO_SHAX_LOGLEVEL, "trace");
        setSysProp(IO_VACCO_SHAX_PRETTYPRINT, "true");
        setSysProp(IO_VACCO_SHAX_DEVMODE, "true");
        setLoggerSysProp("io.vacco.shax.test", ShLogLevel.TRACE);

        ShLogConfig c = ShLogConfig.load();
        System.out.println(c);
      });
      it("Can log JSON messages", () -> {
        var log = ShLogger.withTransformer(
            LoggerFactory.getLogger(ShLoggerSpec.class),
            r -> {
              r.put("@timestamp", r.get(ShField.utc.name()));
              r.put("@version", 1);
              r.remove(ShField.utc.name());
              r.remove(ShField.utc_ms.name());
              return r;
            }
        );

        var otherLog = LoggerFactory.getLogger("someOtherLogger");
        otherLog.error("This is an ERROR message from some other logger");

        var p = MyPojo.getInstance();
        var x = new IllegalStateException("oops");

        log.info("{}", kv("arrayWithNulls", new Integer[] {1, 2, null, 4, null, 5}));

        log.info("Let's see some cats and owners");

        var catOwners = map(
          e("Garfield", "Jon"),
          e("Arlene", "Jon"),
          e("Azrael", "Gargamel"),
          e("Chi", "Youhei")
        );

        log.info("Cats and Owners {}", kv("catOwners", catOwners));
        log.info("Boolean log {}", kv("boolVal", true));
        log.info("Integer log {}", kv("intVal", 42));
        log.info("Long    log {}", kv("longVal", 42L));
        log.info("Float   log {}", kv("floatVal", 2.0f));
        log.info("Double  log {}", kv("doubleVal", 3.0));

        if (log.isTraceEnabled()) {
          log.trace("This is a TRACE message");
          log.trace("This is a TRACE message with format: [{}]", 1);
          log.trace("This is a TRACE message with two arguments: [{}, {}]", 1, null);
          log.trace("This is a TRACE message with no data, no format");
          log.trace("This is a TRACE message with format and multiple arguments: [{}, {}, {}, {}]", 1, 2, 3, 4);
          log.trace("This is a TRACE message with error data", x);
        }

        if (log.isDebugEnabled()) {
          log.debug("This is a DEBUG message");
          log.debug("This is a DEBUG message with format: [{}]", 1);
          log.debug("This is a DEBUG message with two arguments: [{}, {}]", 1, 2);
          log.debug("This is a DEBUG message with no data, no format");
          log.debug("This is a DEBUG message with format and multiple arguments: [{}, {}, {}, {}]", 1, 2, 3, 4);
          log.debug("This is a DEBUG message with error data", x);
        }

        if (log.isInfoEnabled()) {
          log.info("This is an INFO message");
          log.info("This is an INFO message with format: [{}]", 1);
          log.info("This is an INFO message with two arguments: [{}, {}]", 1, 2);
          log.info("This is an INFO message with no object data, and no format.", kv("test", null));
          log.info("This is an INFO message with object data and format: {}", kv("test", p));
          log.info("This is an INFO message with format and multiple arguments: [{}, {}, {}, {}]", 1, 2, 3, 4);
          log.info("This is an INFO message with error data", x);
        }

        if (log.isWarnEnabled()) {
          log.warn("This is a WARN message");
          log.warn("This is a WARN message with format: [{}]", 1);
          log.warn("This is a WARN message with two arguments: [{}, {}]", 1, 2);
          log.warn("This is a WARN message with format and multiple arguments: [{}, {}, {}, {}]", 1, 2, 3, 4);
          log.warn("This is a WARN message with error data", x);
        }

        if (log.isErrorEnabled()) {
          log.error("This is an ERROR message");
          log.error("This is an ERROR message with format: [{}]", 1);
          log.error("This is an ERROR message with two arguments: [{}, {}]", 1, 2);
          log.error("This is an ERROR message with format and multiple arguments: [{}, {}, {}, {}]", 1, 2, 3, 4);
          log.error("This is an ERROR message with error data", x);
        }
      });
      it("Cannot override a log record transform once one has been defined",
          c -> c.expected(IllegalArgumentException.class),
          () -> ShLogger.withTransformer(LoggerFactory.getLogger(ShLoggerSpec.class), r -> r)
      );
    });

    describe("OTEL Logging", () -> {
      it("Creates OTEL log batches", () -> {
        Thread.sleep(8000);
        var otLog = LoggerFactory.getLogger("otel.logger");
        otLog.info("OTEL message 0 {}", 0);
        otLog.warn("OTEL message 1 {}", 1);
        otLog.info("OTEL message 2 {}", kv("number", 2));
        System.err.println("----------------------");
        Thread.sleep(8000);
        superComputeStuff();
        System.err.println("----------------------");
        Thread.sleep(8000);
        superComputeStuff();
        System.err.println("----------------------");
        otLog.info("OTEL message 3 {}", kv("number", 3));
        Thread.sleep(8000);
      });
    });
  }

  private static Integer superComputeStuff() {
    OtSpan<Integer> lol = OtContext.span(OtSpanKind.SPAN_KIND_INTERNAL, sp -> {
      var meaningOfUniverse = "MOMOMOMOMOMO";
      OtSpan<String> lol1 = OtContext.span(sp, OtSpanKind.SPAN_KIND_CLIENT, sp1 -> {
        Thread.sleep(2500);
        return sp1
            .att("TheSuperComputeResult", meaningOfUniverse)
            .ok(meaningOfUniverse);
      });
      return sp.ok(42);
    });
    return lol.result;
  }

}
