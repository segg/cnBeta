package gg.feed;

import javax.persistence.Id;

public class NewsList {
	@Id String id;
	String hash;	// used to identify changes
	String newsList;
	
	public NewsList(){}
	public NewsList(String id, String hash, String newsList)
	{
		this.id = id;
		this.hash = hash;
		this.newsList = newsList;
	}
}
