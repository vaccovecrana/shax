package io.vacco.shax;

import io.vacco.shax.logging.ShLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class ShaxServiceProvider implements SLF4JServiceProvider {

  public static String REQUESTED_API_VERSION = "2.0.99"; // !final

  private ILoggerFactory loggerFactory;
  private IMarkerFactory markerFactory;
  private MDCAdapter mdcAdapter;

  @Override public String getRequestedApiVersion() { return REQUESTED_API_VERSION; }
  @Override public IMarkerFactory getMarkerFactory() { return markerFactory; }
  @Override public ILoggerFactory getLoggerFactory() { return loggerFactory; }
  @Override public MDCAdapter getMDCAdapter() { return mdcAdapter; }

  @Override public void initialize() {
    loggerFactory = new ShLoggerFactory();
    markerFactory = new BasicMarkerFactory();
    mdcAdapter = new BasicMDCAdapter();
  }

}
