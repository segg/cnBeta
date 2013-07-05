package gg.cnbeta.data;

import java.util.ArrayList;
import java.util.List;


public class NewsListParser {
    private static final String DELIMITER_FIELD = "<>";
    private static final String DELIMITER_LINE = "<<>>";
		
	public static List<News> parse(String raw) {
	    if(raw == null) {
	        return null;
	    }
	    List<News> list = new ArrayList<News>();
	    String [] tokens = raw.split(DELIMITER_LINE);
	    for(String s : tokens) {
	        News n = parseNews(s);
	        if(n != null) {
	            list.add(n);
	        }
	    }
	    return list;
	}
	
	private static News parseNews(String s) {
        String [] tokens = s.split(DELIMITER_FIELD);
        if(tokens.length != 7) {
            return null;
        }
        return new News(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
    }
    
}
