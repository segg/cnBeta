package gg.feed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class DAO
{
	
	public static final String BASE_URL = "http://www.cnbeta.com";
	public static final String NEWS_LIST_ID = "!@##$";
	 
	static {
        ObjectifyService.register(News.class);
        ObjectifyService.register(NewsList.class);
    }

    public static NewsList getOrCreateNewsList()
    {
    	Objectify ofy = ObjectifyService.begin();
		NewsList nl = ofy.find(NewsList.class, NEWS_LIST_ID);
		if(nl == null)
		{
			nl = createNewsList(false);
			System.out.println("create news list");
			if(nl != null)
				ofy.put(nl);
		}
		else
			System.out.println("get news list from datastore");
		return nl;
    }
    
    public static boolean updateNewsList()
    {
    	Objectify ofy = ObjectifyService.begin();
		NewsList nl = createNewsList(true);
		System.out.println("update news list");
		if(nl != null)
		{
			ofy.put(nl);
			return true;
		}
		return false;
    }
    
    /* Set fetchContent true if need to crawl the contents for all the news */
	public static NewsList createNewsList(boolean fetchContent)
	{
		String html = fetchHtml(BASE_URL, "GB2312");
		if(html == null) return null;
		StringBuffer sb = new StringBuffer();
		Document doc = Jsoup.parse(html );
		Elements newsList = doc.getElementsByClass("newslist");		
		for(Element e : newsList)
		{	
			Element a = e.getElementsByClass("topic").first().child(0);
			String id = a.attr("href").replaceAll(".*/", "").replace(".htm", "");
			String title = a.child(0).html();
			String tokens[] = e.getElementsByClass("author").first().child(0).html().split(" ");
			String author = tokens[0].replace("发布于", "");
			String time = tokens[1]+" "+tokens[2];
			a = e.getElementsByClass("desc").first().child(0);
			String topicId = a.attr("href").replaceAll(".*/", "").replace(".htm", "");
			String pic = a.child(0).attr("src").replaceAll(".*/", "");
			String brief = a.siblingElements().get(1).html().replaceAll("<.*?>", "");
			News news = new News(id, time, author, title, brief, topicId, pic);
			sb.append(news.toString());
			sb.append("<<>>");
			if(fetchContent)	// crawl and store news
				loadOrFetchNewsContent(id);
		}
		String list = escape(sb.toString());
		NewsList nl = new NewsList(NEWS_LIST_ID, md5(list), list);
		return nl;
	}
	
	public static String md5(String input)
	{
		if(input == null)
			return null;
		byte[] bytesOfMessage = null;
		try {
			bytesOfMessage = input.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] digest = md.digest(bytesOfMessage);
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// zero pad to get full 32 chars
		while(hashtext.length() < 32 )
		  hashtext = "0"+hashtext;
		return hashtext;
	}
	
	public static String escape(String s)
	{
		return s.replace("&nbsp;", " ").replace("&quot;", "\"").replace("&amp;", "&").replace("&middot;", "·");		
	}
	
	private static String fetchHtml(String url, String encode)
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			URL u = new URL(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), encode));
			String line = null;
			
			while((line = br.readLine()) != null)
				sb.append(line);	
		}catch(IOException e){
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
	public static String loadOrFetchNewsContent(String id)
	{
		Objectify ofy = ObjectifyService.begin();
		News news = ofy.find(News.class, id);
		if(news == null)
		{
			String content = fetchNewsContent(id);
			if(content != null)
			{
				news = new News();
				news.setId(id);
				news.setContent(content);
				ofy.put(news);
			}
		}
		return news == null?null:news.getContent();
	}
	
	/* Return null if the news with the id does not exist */
	public static String fetchNewsContent(String id)
	{
		String html = fetchHtml(BASE_URL+"/articles/" + id +".htm", "GB2312");
		Document doc = Jsoup.parse(html);
		Element content = doc.getElementById("news_content");
		if(content == null)
			return null;
		// Remove useless content
		content.getElementById("sign").remove();
		content.getElementById("googleAd_afc").remove();
		content.getElementsByClass("digbox").first().remove();
		
		// resize images
		content.select("[style]").removeAttr("style");	
		content.select("[height]").removeAttr("height");
		content.select("[width]").removeAttr("width");
		content.select("img").attr("style","width:100%;");
		content.select("iframe").attr("style","width:100%;");
		
		// get title and author
		Element title = doc.getElementById("news_title");
		title.attr("style", "color:#3090C7;");
		Element author = doc.getElementById("news_author");
		StringBuffer sb = new StringBuffer();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append("<meta name=\"HandheldFriendly\" content=\"true\" /><meta name=\"viewport\" content=\"width=device-width, height=device-height, user-scalable=no\" />");
		sb.append("</head>");
		sb.append("<body style=\"background:#fff;color:#595454;\">");
		sb.append(title.outerHtml());
		sb.append("<p style=\"font-size:80%\">");
		sb.append(author.text().split("\\|")[0]);
		sb.append("</p>");
		sb.append(content.outerHtml());
		sb.append("</body>");
		sb.append("</html>");
		return escape(sb.toString());
	}
	
	public static void deleteDatabase()
	{
		Objectify ofy = ObjectifyService.begin();
		Iterable<Key<News>> allKeys = ofy.query(News.class).fetchKeys();
		ofy.delete(allKeys);
	}
}
