package io.vacco.shax.test;

import io.vacco.shax.logging.ShLogLevel;
import io.vacco.shax.logging.ShLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public final class ShLoggerBench {

  private static final int CHECKS = 100_000_000;
  private static final int WARMUP = 20_000_000;
  private static final int EMISSIONS = 1_000_000;

  private static volatile long blackhole;

  private ShLoggerBench() {}

  private static long checks(Logger a, Logger b, int iterations) {
    var hits = 0L;
    for (var i = 0; i < iterations; i++) {
      var log = (i & 1) == 0 ? a : b;
      if (log.isDebugEnabled()) {
        hits++;
      }
    }
    return hits;
  }

  private static void report(String label, Logger a, Logger b) {
    blackhole += checks(a, b, WARMUP);
    var start = System.nanoTime();
    var hits = checks(a, b, CHECKS);
    var elapsed = System.nanoTime() - start;
    blackhole += hits;
    System.out.printf(Locale.ROOT, "%-42s %8.2f ns/op  (enabled=%d)%n", label, elapsed / (double) CHECKS, hits);
  }

  private static void reportEmissions(int iterations) {
    var log = LoggerFactory.getLogger("bench.emit.Hot");
    for (var i = 0; i < iterations / 10; i++) {
      log.debug("warmup {}", i);
    }
    var start = System.nanoTime();
    for (var i = 0; i < iterations; i++) {
      log.debug("value {}", i);
    }
    var elapsed = System.nanoTime() - start;
    System.out.printf(Locale.ROOT, "%-42s %8.2f ns/op  (%d emitted)%n", "enabled debug emission (null stream)", elapsed / (double) iterations, iterations);
  }

  public static void main(String[] args) {
    ShLogger.setPrintStream(new PrintStream(OutputStream.nullOutputStream(), false));
    ShLogger.setRootLoggerLevel(ShLogLevel.INFO);

    ShLogger.setLoggerLevel("bench", ShLogLevel.DEBUG);
    ShLogger.setLoggerLevel("bench.deep.nested", ShLogLevel.TRACE);
    ShLogger.setLoggerLevel("other.a", ShLogLevel.WARN);
    ShLogger.setLoggerLevel("other.b", ShLogLevel.ERROR);
    ShLogger.setLoggerLevel("other.c.d", ShLogLevel.DEBUG);
    ShLogger.setLoggerLevel("bench.emit", ShLogLevel.DEBUG);

    System.out.printf(Locale.ROOT, "ShLoggerBench - JDK %s - %,d checks, %,d warmup, 6 overrides%n", System.getProperty("java.version"), CHECKS, WARMUP);
    System.out.printf(Locale.ROOT, "%-42s %8s%n", "case", "ns/op");

    report("hit at bench.* (2 probes)",
      LoggerFactory.getLogger("bench.Hot"),
      LoggerFactory.getLogger("bench.Other"));
    report("hit at bench.deep.nested.* (2 probes)",
      LoggerFactory.getLogger("bench.deep.nested.Hot"),
      LoggerFactory.getLogger("bench.deep.nested.Other"));
    report("hit at bench.deep.nested.more.* (3 probes)",
      LoggerFactory.getLogger("bench.deep.nested.more.Hot"),
      LoggerFactory.getLogger("bench.deep.nested.more.Other"));
    report("no override, root fallback (3 probes)",
      LoggerFactory.getLogger("unrelated.pkg.Hot"),
      LoggerFactory.getLogger("sample.other.Thing"));

    reportEmissions(EMISSIONS);
    System.out.println("blackhole=" + blackhole);
  }

}
