package uk.co.mdjcox.sagetv.utils;

/**
 * Created by michael on 21/10/14.
 */
public interface LoggerInterface {
  void info(String s);

  void info(String s, Throwable e);

  void warn(String s);

  void warn(String s, Throwable throwable);

  void error(String s);

  void error(String s, Throwable throwable);

  void flush();

  void debug(String s);
}
