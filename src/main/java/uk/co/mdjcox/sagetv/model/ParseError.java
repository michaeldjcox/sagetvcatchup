package uk.co.mdjcox.sagetv.model;

/**
 * Class representing a parsing error
 */
public class ParseError implements Comparable<ParseError> {
  private final String level;
  private final String plugin;
  private final String programme;
  private final String episode;
  private final String message;
  private final String[] sourceUrl;

  /**
   * Constructs a parse error
   * @param level a severity level for the error
   * @param plugin the plugin name e.g. iplayer
   * @param programme the programme name affected
   * @param episode the episode name affected
   * @param message a message indicating the nature of the failure
   * @param sourceUrl the source URL from which the information could not be parsed
   */
  public ParseError(String level, String plugin, String programme, String episode, String message, String... sourceUrl) {
    this.level = level;
    this.plugin = plugin;
    this.programme = programme;
    this.episode = episode;
    this.message = message;
    this.sourceUrl = sourceUrl;
  }

  public String getPlugin() {
    return plugin;
  }

  public String getLevel() {
    return level;
  }

  public String getProgramme() {
    return programme;
  }

  public String getEpisode() {
    return episode;
  }

  public String[] getSourceUrl() {
    return sourceUrl;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ParseError that = (ParseError) o;

    if (!episode.equals(that.episode)) return false;
    if (!level.equals(that.level)) return false;
    if (!message.equals(that.message)) return false;
    if (!plugin.equals(that.plugin)) return false;
    if (!programme.equals(that.programme)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = level.hashCode();
    result = 31 * result + plugin.hashCode();
    result = 31 * result + programme.hashCode();
    result = 31 * result + episode.hashCode();
    result = 31 * result + message.hashCode();
    return result;
  }

  @Override
  public int compareTo(ParseError o) {
    int result = getPlugin().compareTo(o.getPlugin());
    if (result != 0) return result;
    result = getLevel().compareTo(o.getLevel());
    if (result != 0) return result;
    result = getProgramme().compareTo(o.getProgramme());
    if (result != 0) return result;
    result = getEpisode().compareTo(o.getEpisode());
    if (result != 0) return result;
    result = getMessage().compareTo(o.getMessage());
    return result;
  }
}
