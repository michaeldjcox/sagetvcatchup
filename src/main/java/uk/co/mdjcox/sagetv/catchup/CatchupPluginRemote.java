package uk.co.mdjcox.sagetv.catchup;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Created by michael on 24/10/14.
 */
public interface CatchupPluginRemote extends Remote {

  boolean available() throws RemoteException;

  void addRecordingToSageTV(String source, String episodeId, String recordingFile, String programmeTitle, String episodeTitle, String description,
                            List<String> category, String origAirDate, String origAirTime, String airDate, String airTime,
                            int seriesNumber, int episodeNumber, String episodeIcon, int durationInSeconds, String channel) throws RemoteException;

  Set<String> getFavouriteTitles() throws RemoteException;

}
