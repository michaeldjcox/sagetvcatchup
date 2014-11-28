package uk.co.mdjcox.sagetv.catchup;

import sagex.api.FavoriteAPI;
import uk.co.mdjcox.utils.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

  public void addRecordingToSageTV(final String episodeId, String file, String programmeTitle, String episodeTitle, String description,
                                   List<String> categories, String origAirDate, String origAirTime, String airDate,
                                   String airTime, int seriesNumber, int episodeNumber, final String episodeIcon,
                                   final int durationInSeconds) throws RemoteException {

    try {
      if (sageRecordingDir == null) {
        sageUtils.error("No sageTV recording directory found");
        return;
      }

      File completedFile = new File(file);
      String safeProgrammeTitle = htmlUtils.makeIdSafe(programmeTitle);
      String safeEpisodeTitle = htmlUtils.makeIdSafe(episodeTitle);

      String recordingName = safeProgrammeTitle + "-" + safeEpisodeTitle+ ".mp4";
      File savedFile = new File(sageRecordingDir, recordingName);

      long timeout = System.currentTimeMillis() + 60000;

      do {
        try {
          Files.move(completedFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          break;
        } catch (Exception e) {
          sageUtils.warn("Failed to copy recording " + completedFile + " to " + savedFile, e);
          osUtils.waitFor(1000);
        }
      } while (completedFile.exists() && !savedFile.exists() && System.currentTimeMillis() < timeout);

      sageUtils.addRecordingToSageTV(
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
              durationInSeconds*1000);


      final String fileName = recordingName.replace(".mp4", ".jpg");
      Runnable runnable = new Runnable() {
        public void run() {
          try {
            File file = new File(sageRecordingDir, fileName);
            if (file.exists()) {
              file.delete();
            }
            downloadUtils.downloadFile(new URL(episodeIcon), file.getAbsolutePath());
          } catch (Exception e) {
            sageUtils.error("Failed to download episode fanart for " + episodeId, e);
          }
        }
      };

      if (episodeIcon != null) {
        downloadService.submit(runnable);
      }

    } catch (Exception e) {
      sageUtils.error("Failed to add catchup tv recording " + episodeId, e);
      throw new RemoteException("Catchup Plugin unable to add recording to SageTV", e);
    }
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
