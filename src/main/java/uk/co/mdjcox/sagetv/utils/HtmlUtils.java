/**
 * HtmlUtils.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.util.ArrayList;


public class HtmlUtils implements HtmlUtilsInterface {
    private static HtmlUtilsInterface instance;

    public static HtmlUtilsInterface instance() {
        if (instance == null) {
            instance = new HtmlUtils();
        }
        return instance;
    }


    private class Parser extends HTMLEditorKit.ParserCallback {
        StringBuffer s;

        public void handleText(char[] text, int pos) {
            s.append(text);
        }

        private String parse(String html) {
            StringReader in = new StringReader(html);
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            // the third parameter is TRUE to ignore charset directive
            try {
                delegator.parse(in, this, Boolean.TRUE);
            } catch (IOException e) {
                s.append(html);
            }
            return s.toString();
        }
    }

    @Override
    public String removeHtml(String html) {
        if (html == null) return html;
//        html = html.replace('�', '-');
        html = html.replace('\n', ' ');
        html = html.replace('\r', ' ');
        html = html.trim();
      Parser parser = new Parser();
      String newhtml = parser.parse(html);
        if (newhtml == null || newhtml.isEmpty()) {
            return html;
        } else {
            html = newhtml;
        }
        newhtml = parser.parse(newhtml);
        if (newhtml == null || newhtml.isEmpty()) {
            return html;
        } else {
            html = newhtml;
        }
        html = html.trim();
        return html;
    }

    @Override
    public String makeLinkAbsolute(String base, String relative) {
        if (relative != null) {
            relative = relative.replace("https", "http");
            if (!relative.startsWith("http")) {
                if (relative.startsWith("/") || base.endsWith("/")) {
                    relative = base + relative;
                } else {
                    relative = base + "/" + relative;

                }
            }
        }
        return relative;
    }

    @Override
    public String makeIdSafe(String id) {
        String newId = "";
        for (char character : id.toCharArray()) {
          String charStr = "" + character;
            if (charStr.matches("[a-z,A-Z,0-9,-,_]")) {
                newId += character;
            }
        }
        return newId;
    }

    @Override
    public String makeContentSafe(String id) {
        id = id.replace(" & ", " and ");
        return id;
    }

    @Override
    public String extractTo(String token, String fileStr) {
        if (fileStr == null) {
            return null;
        }
        if (token == null) {
            return fileStr;
        }
        if (token.equals("")) {
            return fileStr;
        }
        int index = fileStr.indexOf(token);
        if (index == -1) {
            return null;
        } else {
            return fileStr.substring(0, index);
        }
    }

    @Override
    public boolean hasToken(String token, String fileStr) {
        if (fileStr == null) {
            return false;
        }
        if (token == null) {
            return false;
        }
        if (token.equals("")) {
            return false;
        }
        int index = fileStr.indexOf(token);
        if (index == -1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String moveTo(String token, String fileStr) {
        if (fileStr == null) {
            return null;
        }
        if (token == null) {
            return fileStr;
        }
        if (token.equals("")) {
            return fileStr;
        }
        int index = fileStr.indexOf(token);
        if (index == -1) {
            return null;
        } else {
            return fileStr.substring(index + token.length());
        }
    }

    @Override
    public String getFileString(String htmlfile) throws IOException {
        return getFileString(htmlfile, null);
    }

    @Override
    public String getFileString(String htmlfile, String filter) throws IOException {
        File file = new File(htmlfile);
        if (!file.exists()) {
            throw new FileNotFoundException(file + " cannot be found");
        }
        BufferedReader in = new BufferedReader(new FileReader(file));
        if (!in.ready()) {
            throw new IOException();
        }
        String fileContent = "";
        String line = "";
        while ((line = in.readLine()) != null) {
            if (filter != null) {
                if (line.indexOf(filter) < 0) {
                    continue;
                }
            }
            fileContent = fileContent + line;

        }
        in.close();
        return fileContent;
    }

    @Override
    public String moveToInSteps(ArrayList<String> tokens, String fileStr) {
        try {
            for (String token : tokens) {
                fileStr = moveTo(token, fileStr);
                if (fileStr == null) {
                    return null;
                }
            }
        } catch (Exception e) {
        }
        return fileStr;
    }

    @Override
    public ArrayList<String> extractItem(String fileStr, ArrayList<String> start, String stop, boolean removeHtml) {
        fileStr = moveToInSteps(start, fileStr);
        if (fileStr == null || fileStr.equals("")) {
            return null;
        }

        String result = extractTo(stop, fileStr);
        if (result == null) {
            return null;
        }
        if (removeHtml) {
            result = removeHtml(result);
        }
        fileStr = moveTo(stop, fileStr);
        if (fileStr == null || fileStr.equals("")) {
            return null;
        }

        ArrayList<String> list = new ArrayList<String>();
        list.add(fileStr);
        list.add(result);
        return list;
    }
}
