package uk.co.mdjcox.utils;

import sagex.api.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Created by michael on 23/09/14.
 */
public class SageUtils implements SageUtilsInterface {

    private static SageUtilsInterface instance;

    public static SageUtilsInterface instance() {
        if (instance == null) {
            instance = new SageUtils();
        }
        return instance;
    }

    private SageUtils() {

    }

    @Override
    public String getSageTVProperty(String property, String defaultValue) throws Exception {
        return Configuration.GetServerProperty(property, defaultValue);
    }

    @Override
    public String[] findTitlesWithName(String regex) {
        Pattern pattern = Pattern.compile(regex);
        String[] titles = Database.SearchForTitlesRegex(pattern);
        return titles == null ? new String[0] : titles;
    }

    public Object[] findAiringsByText(String name) {
        Object[] results  = Database.SearchByText(name);
        return results== null ? new Object[0] : results;
    }

    public String printAiring(Object airing) {
        return AiringAPI.PrintAiringLong(airing);
    }

    @Override
    public Object findShowForAiring(Object airing) {
        if (airing != null) {
            return AiringAPI.GetShow(airing);
        } else {
            return "";
        }
    }

    @Override
    public String printShow(Object show) {
        if (show != null) {
            return ShowAPI.GetShowTitle(show);
        } else {
            return "";
        }
    }

    @Override
    public File[] getRecordingDirectories() {
        return Configuration.GetVideoDirectories();
    }


    @Override
    public void setClientProperty(String property, String value) {
        Configuration.SetProperty(property, value);

    }

  public void addRecordingToSageTV(String file, String programmeTitle, String episodeTitle, String description, List<String> categories, String origAirDate, String origAirTime, String airDate, String airTime, int seriesNumber, int episodeNumber) {
    File recordingFile = new File(file);

    if (!recordingFile.exists()) {
        debugLog("Add airing to SageTV - recording does not exist: " + recordingFile.getAbsolutePath());
      return;
    }

    // Add the MediaFile to the database.
    Object mediaFile = MediaFileAPI.AddMediaFile(recordingFile, "");
    if (mediaFile == null) {
        debugLog("Add airing to SageTV - add media file failed: " + recordingFile.getAbsolutePath());
        return;
    }


    long duration = 1000;
    String peopleList[] = {};
    String rolesList[] = {};
    String rated = null;
    String expandedRatedList[] = null;
    String parentalRating = null;
    String miscList[] = new String[0];
    Long now = Utility.Time();
    String nowString = now.toString();
    String externalID = "ONL" + nowString;
    String airingExternalID = "EP" + nowString;
    String language = "English";


    long originalAirDate = now;

    if ((origAirDate != null) && (origAirTime != null)) {
      try {
        String dateTime = origAirDate + " " + origAirTime;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date date = format.parse(dateTime);

        originalAirDate = date.getTime();

        debugLog("Add airing to SageTV - Uploading recording of " + episodeTitle + " to sageTV " + date + " from " + dateTime);

      } catch (ParseException e) {
        debugLog("Add airing to SageTV - Failed to parse original air date " + origAirDate + " " + origAirTime + " into long time");
      }
    }

    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    yearFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    String year = yearFormat.format(new Date(originalAirDate));

    boolean isFirstRun = true;


    if ((airDate != null) && (airTime != null)) {
      try {
        String dateTime = airDate + " " + airDate;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date date = format.parse(dateTime);

        isFirstRun = date.getTime() == originalAirDate;

      } catch (ParseException e) {
        debugLog("Add airing to SageTV - Failed to parse air date " + airDate + " " + airTime + " into long time");
      }
    }

    String[] categoryArray = new String[0];
    if (categories != null) {
      categoryArray = categories.toArray(new String[categories.size()]);
    }


    Object Show = ShowAPI.AddShow(
            programmeTitle,
            isFirstRun,
            episodeTitle,
            description,
            duration,
            categoryArray,
            peopleList,
            rolesList,
            rated,
            expandedRatedList,
            year,
            parentalRating,
            miscList,
            externalID,
            language,
            originalAirDate,
            seriesNumber,
            episodeNumber);

    if (Show == null) {
      debugLog("Add airing to SageTV - Failed to add show");
        return ;
    }

    if (!MediaFileAPI.SetMediaFileShow(mediaFile, Show)) {
      debugLog("Add airing to SageTV - Failed to set media show");
        return ;
    }

    // Change the ExternalID metadata to something that starts with "EP" to turn the Imported video
    // file into an archived TV recording.
    MediaFileAPI.SetMediaFileMetadata(mediaFile, "ExternalID", airingExternalID);

    // Clear the Archived flag.
    MediaFileAPI.MoveTVFileOutOfLibrary(mediaFile);

    Object airing = MediaFileAPI.GetMediaFileAiring(mediaFile);

    if (!AiringAPI.IsAiringObject(airing)) {
      debugLog("Add airing to SageTV - Object is not an Airing.");
        return ;
    }

    debugLog("Add airing to SageTV - Succeeded");

  }

  @Override
  public void info(String s) {
    debugLog("INFO: " + s);
  }

  @Override
  public void info(String s, Throwable throwable) {
    debug("INFO: " + s, throwable);
  }

  @Override
  public void warn(String s) {
    debugLog("WARN: " + s);
  }

  @Override
  public void warn(String s, Throwable throwable) {
    debug("WARN: " + s, throwable);
  }

  @Override
  public void error(String s) {
    debugLog("ERROR: " + s);
  }

  @Override
  public void error(String s, Throwable throwable) {
    debug("ERROR: " + s, throwable);
  }

  @Override
  public void flush() {

  }

  @Override
  public void debug(String s) {
    debugLog("DEBUG: " + s);
  }

  private void debugLog(String message) {
    try {
      Global.DebugLog(message);
    } catch (Exception e) {
      // Ignore
    }
  }

  private void debug(String message, Throwable ex) {
    try {
      Global.DebugLog(message);
      String exmessage = ex.getMessage();
      if (exmessage == null) {
        exmessage = ex.getClass().getSimpleName();
      }
      Global.DebugLog(exmessage);
      for (StackTraceElement el : ex.getStackTrace()) {
        Global.DebugLog(el.toString());
      }
    } catch (Exception e) {
      // Ignore
    }
  }

}
