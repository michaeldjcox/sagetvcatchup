package uk.co.mdjcox.sagetv.utils;

import org.slf4j.LoggerFactory;

/**
 * Created by michael on 20/10/14.
 */
public class Logger implements LoggerInterface {
  org.slf4j.Logger logger;

  public Logger(Class clazz) {
    logger = LoggerFactory.getLogger(clazz);
  }

  @Override
  public void info(String s) {
    logger.info(s);
  }

  @Override
  public void info(String s, Throwable e) {
    logger.info(s, e);
  }

  @Override
  public void warn(String s) {
    logger.warn(s);
  }

  @Override
  public void warn(String s, java.lang.Throwable throwable) {
    logger.warn(s, throwable);
  }

  @Override
  public void error(String s) {
    logger.error(s);
  }

  @Override
  public void error(String s, java.lang.Throwable throwable) {
    logger.error(s, throwable);
  }

  @Override
  public void debug(String s) {
    logger.debug(s);
  }

  @Override
  public void flush() {

  }
}
