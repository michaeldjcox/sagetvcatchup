package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.sagetv.utils.OrderedPropertiesFileLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michael on 10/10/14.
 */
public class CatchupPropertiesFileLayout extends OrderedPropertiesFileLayout {

  private static ArrayList<String> ORDER = new ArrayList<String>();

  static {
    ORDER.add("onlineVideoPropertiesDir");
    ORDER.add("onlineVideoPropsSuffix");
    ORDER.add("recordingDir");
    ORDER.add("partialSizeForStreamingTimeout");
    ORDER.add("partialSizeForStreaming");
    ORDER.add("partialFileNameConfirmationTimeout");
    ORDER.add("streamingTimeout");
    ORDER.add("recordingTimeout");
    ORDER.add("podcasterPort");
    ORDER.add("catchupPluginRmiPort");
    ORDER.add("catchupServerRmiPort");
    ORDER.add("refreshRateHours");
    ORDER.add("refreshStartHour");
    ORDER.add("refreshStartNowProgrammeThreshold");
    ORDER.add("Iplayer.skip");
    ORDER.add("Iplayer.programmes");
    ORDER.add("Iplayer.maxprogrammes");
    ORDER.add("Iplayer.showRoot");
    ORDER.add("Iplayer.command");
    ORDER.add("Channel4OD.skip");
    ORDER.add("Channel4OD.programmes");
    ORDER.add("Channel4OD.maxprogrammes");
    ORDER.add("Channel4OD.showRoot");
    ORDER.add("Channel4OD.command");
    ORDER.add("Demand5.skip");
    ORDER.add("Demand5.programmes");
    ORDER.add("Demand5.maxprogrammes");
    ORDER.add("Demand5.showRoot");
    ORDER.add("Demand5.command");
    ORDER.add("ITVPlayer.skip");
    ORDER.add("ITVPlayer.programmes");
    ORDER.add("ITVPlayer.maxprogrammes");
    ORDER.add("ITVPlayer.showRoot");
    ORDER.add("ITVPlayer.command");
    ORDER.add("Test.skip");
    ORDER.add("Test.programmes");
    ORDER.add("Test.maxprogrammes");
    ORDER.add("Test.showRoot");
    ORDER.add("Test.command");

  }

  private static String LINEFEED = System.getProperty("line.separator");

  private static String EMPTY = "#" + LINEFEED;
  private static String DIVIDER = "# ===================================";
  private static String EMPTY2 = LINEFEED + "#";
  private static String HEAD_COMMENT = DIVIDER + LINEFEED +"# SageTV Catchup Properties"+LINEFEED + DIVIDER;
  private static String TAIL_COMMENT = DIVIDER + LINEFEED + "# End of SageTV Catchup Properties"+LINEFEED + DIVIDER;

  public CatchupPropertiesFileLayout() {
    super(ORDER, HEAD_COMMENT, TAIL_COMMENT);
  }

  @Override
  public HashMap<String, String> getPrePropComments() {
    HashMap<String, String> prepropComments = new HashMap<String, String>();

    prepropComments.put("onlineVideoPropertiesDir", EMPTY+"# Directory used by sageTV for custom online video properties - use windows 8 char folder names"+EMPTY2);
    prepropComments.put("onlineVideoPropsSuffix", EMPTY+"# Suffix to add the the properties files to distinguish from others"+EMPTY2);
    prepropComments.put("recordingDir", EMPTY+"# Directory used by the plugin to temporaily place recordings - use windows 8 char folder names"+EMPTY2);
    prepropComments.put("partialSizeForStreamingTimeout", EMPTY + "# How long to wait for recording file to be big enough to stream (ms)" + EMPTY2);
    prepropComments.put("partialSizeForStreaming", EMPTY + "# How large the recording file needs to be to stream (bytes)" + EMPTY2);
    prepropComments.put("partialFileNameConfirmationTimeout", EMPTY + "# How long to wait the partial recording file to appear (ms)" + EMPTY2);
    prepropComments.put("streamingTimeout", EMPTY + "# If the content stops how long to wait before giving up streaming (ms)" + EMPTY2);
    prepropComments.put("recordingTimeout", EMPTY + "# If the content stops how long to wait before given up recording (ms)" + EMPTY2);
    prepropComments.put("podcasterPort", EMPTY+"# Port to use for the web service"+EMPTY2);
    prepropComments.put("catchupPluginRmiPort", EMPTY+"# Ports to use for sagetv plugin to catchup server comms"+EMPTY2);
    prepropComments.put("refreshRateHours", EMPTY+"# Catalog refresh period in hours"+EMPTY2);
    prepropComments.put("refreshStartHour", EMPTY+"# Which hour of day 0-23 to start the periodic refresh"+EMPTY2 );
    prepropComments.put("refreshStartNowProgrammeThreshold", EMPTY+"# Minimum number of programmes in startup catalog size required to stop instant catalog run"+EMPTY2);
    prepropComments.put("Iplayer.skip", EMPTY+"# BBC IPlayer"+EMPTY2 + LINEFEED +
            "# (for command: use windows 8 char folder names, <URL> where the iplayer url should go, and <DIR> for the recordingDir above)" + EMPTY2);
    prepropComments.put("Channel4OD.skip", EMPTY+"# Channel 4 OD"+EMPTY2);
    prepropComments.put("Demand5.skip", EMPTY+"# Channel 5 Demand5"+EMPTY2);
    prepropComments.put("ITVPlayer.skip", EMPTY+"# ITV Player"+EMPTY2);
    prepropComments.put("Test.skip", EMPTY+"# Plugin test source"+EMPTY2);

    return prepropComments;
  }
}
