package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.PropertiesInterface;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by michael on 13/10/14.
 */
public interface CatchupContextInterface {
  String getDefaultCatalogFileName();

  String getCatalogFileName();

  int getRefreshRate();

  int getPort();

  String getPodcastBase();

  String getPluginDir();

  String getCssDir();

  String getConfigDir();

  String getXsltDir();

  String getLogDir();

  String getRecordingDir();

  String getOnlineVideoPropertiesSuffix();

  String getOnlineVideoPropertiesDir();

  ArrayList<String> getTestProgrammes(String pluginName);

  int getMaxProgrammes(String pluginName);

  PropertiesInterface getProperties();

  boolean skipPlugin(String sourceId);

  File getSageTVPluginsDevFile();

  String getSageTVPluginsURL();

  String toString();
}
