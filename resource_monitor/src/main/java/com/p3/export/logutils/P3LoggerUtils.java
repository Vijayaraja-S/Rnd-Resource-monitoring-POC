package com.p3.export.logutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P3LoggerUtils {
  private static Logger logger;

  public P3LoggerUtils(Class<?> className) {
    logger = LoggerFactory.getLogger(className);
  }

  public void info(String msg) {
    logger.info(msg);
  }

  public void info(String format, Object... arguments) {
    logger.info(format, arguments);
  }

  public void error(String msg) {
    logger.error(msg);
  }

  public void debug(String msg) {
    logger.debug(msg);
  }

  public void error(String format, Object... arguments) {
    logger.error(format, arguments);
  }

  public void debug(String format, Object... arguments) {
    logger.debug(format, arguments);
  }
}
