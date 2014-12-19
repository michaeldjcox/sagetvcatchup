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

  private final SimpleDateFormat format=new SimpleDateFormat("dd-MM-yyyy");
  private final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEddMMM");
  private final SimpleDateFormat monthFormatThisYear = new SimpleDateFormat("MMMM");
  private final SimpleDateFormat monthFormatLastYear = new SimpleDateFormat("MMMMyyyy");

  private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");


  public CategoryComparator() {
    format.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    dayOfWeekFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    monthFormatThisYear.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    monthFormatLastYear.setTimeZone(TimeZone.getTimeZone("Europe/London"));
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

        Date date1 = parseDateStr(dateTime1, false);
        Date date2 = parseDateStr(dateTime2, false);

//        System.err.println("SORT " + o1.getShortName() + " vs " + o2.getShortName() + " Comparing " + dateTime1 + " with " + dateTime2 + "=" + date2.compareTo(date1));
//        System.err.println("SORT " + o1.getShortName() + " vs " + o2.getShortName() + " Comparing " + date1 + " with " + date2 + "=" + date2.compareTo(date1));

        int compare = date2.compareTo(date1);

        if (compare != 0) {
          return compare;
        }
      }
    } catch (ParseException e) {
       // Ignore
    }

//    System.err.println("Defaulting to alpha sort on " + o1.getShortName() + " vs " + o2.getShortName());

    return o1.getShortName().compareTo(o2.getShortName());
  }

  private Date parseDateStr(String dateTime1, boolean future) throws ParseException {
    Date now = new Date();
    if (dateTime1.equals("00s")) {
      if (future) {
        return yearFormat.parse("2100");
      }
      return yearFormat.parse("2000");
    }
    if (dateTime1.matches("[0-9][0-9]s")) {
      if (future) {
        dateTime1 = "20" + dateTime1.replace("s", "");
      } else {
        dateTime1 = "19" + dateTime1.replace("s", "");
      }
      return yearFormat.parse(dateTime1);
    }
    try {
      Date date = format.parse(dateTime1);
      date.setYear(now.getYear());
      return date;
    } catch (ParseException e) {
    try {
      Date date = dayOfWeekFormat.parse(dateTime1);
      date.setYear(now.getYear());
      return date;
    } catch (ParseException e1) {
      try {
        Date date = monthFormatLastYear.parse(dateTime1);
        return date;
      } catch (ParseException e4) {
      try {
        Date date = monthFormatThisYear.parse(dateTime1);
        date.setYear(now.getYear());
        return date;
      } catch (ParseException e2) {
        try {
          Date date = yearFormat.parse(dateTime1);
          return date;
        } catch (ParseException e3) {

        }
      }
      }
    }
    }
    return new Date();
  }
}

