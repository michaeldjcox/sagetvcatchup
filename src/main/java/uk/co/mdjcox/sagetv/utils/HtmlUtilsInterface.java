package uk.co.mdjcox.sagetv.utils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 02/04/13
 * Time: 07:51
 * To change this template use File | Settings | File Templates.
 */
public interface HtmlUtilsInterface {
    String removeHtml(String html);

    String makeLinkAbsolute(String base, String relative);

    String makeIdSafe(String id);

    String makeContentSafe(String id);

    String extractTo(String token, String fileStr);

    boolean hasToken(String token, String fileStr);

    String moveTo(String token, String fileStr);

    String getFileString(String htmlfile) throws IOException;

    String getFileString(String htmlfile, String filter) throws IOException;

    String moveToInSteps(ArrayList<String> tokens, String fileStr);

    ArrayList<String> extractItem(String fileStr, ArrayList<String> start, String stop, boolean removeHtml);
}
