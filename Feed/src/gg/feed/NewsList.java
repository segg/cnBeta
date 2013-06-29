package gg.feed;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

@Cached
public class NewsList {
	@Id String id;
	String firstArticleId;	// used to identify changes
	String newsList;
	
	public NewsList(){}
	public NewsList(String id, String firstArticleId, String newsList)
	{
		this.id = id;
		this.firstArticleId = firstArticleId;
		this.newsList = newsList;
	}
}
