/**
 * CommentedProperties.java
 * Author: Michael Cox
 * Date: 27-Jan-2009
 * Time: 18:09:14
 */


package uk.co.mdjcox.sagetv.utils;


import java.io.*;
import java.util.*;


/**
 * A singleton class which loads properties which configure the network encoder.
 */
public class CommentedProperties extends Properties {

    private String file;

    public CommentedProperties(String file, boolean throwError) throws Exception {
        this.file = file;
        loadFile(file, throwError);
    }

    public CommentedProperties() {
    }

    public void refresh(boolean throwError) throws Exception {
       loadFile(file, throwError);
    }

    private void loadFile(String name, boolean throwError) throws Exception {
        FileReader reader = null;
        try {
            reader = new FileReader(name);
            load(reader);
        } catch (Exception e) {
            if (throwError) {
                throw e;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public final void commit(String fileName, PropertiesFileLayout layout) throws Exception {
        FileWriter writer = null;
        try {
            layout.setProperties(this);
            writer = new FileWriter(fileName);
            store(writer, layout );
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    class SortedEnumeration implements Enumeration {

        TreeSet<String> set;
        Iterator itr;

        public SortedEnumeration(Properties props, Comparator comp) {
            super();
            set = new TreeSet<String>(comp);
            for (Object ob : props.keySet()) {
                set.add(ob.toString());
            }
            itr = set.iterator();
        }


        public boolean hasMoreElements() {
            return itr.hasNext();
        }

        public Object nextElement() {
            return itr.next();
        }
    }

        private Enumeration<Object> storeKeys(Comparator comp) {
            return new CommentedProperties.SortedEnumeration(this, comp);
        }

    private void store(Writer writer, PropertiesFileLayout layout) throws IOException {
        store((writer instanceof BufferedWriter)?(BufferedWriter)writer
	                                         : new BufferedWriter(writer),
	       true, layout);
    }

    private void store(BufferedWriter bw, boolean escUnicode, PropertiesFileLayout layout)
        throws IOException
    {
        String headComments = layout.getHeadComment();
        String tailComments = layout.getTailComment();
        String otherComment = layout.getOtherComment();
        HashMap<String,String> prePropComments = layout.getPrePropComments();
        HashMap<String,String> postPropComments = layout.getPostPropComments();

        if (headComments != null) {
            writeComments(bw, headComments);
        }
//        bw.write("#" + new Date().toString());
//        bw.newLine();
	synchronized (this) {
            for (Enumeration e = storeKeys(layout.getComparator(this)); e.hasMoreElements();) {
                String key = (String)e.nextElement();
		String val = (String)get(key);
                if (prePropComments != null) {
                    String propKey = null;
                    String propComment = null;
                    for (Map.Entry<String,String> entry : prePropComments.entrySet()) {
                        String prop = entry.getKey();
                        String value = entry.getValue();
                        if (key.matches(prop) || key.equals(prop)) {
                            propKey = prop;
                            propComment = value;
                            break;
                        }
                    }

                    if (prePropComments.isEmpty() && otherComment != null) {
                        writeComments(bw, otherComment);
                        otherComment = null;
                    }

                    prePropComments.get(key);
                    if (propComment != null) {
                        prePropComments.remove(propKey);
                        writeComments(bw, propComment);

                    }
                }
                key = saveConvert(key, true, escUnicode);
		/* No need to escape embedded and trailing spaces for value, hence
		 * pass false to flag.
		 */
		val = saveConvert(val, false, escUnicode);
		bw.write(key + "=" + val);
                bw.newLine();
                if (postPropComments != null) {
                    String propKey = null;
                    String propComment = null;
                    for (Map.Entry<String,String> entry : postPropComments.entrySet()) {
                        String prop = entry.getKey();
                        String value = entry.getValue();
                        if (key.matches(prop)) {
                            propKey = prop;
                            propComment = value;
                            break;
                        }
                    }
                    if (propComment != null) {
                        // Allow them to repeat
//                        postPropComments.remove(propKey);
                        writeComments(bw, propComment);

                    }
                }

            }
	}
        if (tailComments != null) {
            writeComments(bw, tailComments);
        }
        bw.flush();
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash
     */
    private String saveConvert(String theString,
			       boolean escapeSpace,
			       boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuffer outBuffer = new StringBuffer(bufLen);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\'); outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch(aChar) {
		case ' ':
		    if (x == 0 || escapeSpace)
			outBuffer.append('\\');
		    outBuffer.append(' ');
		    break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                case '=': // Fall through
                case ':': // Fall through
                case '#': // Fall through
                case '!':
                    outBuffer.append('\\'); outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    private void writeComments(BufferedWriter bw, String comments)
        throws IOException {
//        bw.write("# ");
        int len = comments.length();
        int current = 0;
        int last = 0;
        char[] uu = new char[6];
        uu[0] = '\\';
        uu[1] = 'u';
        while (current < len) {
            char c = comments.charAt(current);
	    if (c > '\u00ff' || c == '\n' || c == '\r') {
	        if (last != current)
                    bw.write(comments.substring(last, current));
                if (c > '\u00ff') {
                    uu[2] = toHex((c >> 12) & 0xf);
                    uu[3] = toHex((c >>  8) & 0xf);
                    uu[4] = toHex((c >>  4) & 0xf);
                    uu[5] = toHex( c        & 0xf);
                    bw.write(new String(uu));
                } else {
                    bw.newLine();
                    if (c == '\r' &&
			current != len - 1 &&
			comments.charAt(current + 1) == '\n') {
                        current++;
                    }
                    if (current == len - 1 ||
                        (comments.charAt(current + 1) != '#' &&
			comments.charAt(current + 1) != '!'));
//                        bw.write("# ");
                }
                last = current + 1;
	    }
            current++;
	}
        if (last != current)
            bw.write(comments.substring(last, current));
        bw.newLine();
    }

    /**
     * Convert a nibble to a hex character
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble) {
	return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

}
