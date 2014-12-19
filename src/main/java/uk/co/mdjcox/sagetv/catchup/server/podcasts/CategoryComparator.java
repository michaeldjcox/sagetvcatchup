package uk.co.mdjcox.sagetv.catchup.server.podcasts;

import uk.co.mdjcox.sagetv.model.Category;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by michael on 23/10/14.
 */
public class CategoryComparator implements Comparator<Category> {

  private static ArrayList<String> CHANNELS = new ArrayList<String>();

  static {
    CHANNELS.add("BBCOne");
    CHANNELS.add("BBCTwo");
    CHANNELS.add("BBCThree");
    CHANNELS.add("BBCFour");
    CHANNELS.add("BBC");
    CHANNELS.add("CBBC");
    CHANNELS.add("CBeebies");
    CHANNELS.add("BBCNews");
    CHANNELS.add("BBCNewsChannel");
    CHANNELS.add("BBCAlba");
    CHANNELS.add("S4C");
  }

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

      String o1Parent = o1.getId() == null ? "" : o1.getId();
      String o2Parent = o2.getId() == null ? "" : o2.getId();

    if (o1Parent.contains("/AirDate/") && o2Parent.contains("/AirDate/")) {
      try {
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
      } catch (ParseException e) {
        // Ignore
      }
    }

    if (o1Parent.contains("/Channel/") && o2Parent.contains("/Channel/")) {
      String chan1 = o1Parent.replaceFirst(".*/Channel/", "");
      String chan2 = o2Parent.replaceFirst(".*/Channel/", "");

      Integer index1 = getChannelIndex(chan1);
      Integer index2 = getChannelIndex(chan2);

      int compare = index1.compareTo(index2);

      if (compare != 0) {
        return compare;
      }

    }

//    System.err.println("Defaulting to alpha sort on " + o1.getShortName() + " vs " + o2.getShortName());

    return o1.getShortName().compareTo(o2.getShortName());
  }

  private Integer getChannelIndex(String channel) {
    int index = CHANNELS.indexOf(channel);
    if (index != -1) {
      return index;
    }
    return CHANNELS.size();
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

