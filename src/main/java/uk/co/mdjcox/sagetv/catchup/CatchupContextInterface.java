package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.utils.PropertiesInterface;

import java.util.ArrayList;

/**
 * Created by michael on 13/10/14.
 */
public interface CatchupContextInterface {
    String getDefaultCatalogFileName();

    String getCatalogFileName();

    int getRefreshRate();

    int getRefreshStartHour();

    int getPort();

    String getPodcastBase();

    String getPluginDir();

    String getCssDir();

    String getConfigDir();

    String getTmpDir();

    String getXsltDir();

    String getLogDir();

    String getRecordingDir();

    String getOnlineVideoPropertiesSuffix();

    String getOnlineVideoPropertiesDir();

    ArrayList<String> getTestProgrammes(String pluginName);

    int getMaxProgrammes(String pluginName);

  PropertiesInterface getProperties();

    boolean skipPlugin(String sourceId);

    String toString();

    void setPort(int port);

    void setRecordingDir(String recordDir);

    int getRefreshStartNowProgrammeThreshold();

    int getCatchupServerRmiPort();

    int getCatchupPluginRmiPort();

    void setProperty(String name, String value);

  long getPartialSizeForStreamingTimeout();

  long getPartialSizeForStreaming();

  long getPartialFileNameConfirmationTimeout();

  long getStreamingTimeout();

  long getRecordingTimeout();

  String getImageDir();
}
