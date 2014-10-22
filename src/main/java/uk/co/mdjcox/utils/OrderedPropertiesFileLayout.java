package uk.co.mdjcox.utils;

import java.util.*;

/**
 * Created by michael on 10/10/14.
 */
public class OrderedPropertiesFileLayout extends PropertiesFileLayout {
  private final String headComment;
  private final String tailComment;
  private List<String> order = new ArrayList<String>();

  public OrderedPropertiesFileLayout(List<String> order, String headComment, String tailComment) {
    this.order = order;
    this.headComment=headComment;
    this.tailComment=tailComment;
  }

  @Override
  public String getHeadComment() {
    return headComment;
  }

  @Override
  public String getTailComment() {
    return tailComment;
  }

  @Override
  public HashMap<String, String> getPrePropComments() {
    return new HashMap<String, String>();
  }

  @Override
  public HashMap<String, String> getPostPropComments() {
    return new HashMap<String, String>();
  }

  public Comparator getComparator(Properties props) {
    return new Comparator() {
      public int compare(Object o1, Object o2) {
        String s1 = (String)o1;
        String s2 = (String)o2;

        Integer order1 = order.indexOf(s1);
        Integer order2 = order.indexOf(s2);

        if (order1 == -1) {
          order1 = Math.abs(s1.hashCode());
        }
        if (order2 == -1) {
          order2 = Math.abs(s2.hashCode());
        }
        return order1.compareTo(order2);
      }
    };
  }
}
