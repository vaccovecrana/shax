package org.slf4j.impl;

import io.vacco.shax.logging.ShLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {

  private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

  public static String REQUESTED_API_VERSION = "1.6.99"; // !final

  public static StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }

  private final ILoggerFactory loggerFactory;

  private StaticLoggerBinder() {
    loggerFactory = new ShLoggerFactory();
  }

  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  public String getLoggerFactoryClassStr() {
    return ShLoggerFactory.class.getName();
  }
}
