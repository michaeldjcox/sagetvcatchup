package uk.co.mdjcox.sagetv.catchup;

import sagex.api.FavoriteAPI;
import uk.co.mdjcox.sagetv.utils.*;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by michael on 24/10/14.
 */
public class CatchupPluginService extends UnicastRemoteObject implements CatchupPluginRemote {

  private final HtmlUtilsInterface htmlUtils;
  private final OsUtilsInterface osUtils;
  private final SageUtilsInterface sageUtils;
  private final DownloadUtilsInterface downloadUtils;
  private File sageRecordingDir;
  private ExecutorService downloadService = Executors.newSingleThreadExecutor();

  public CatchupPluginService(SageUtilsInterface sageUtils, OsUtilsInterface osUtils,
                              DownloadUtilsInterface downloadUtils, HtmlUtilsInterface htmlUtils) throws RemoteException {
    this.sageUtils = sageUtils;
    this.htmlUtils = htmlUtils;
    this.osUtils = osUtils;
    this.downloadUtils = downloadUtils;
    File[] dirs = sageUtils.getRecordingDirectories();

    if (dirs.length > 0) {
      sageRecordingDir = dirs[0];
    }
  }

  @Override
  public boolean available() throws RemoteException {
    return true;
  }

  public void addRecordingToSageTV(final String source, final String episodeId, final String file, final String programmeTitle, final String episodeTitle,
                                   final String description, final List<String> categories, final String origAirDate,
                                   final String origAirTime, final String airDate, final String airTime,
                                   final int seriesNumber, final int episodeNumber, final String episodeIcon,
                                   final int durationInSeconds, final String channel) throws RemoteException {

    long timeout = System.currentTimeMillis() + 60000;

    if (sageRecordingDir == null) {
      sageUtils.error("No sageTV recording directory found");
      return;
    }

    String safeProgrammeTitle = htmlUtils.makeIdSafe(programmeTitle);
    String safeEpisodeTitle = htmlUtils.makeIdSafe(episodeTitle);

    final String recordingName = safeProgrammeTitle + "-" + safeEpisodeTitle+ ".mp4";

    final File completedFile = new File(file);
    final File savedFile = new File(sageRecordingDir, recordingName);

    do {
      try {
        Files.move(completedFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        break;
      } catch (Exception e) {
        sageUtils.warn("Failed to copy recording " + completedFile + " to " + savedFile, e);
        osUtils.waitFor(1000);
      }
    } while (completedFile.exists() && !savedFile.exists() && System.currentTimeMillis() < timeout);

    Runnable runnable = new Runnable() {
      public void run() {
        try {


          final String imageFileName = recordingName.replace(".mp4", ".jpg");
          try {
            File imageFile = new File(sageRecordingDir, imageFileName);
            if (imageFile.exists()) {
              imageFile.delete();
            }
            downloadUtils.downloadFile(new URL(episodeIcon), imageFile.getAbsolutePath());
          } catch (Exception e) {
            sageUtils.error("Failed to download episode fanart for " + episodeId, e);
          }

          sageUtils.addRecordingToSageTV(
                  source,
                  savedFile.getAbsolutePath(),
                  programmeTitle,
                  episodeTitle,
                  description,
                  categories,
                  origAirDate,
                  origAirTime,
                  airDate,
                  airTime,
                  seriesNumber,
                  episodeNumber,
                  durationInSeconds*1000,
                  channel);
        } catch (Exception e) {
          sageUtils.error("Failed to add catchup tv recording " + episodeId, e);
        }

      }
    };

    downloadService.submit(runnable);
  }

  public void start() {
    downloadService = Executors.newSingleThreadScheduledExecutor();
  }

  public void stop() {
    try {
      downloadService.shutdownNow();
    } catch (Exception e) {
      // Ignore
    }
  }

  @Override
  public Set<String> getFavouriteTitles() throws RemoteException {
    HashSet<String> favouriteTitles = new HashSet<String>();
    Object[] faves = FavoriteAPI.GetFavorites();
    for (Object fave : faves) {
      try {
        String title = FavoriteAPI.GetFavoriteTitle(fave);
        if (title != null && !title.isEmpty()) {
          favouriteTitles.add(title.toUpperCase());
        }
      } catch (Exception e) {
        sageUtils.warn("Failed to get favourite title", e);
      }
    }
    return favouriteTitles;
  }
}
