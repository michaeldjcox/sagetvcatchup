package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.SageUtils;
import uk.co.mdjcox.utils.SageUtilsInterface;

import java.io.File;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

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
      Files.move(completedFile.toPath(), savedFile.toPath());

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
      e.printStackTrace();
    }
  }
}
