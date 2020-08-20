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

        System.setProperty(ShOption.IO_VACCO_SHAX_LOG_LEVEL.asSysProp(), "info");
        System.setProperty(ShOption.IO_VACCO_SHAX_PRETTY_PRINT.asSysProp(), "false");
        String logNameProp = String.format("%s.%s", ShOption.IO_VACCO_SHAX_LOGGER.asSysProp(), "io.vacco.shax.test");
        System.setProperty(logNameProp, ShLogLevel.DEBUG.name());

        ShLogConfig c = ShLogConfig.load();
        System.out.println(c);
      });
      it("Can log JSON messages", () -> {
        Logger log = LoggerFactory.getLogger(ShLoggingSpec.class);
        if (log.isDebugEnabled()) {
          MyPojo p = MyPojo.getInstance();
          log.debug("Hello, this is some debugging code: [{}]", 1);
          log.info("And this is an INFO message with object data, and no markers.", kv("test", p));
          log.info("And this is an INFO message with object data and one marker: {}", kv("test", p));
        }
      });
    });
  }

}
