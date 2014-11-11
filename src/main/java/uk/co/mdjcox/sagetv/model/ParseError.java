package uk.co.mdjcox.sagetv.model;

import java.util.Set;

/**
 * Class representing a parsing error
 */
public class ParseError implements Comparable<ParseError> {
  private transient ErrorRecorder item;
  private String level;
  private String message;

  public ParseError() {
  }

  /**
   * Constructs a parse error
   * @param level a severity level for the error
   * @param message a message indicating the nature of the failure
   */
  public ParseError(String level, String message) {
    this.level = level;
    this.message = message;
  }

  public void setItem(ErrorRecorder item) {
    this.item = item;
  }

  public String getSource() {
    return item.getSourceId();
  }

  public String getLevel() {
    return level;
  }

  public String getType() {
    return item.getClass().getSimpleName();
  }

  public String getId() {
    return item.getId();
  }

  public Set<String> getSourceUrl() {
    return item.getMetaUrls();
  }

  public String getMessage() {
    return message;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParseError that = (ParseError) o;

    if (item != null ? !item.equals(that.item) : that.item != null) return false;
    if (level != null ? !level.equals(that.level) : that.level != null) return false;
    if (message != null ? !message.equals(that.message) : that.message != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = item != null ? item.hashCode() : 0;
    result = 31 * result + (level != null ? level.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

  @Override
  public int compareTo(ParseError o) {
    int result = getLevel().compareTo(o.getLevel());
    if (result != 0) return result;
    result = getMessage().compareTo(o.getMessage());
    return result;
  }

  @Override
  public String toString() {
    return level + "|" + message;
  }
}
