package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Episode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by michael on 23/10/14.
 */
public class EpisodeComparator implements Comparator<Episode> {

  private final SimpleDateFormat format;

  public EpisodeComparator() {
    format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    format.setTimeZone(TimeZone.getTimeZone("Europe/London"));

  }

  @Override
  public int compare(Episode o1, Episode o2) {

    try {
      String dateTime1 = o1.getOrigAirDate() + " " + o1.getOrigAirTime();
      Date date1 = format.parse(dateTime1);

      String dateTime2 = o2.getOrigAirDate() + " " + o2.getOrigAirTime();
      Date date2 = format.parse(dateTime2);

      int compare = date2.compareTo(date1);

      if (compare != 0) {
        return compare;
      }
    } catch (ParseException e) {

    }

    return o1.getEpisodeTitle().compareTo(o2.getEpisodeTitle());
  }
}

