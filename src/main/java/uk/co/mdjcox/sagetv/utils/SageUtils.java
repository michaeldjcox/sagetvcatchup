package uk.co.mdjcox.sagetv.utils;

import sagex.api.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    public File[] getRecordingDirectories() {
        return Configuration.GetVideoDirectories();
    }


    @Override
    public void setClientProperty(String property, String value) {
        Configuration.SetProperty(property, value);

    }

  public void addRecordingToSageTV(String source, String file, String programmeTitle, String episodeTitle, String description,
                                   List<String> categories, String origAirDate, String origAirTime, String airDate,
                                   String airTime, int seriesNumber, int episodeNumber, int duration, String channel) {
    File recordingFile = new File(file);

    info("Adding recording to SageTV - " + file);

    if (!recordingFile.exists()) {
      error("Add recording to SageTV - recording file does not exist: " + recordingFile.getAbsolutePath());
      return;
    }

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
      } catch (ParseException e) {
        warn("Add recording to SageTV - Failed to parse original air date " + origAirDate + " " + origAirTime + " into long time");
      }
    }

    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    yearFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    String year = yearFormat.format(new Date(originalAirDate));

    boolean isFirstRun = true;

    long actualAirDate = originalAirDate;
    if ((airDate != null) && (airTime != null)) {
      try {
        String dateTime = airDate + " " + airTime;
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
        Date date = format.parse(dateTime);
        actualAirDate = date.getTime();

        isFirstRun = actualAirDate == originalAirDate;

      } catch (ParseException e) {
        warn("Add recording to SageTV - Failed to parse air date " + airDate + " " + airTime + " into long time");
      }
    }

    Object temp = MediaFileAPI.CreateTempMediaFile(recordingFile.getAbsolutePath());

    if (temp == null) {
      error("Add recording to SageTV - create temp media file failed: " + recordingFile.getAbsolutePath());
      return;
    }

    long fileDuration = MediaFileAPI.GetFileDuration(temp);

    recordingFile.setLastModified(actualAirDate + fileDuration);

    // Add the MediaFile to the database.
    Object mediaFile = MediaFileAPI.AddMediaFile(recordingFile, "");
    if (mediaFile == null) {
      error("Add recording to SageTV - add media file failed: " + recordingFile.getAbsolutePath());
      return;
    }

    long mediaStart = MediaFileAPI.GetFileStartTime(mediaFile);
    long mediaStop = MediaFileAPI.GetFileEndTime(mediaFile);
    long mediaObjDuration = MediaFileAPI.GetFileDuration(mediaFile);
    info("Media file  " + new Date(mediaStart) + " to " + new Date(mediaStop) + " duration " + mediaObjDuration);

    String[] categoryArray = new String[0];
    if (categories != null) {
      categoryArray = categories.toArray(new String[categories.size()]);
    }

    Object show = ShowAPI.AddShow(
            programmeTitle,
            isFirstRun,
            episodeTitle,
            description,
            fileDuration,
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

    if (show == null) {
      error("Add recording to SageTV - Failed to add show");
      return ;
    }

    if (!ShowAPI.IsShowObject(show)) {
      error("Add recording to SageTV failed - show object returned is a " + show.getClass().getSimpleName());
      return ;
    }

    info("Show        " + episodeTitle + " isFirstRun=" + isFirstRun + " orig=" + origAirDate + " " + origAirTime + " actual=" + airDate + " " + airTime);

    if (!MediaFileAPI.SetMediaFileShow(mediaFile, show)) {
      error("Add recording to SageTV - Failed to set media show");
      return ;
    }

    // Change the ExternalID metadata to something that starts with "EP" to turn the Imported video
    // file into an archived TV recording.
    MediaFileAPI.SetMediaFileMetadata(mediaFile, "ExternalID", airingExternalID);

    // Clear the Archived flag.
    MediaFileAPI.MoveTVFileOutOfLibrary(mediaFile);

    Object channelObject = findChannelForSourceAndChannel(source, channel);

    int stationId = -1;

    try {
      if (channelObject == null) {
        String callSign = makeIdSafe(channel);

        stationId = findUniqueStationId();

        if (stationId != -1) {
          info("Adding channel " + stationId + " " + callSign + " " + channel + " " + source);
          ChannelAPI.AddChannel(callSign, channel, source, stationId);
        }
      } else {
        stationId = ChannelAPI.GetStationID(channelObject);
      }
    } catch (Exception e) {
      // Ignore
    }

    Object airing = null;

    // If we have a station Id then re-add the airing and associate
    if (stationId != -1) {
      airing = AiringAPI.AddAiring(airingExternalID, stationId, actualAirDate, fileDuration);

      if (airing != null && AiringAPI.IsAiringObject(airing)) {
        MediaFileAPI.SetMediaFileAiring(mediaFile, airing);
//        AiringAPI.SetRecordingTimes(airing, actualAirDate, actualAirDate + fileDuration);
      }
  } else {
      airing = MediaFileAPI.GetMediaFileAiring(mediaFile);
    }

    if (airing == null) {
      error("Add recording to SageTV failed - no airing created");
      return ;
    }

    if (!AiringAPI.IsAiringObject(airing)) {
      error("Add recording to SageTV failed - airing object returned is a " + airing.getClass().getSimpleName());
      return ;
    }

    long airingStart = AiringAPI.GetAiringStartTime(airing);
    long airingStop = AiringAPI.GetAiringEndTime(airing);
    long airingObjDuration = AiringAPI.GetAiringDuration(airing);
    info("Airing      " + new Date(airingStart) + " to " + new Date(airingStop) + " duration " + airingObjDuration);


    info("Add recording to SageTV - Succeeded");
  }

  private int findUniqueStationId() throws Exception {
    for (int stationId = 9999; stationId>0; stationId--) {
      Object chan = ChannelAPI.GetChannelForStationID(stationId);
      if (chan == null) {
        info("Found free station id " + stationId);
        return stationId;
      }
    }
    return -1;
  }

  private String makeIdSafe(String id) {
    String newId = "";
    for (char character : id.toCharArray()) {
      String charStr = "" + character;
      if (charStr.matches("[a-z,A-Z,0-9,-,_]")) {
        newId += character;
      }
    }
    return newId;
  }

  public Object findChannelForSourceAndChannel(String source, String channel) {
    try {
      Object[] channels = ChannelAPI.GetAllChannels();
      for (Object chan : channels) {
        if (ChannelAPI.IsChannelObject(chan)) {
          String network = ChannelAPI.GetChannelNetwork(chan);
          if (network != null && network.equals(source)) {
            String name = ChannelAPI.GetChannelDescription(chan);
            if (channel.equals(name)) {
              return chan;
            }
          }
        }
      }
    } catch (Exception e) {
      error("Cannot find channel", e);
    }
    return null;
  }

  @Override
  public void info(String s) {
    debugLog("INFO: " + s);
  }

  @Override
  public void info(String s, Throwable throwable) {
    debugLog("INFO: " + s, throwable);
  }

  @Override
  public void warn(String s) {
    debugLog("WARN: " + s);
  }

  @Override
  public void warn(String s, Throwable throwable) {
    debugLog("WARN: " + s, throwable);
  }

  @Override
  public void error(String s) {
    debugLog("ERROR: " + s);
  }

  @Override
  public void error(String s, Throwable throwable) {
    debugLog("ERROR: " + s, throwable);
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
    } catch (Throwable e) {
      System.err.println(message);
      // Ignore
    }
  }

  private void debugLog(String message, Throwable ex) {
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
    } catch (Throwable e) {
      System.err.println(message);
      ex.printStackTrace();

      // Ignore
    }
  }

}
