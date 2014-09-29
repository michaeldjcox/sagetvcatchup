package Test

String url = recording.getUrl();

String pid = REPLACE_LINK_PREFIX(url, "http://www.bbc.co.uk/iplayer/episode/", "");
pid = REPLACE_LINK_TARGET(pid, "");

// Kill -9 makes for an unresumable download
KILL_MATCHING(".*iplayer.*" + pid + ".*");
