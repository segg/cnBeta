package gg.cnbeta.data;
import gg.cnbeta.activity.NewsListActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;


public class NewsParser {
	
	private List<News> list;
	
	public NewsParser() {
		list = new ArrayList<News>();
	}
	
	public List<News> getNewsList() {
		return this.list;
	}
	
	public void clearList() {
		this.list.clear();
	}
	
	public void parse(String raw) {
		if(raw == null)
			return;
		String [] tokens = raw.split(News.DELIMITER_LINE);
		for(String s : tokens) {
			News n = News.createNews(s);
			if(n != null)
				list.add(n);
		}
	}
}
