package uk.co.mdjcox.sagetv.catchup;

import uk.co.mdjcox.utils.OrderedPropertiesFileLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michael on 10/10/14.
 */
public class CatchupPropertiesFileLayout extends OrderedPropertiesFileLayout {

  private static ArrayList<String> ORDER = new ArrayList<String>();

  static {
    ORDER.add("STV");
    ORDER.add("onlineVideoPropsSuffix");
    ORDER.add("recordingDir");
    ORDER.add("podcasterPort");
    ORDER.add("refreshRateHours");
    ORDER.add("refreshStartHour");
    ORDER.add("refreshStartNowProgrammeThreshold");
    ORDER.add("Iplayer.scriptDir");
    ORDER.add("Iplayer.command");
    ORDER.add("Iplayer.skip");
    ORDER.add("Iplayer.programmes");
    ORDER.add("Iplayer.maxprogrammes");
    ORDER.add("Channel4OD.skip");
    ORDER.add("Demand5.skip");
    ORDER.add("ITVPlayer.skip");
    ORDER.add("Test.skip");
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
    prepropComments.put("STV", EMPTY+"# Sage TV"+EMPTY2);
    prepropComments.put("recordingDir", EMPTY+"# Directories used by the plugin"+EMPTY2);
    prepropComments.put("podcasterPort", EMPTY+"# Port to use for the web service"+EMPTY2);
    prepropComments.put("refreshRateHours", EMPTY+"# How often to catalog sources in hours"+EMPTY2);
    prepropComments.put("Iplayer.scriptDir", EMPTY+"# BBC IPlayer"+EMPTY2);
    prepropComments.put("Channel4OD.skip", EMPTY+"# Channel 4 OD"+EMPTY2);
    prepropComments.put("Demand5.skip", EMPTY+"# Channel 5 Demand5"+EMPTY2);
    prepropComments.put("ITVPlayer.skip", EMPTY+"# ITV Player"+EMPTY2);
    prepropComments.put("Test.skip", EMPTY+"# Plugin test source"+EMPTY2);

    return prepropComments;
  }
}
