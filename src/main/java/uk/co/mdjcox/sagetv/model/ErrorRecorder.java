package uk.co.mdjcox.sagetv.model;

import java.util.List;

/**
 * Created by michael on 02/09/14.
 */
public interface ErrorRecorder {
  List<ParseError> getErrors();

  void addError(String level, String plugin, String programme, String episode, String message, String... sourceUrl);

  boolean hasErrors();
}
