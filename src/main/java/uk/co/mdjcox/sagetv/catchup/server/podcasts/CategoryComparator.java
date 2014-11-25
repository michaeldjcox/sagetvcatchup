package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Category;
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
public class CategoryComparator implements Comparator<Category> {

  private final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEdMMM");
  private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
  private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");


  public CategoryComparator() {
    dayOfWeekFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    monthFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    yearFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
  }

  @Override
  public int compare(Category o1, Category o2) {

    try {
      String o1Parent = o1.getId() == null ? "" : o1.getId();
      String o2Parent = o2.getId() == null ? "" : o2.getId();

      if (o1Parent.contains("/AirDate/") && o2Parent.contains("/AirDate/")) {
        String dateTime1 = o1Parent.replaceFirst(".*/AirDate/", "");
        String dateTime2 = o2Parent.replaceFirst(".*/AirDate/", "");

        Date date1 = parseDateStr(dateTime1);
        Date date2 = parseDateStr(dateTime2);

        int compare = date2.compareTo(date1);

        if (compare != 0) {
          return compare;
        }
      }

    } catch (ParseException e) {

    }

    return o1.getShortName().compareTo(o2.getShortName());
  }

  private Date parseDateStr(String dateTime1) throws ParseException {
    try {
      Date date = dayOfWeekFormat.parse(dateTime1);
      return date;
    } catch (ParseException e) {
      try {
        Date date = monthFormat.parse(dateTime1);
        return date;
      } catch (ParseException e1) {
        try {
          Date date = yearFormat.parse(dateTime1);
          return date;
        } catch (ParseException e2) {
          if (dateTime1.equals("00s")) {
            return yearFormat.parse("2000");
          }
          if (dateTime1.matches("[0-9][0-9]s")) {
            dateTime1 = "19" + dateTime1.replace("s", "");
            return yearFormat.parse(dateTime1);
          }
        }
      }

    }
    return new Date();
  }
}

