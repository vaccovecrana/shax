package io.vacco.shax.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ShLoggerFactory implements ILoggerFactory {

  ConcurrentMap<String, Logger> loggerMap;

  public ShLoggerFactory() {
    loggerMap = new ConcurrentHashMap<>();
    ShLogger.lazyInit();
  }

  public Logger getLogger(String name) {
    Logger simpleLogger = loggerMap.get(name);
    if (simpleLogger != null) {
      return simpleLogger;
    } else {
      var newInstance = new ShLogger(name);
      var oldInstance = loggerMap.putIfAbsent(name, newInstance);
      return oldInstance == null ? newInstance : oldInstance;
    }
  }

}
