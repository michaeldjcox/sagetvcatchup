package uk.co.mdjcox.sagetv.utils;

import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by michael on 03/11/14.
 */
public class NumberedThreadFactory implements ThreadFactory {
  private int number = 0;
  private String name = "";

  public NumberedThreadFactory(String name) {
    checkNotNull(name);
    this.name = name;
  }

  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, name + "-" + (number++));
  }
}
