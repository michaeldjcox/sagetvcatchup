package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Episode;
import uk.co.mdjcox.sagetv.model.SubCategory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by michael on 23/10/14.
 */
public class SubcategoryComparator implements Comparator<SubCategory> {

  private final SimpleDateFormat format;

  public SubcategoryComparator() {
    format = new SimpleDateFormat("dd-MM-yyyy");
    format.setTimeZone(TimeZone.getTimeZone("Europe/London"));

  }

  @Override
  public int compare(SubCategory o1, SubCategory o2) {

    try {
      String o1Parent = o1.getId() == null ? "" : o1.getId();
      String o2Parent = o2.getId() == null ? "" : o2.getId();

      if (o1Parent.contains("/AirDate/") && o2Parent.contains("/AirDate/")) {
        String dateTime1 = o1Parent.replaceFirst(".*/AirDate/", "");
        String dateTime2 = o2Parent.replaceFirst(".*/AirDate/", "");
        Date date1 = format.parse(dateTime1);
        Date date2 = format.parse(dateTime2);

        int compare = date2.compareTo(date1);

        if (compare != 0) {
          return compare;
        }
      }

    } catch (ParseException e) {

    }

    return o1.getShortName().compareTo(o2.getShortName());
  }
}

