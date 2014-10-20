package uk.co.mdjcox.utils;

import org.slf4j.LoggerFactory;

/**
 * Created by michael on 20/10/14.
 */
public class Logger {
  org.slf4j.Logger logger;

  public Logger(Class clazz) {
    logger = LoggerFactory.getLogger(clazz);
  }

  public void info(String s) {
    logger.info(s);
  }

  public void info(String s, Throwable e) {
    logger.info(s, e);
  }

  public void warn(String s) {
    logger.warn(s);
  }

  public void warn(String s, java.lang.Throwable throwable) {
    logger.warn(s, throwable);
  }

  public void error(String s) {
    logger.error(s);
  }

  public void error(String s, java.lang.Throwable throwable) {
    logger.error(s, throwable);
  }

  public void flush() {

  }
}
