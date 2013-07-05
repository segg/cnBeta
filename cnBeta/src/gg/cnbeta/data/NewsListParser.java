package gg.cnbeta.data;

import java.util.ArrayList;
import java.util.List;


public class NewsListParser {

    private static final String DELIMITER_LINE = "<<>>";
		
	public static List<News> parse(String raw) {
	    if(raw == null) {
	        return null;
	    }
	    List<News> list = new ArrayList<News>();
	    String [] tokens = raw.split(DELIMITER_LINE);
	    for(String s : tokens) {
	        News n = News.fromString(s);
	        if(n != null) {
	            list.add(n);
	        }
	    }
	    return list;
	}
	
	public static String build(List<News> list) {
	    StringBuffer sb = new StringBuffer();
	    for (News n : list) {
	        sb.append(n.toString());
	        sb.append(DELIMITER_LINE);
	    }
	    return sb.toString();
	}
}
