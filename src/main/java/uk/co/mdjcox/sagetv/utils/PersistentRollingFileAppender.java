package uk.co.mdjcox.sagetv.utils;

import ch.qos.logback.core.rolling.RollingFileAppender;

import java.io.File;
import java.io.IOException;

/**
 * Created by michael on 14/10/14.
 */
public class PersistentRollingFileAppender extends RollingFileAppender {

  public static boolean stopped = false;
  @Override
  protected void writeOut(Object event) throws IOException {
    if (!stopped) {
      File file = new File(getFile());
      if (!file.exists()) {
        openFile(getFile());
      }
      super.writeOut(event);
    }
  }
}
