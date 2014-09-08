package uk.co.mdjcox.sagetv.model;

import java.util.Set;

/**
 * Class representing a parsing error
 */
public class ParseError implements Comparable<ParseError> {
  private ErrorRecorder item;
  private final String level;
  private final String message;

  /**
   * Constructs a parse error
   * @param level a severity level for the error
   * @param message a message indicating the nature of the failure
   */
  public ParseError(ErrorRecorder item,  String level, String message) {
    this.item = item;
    this.level = level;
    this.message = message;
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

    if (!getId().equals(that.getId())) return false;
    if (!level.equals(that.level)) return false;
    if (!message.equals(that.message)) return false;
    if (!getSource().equals(that.getSource())) return false;
    if (!getType().equals(that.getType())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = level.hashCode();
    result = 31 * result + getSource().hashCode();
    result = 31 * result + getType().hashCode();
    result = 31 * result + getId().hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  @Override
  public int compareTo(ParseError o) {
    int result = getSource().compareTo(o.getSource());
    if (result != 0) return result;
    result = getLevel().compareTo(o.getLevel());
    if (result != 0) return result;
    result = getType().compareTo(o.getType());
    if (result != 0) return result;
    result = getId().compareTo(o.getId());
    if (result != 0) return result;
    result = getMessage().compareTo(o.getMessage());
    return result;
  }
}
