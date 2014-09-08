package uk.co.mdjcox.sagetv.model;

import java.util.List;
import java.util.Set;

/**
 * Created by michael on 02/09/14.
 */
public interface ErrorRecorder {
  List<ParseError> getErrors();

  void addError(String level, String message);

  boolean hasErrors();

  String getSourceId();

  String getId();

  public Set<String> getMetaUrls();
}
