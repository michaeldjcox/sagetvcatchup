package uk.co.mdjcox.sagetv.catchup;

import sagex.api.FavoriteAPI;
import uk.co.mdjcox.utils.SageUtils;
import uk.co.mdjcox.utils.SageUtilsInterface;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by michael on 24/10/14.
 */
public class CatchupPluginService extends UnicastRemoteObject implements CatchupPluginRemote {

  private File sageRecordingDir;
  private SageUtilsInterface sageUtils;

  public CatchupPluginService(SageUtilsInterface sageUtils) throws RemoteException {
    this.sageUtils = sageUtils;
    File[] dirs = sageUtils.getRecordingDirectories();

    if (dirs.length > 0) {
      sageRecordingDir = dirs[0];
    }

  }

  @Override
  public boolean available() throws RemoteException {
    return true;
  }

  public void addRecordingToSageTV(String episodeId, String file, String programmeTitle, String episodeTitle, String description,
                                   List<String> categories, String origAirDate, String origAirTime, String airDate,
                                   String airTime, int seriesNumber, int episodeNumber) throws RemoteException {

    try {
      if (sageRecordingDir == null) {
        sageUtils.error("No sageTV recording directory found");
        return;
      }

      File completedFile = new File(file);
      File savedFile = new File(sageRecordingDir, episodeId + ".mp4");
      Files.move(completedFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

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
              episodeNumber);
    } catch (Exception e) {
      sageUtils.error("Failed to add catchup tv recording " + episodeId, e);
      throw new RemoteException("Catchup Plugin unable to add recording to SageTV", e);
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
