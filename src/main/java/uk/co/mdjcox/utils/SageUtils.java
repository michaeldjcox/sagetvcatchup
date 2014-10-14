package uk.co.mdjcox.utils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import sagex.api.*;
import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.Recording;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Created by michael on 23/09/14.
 */
public class SageUtils implements SageUtilsInterface {

    protected Logger logger;

    private static SageUtilsInterface instance;

    public static SageUtilsInterface instance(final Logger logger) {
        if (instance == null) {
            instance = new SageUtils(logger);
        }
        return instance;
    }

    private SageUtils(Logger logger) {
        this.logger = logger;
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

    /*
 * This is needed because when a SageClient running Windows creates a path it always assumes the server
 * is running Windows and uses the wrong separator character.
 */
    private String fixPath(String Path) {

        // Nothing to do if we're running the plugin on a Windows machine.
        if (Global.IsWindowsOS()) {
            return Path;
        }

        // Replace all of the Windows separator characters with linux separator characters.
        String NewPath = Path.replaceAll("\\\\", File.separator);

        logger.info("RecordingEpisode.fixPath: " + Path + "->" + NewPath);

        return NewPath;
    }

    @Override
    public File[] getRecordingDirectories() {
        return Configuration.GetVideoDirectories();
    }


    @Override
    public void setClientProperty(String property, String value) {
        Configuration.SetProperty(property, value);

    }

    /**
     * Imports recording into the Sage database as an Airing.
     * <p>
     * @return The Airing if success, null otherwise.
     */
    public Object addAiringToSageTV(Recording recording) {

        Preconditions.checkNotNull(recording);

        File recordingFile = recording.getSavedFile();

        if (!recordingFile.exists()) {
            logger.error("RecordingEpisode.importAsMediaFile: Error. MovedFile does not exist.");
        }

        // Add the MediaFile to the database.
        Object mediaFile = MediaFileAPI.AddMediaFile(recordingFile, "");
        if (mediaFile == null) {
            logger.error("RecordingEpisode.importAsMediaFile: AddMediaFile failed for " + recordingFile.getAbsolutePath());
            return null;
        }

        Episode episode = recording.getEpisode();

        String title = episode.getProgrammeTitle();
        String episodeTitle = episode.getEpisodeTitle();
        String description = episode.getDescription();
        long duration = 1000; // TODO but sageTV seems to work it out anyway
        String[] category = episode.getGenres().toArray(new String[episode.getGenres().size()]);
        String peopleList[] = {};
        String rolesList[] = {};
        String rated = null;
        String expandedRatedList[] = null;
        String parentalRating = null; // TODO - this might be available
        String miscList[] = new String[0];
        Long now = Utility.Time();
        String nowString = now.toString();
        String externalID = "ONL" + nowString;
        String airingExternalID = "EP" + nowString;
        String language = "English";

        String origAirDate = episode.getOrigAirDate();
        String origAirTime = episode.getOrigAirTime();

      long originalAirDate = now;

      if ((origAirDate != null) && (origAirTime != null)) {
        try {
          String dateTime = origAirDate + " " + origAirTime;
          SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
          format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
          Date date = format.parse(dateTime);

          originalAirDate = date.getTime();

          logger.info("Uploading episode " + episode + " sageTV " + date + " from " + dateTime);

        } catch (ParseException e) {
          logger.warn("Failed to parse original air date " + origAirDate + " " + origAirTime + " into long time");
        }
      }

      SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
      yearFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
      String year = yearFormat.format(new Date(originalAirDate));

        boolean isFirstRun = true;


      String airDate = episode.getAirDate() ;
      String airTime = episode.getAirTime();

      if ((airDate != null) && (airTime != null)) {
        try {
          String dateTime = airDate + " " + airDate;
          SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
          format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
          Date date = format.parse(dateTime);

          isFirstRun = date.getTime() == originalAirDate;

        } catch (ParseException e) {
          logger.warn("Failed to parse air date " + airDate + " " + airTime + " into long time");
        }
      }

        int seriesNumber = 0;
        int episodeNumber = 0;
        if (!episode.getSeries().isEmpty()) {
            try {
                seriesNumber = Integer.parseInt(episode.getSeries());
            } catch (NumberFormatException e) {

            }
        }
        if (!episode.getEpisode().isEmpty()) {
            try {
                episodeNumber = Integer.parseInt(episode.getSeries());
            } catch (NumberFormatException e) {

            }
        }
        Object Show = ShowAPI.AddShow(
                title,
                isFirstRun,
                episodeTitle,
                description,
                duration,
                category,
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
            logger.error("RecordingEpisode.importAsMediaFile: AddShow failed.");
            return null;
        }

        if (!MediaFileAPI.SetMediaFileShow(mediaFile, Show)) {
            logger.error("RecordingEpisode.importAsMediaFile: SetMediaFileShow failed.");
            return null;
        }

        logger.info("RecordingEpisode.importAsMediaFile succeeded.");

        // Change the ExternalID metadata to something that starts with "EP" to turn the Imported video
        // file into an archived TV recording.
        MediaFileAPI.SetMediaFileMetadata(mediaFile, "ExternalID", airingExternalID);

        // Clear the Archived flag.
        MediaFileAPI.MoveTVFileOutOfLibrary(mediaFile);

        Object airing = MediaFileAPI.GetMediaFileAiring(mediaFile);

        if (!AiringAPI.IsAiringObject(airing)) {
            logger.error("RecordingEpisode.importAsMediaFile: Object is not an Airing.");
            return null;
        }

        return airing;
    }

}
