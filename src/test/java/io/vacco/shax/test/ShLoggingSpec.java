package io.vacco.shax.test;

import io.vacco.shax.logging.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vacco.shax.logging.ShArgument.kv;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class ShLoggingSpec {
  static {
    describe("SLF4J Binding", () -> {
      it("Can load configuration from the environment and system properties", () -> {
        System.setProperty(ShOption.IO_VACCO_SHAX_SHOW_DATE_TIME.asSysProp(), "true");
        System.setProperty(ShOption.IO_VACCO_SHAX_LOG_LEVEL.asSysProp(), "trace");
        System.setProperty(ShOption.IO_VACCO_SHAX_PRETTY_PRINT.asSysProp(), "true");

        String logNameProp = String.format("%s.%s", ShOption.IO_VACCO_SHAX_LOGGER.asSysProp(), "io.vacco.shax.test");
        System.setProperty(logNameProp, ShLogLevel.TRACE.name());

        ShLogConfig c = ShLogConfig.load();
        System.out.println(c);
      });
      it("Can log JSON messages", () -> {
        Logger log = LoggerFactory.getLogger(ShLoggingSpec.class);
        if (log.isDebugEnabled()) {

          MyPojo p = MyPojo.getInstance();
          Exception x = new IllegalStateException("oops");

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
        }
      });
    });
  }

}
