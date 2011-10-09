package gg.cnbeta.data;

import gg.cnbeta.activity.NewsListActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DAO {
	
	public static final String FILENAME_NEWS_LIST = "newslist.txt";
	public static final String DIR_TOPICS = "topics";
	
	
	public static String convertHtmlToText(String s) {
		return s.replace("&nbsp;", " ").replace("&quot;", "\"").replace("&amp;", "&").replace("&middot;", "·");		
	}
	
	// Save rawNewsList
	public static void storeNewsList(Context context, String rawNewsList) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(new File(context.getCacheDir(), FILENAME_NEWS_LIST)));
			pw.println(rawNewsList);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Load rawNewsList from local cache
	// Return null if the file does not exist
	public static String loadRawNewsList(Context context) {
		File f = new File(context.getCacheDir(), FILENAME_NEWS_LIST);
		if(f.exists()) {
			StringBuffer sb = new StringBuffer();
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = null;
				while((line = br.readLine()) != null)
					sb.append(line);
				return sb.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		}
		return null;
	}
	
	// First try GAE
	// Then try cnbeta.com
	public static String fetchRawNewsList(Context context)
	{	
		String hash = md5(loadRawNewsList(context));
		String newsList = fetchRawNewsListFromGAE(hash);
		if(newsList == null)
			newsList = fetchRawNewsListFromCnbeta();
		// update local cache
		if(newsList != null && newsList.length() > 0)
			storeNewsList(context, newsList);	
		return newsList;
		
	}
	
	// Check with GAE server using the md5 hash of the local stored newslist
	// Return news list if any update
	// Return "" if no change
	// Return null if any network problem
	public static String fetchRawNewsListFromGAE(String hash)
	{
		String html = null;
		html = NetworkUtil.fetchHtml(NewsListActivity.URL_GAE_NEWS_LIST + hash, null);		
		if(html == null)
			return null;	// Network problem
		if(html.equals(hash))
			return "";	
		return html;
	}
	
	public static String fetchRawNewsListFromCnbeta() {
		String html = NetworkUtil.fetchHtml(NewsListActivity.URL_CNBETA, "GB2312");
		if(html == null) return null;
		StringBuffer sb = new StringBuffer();
		Document doc = Jsoup.parse(html );
		Elements newsList = doc.getElementsByClass("newslist");		
		for(Element e : newsList) {	
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
		}
		return convertHtmlToText(sb.toString());
	}
	
	// First try GAE
	// Then try cnbeta.com
	public static String fetchNewsContent(String id)
	{
		String content = fetchNewsContentFromGAE(id);
		if(content == null)
			content = fetchNewsContentFromCnbeta(id);
		return content;
	}
	public static String fetchNewsContentFromGAE(String id) {
		String content = NetworkUtil.fetchHtml(NewsListActivity.URL_GAE_NEWS_CONTENT + id, null);
		return content;
	}
	
	/* Return null if the news with the id does not exist */
	public static String fetchNewsContentFromCnbeta(String id) {
		String html = NetworkUtil.fetchHtml(NewsListActivity.URL_CNBETA + "/articles/" + id +".htm", "GB2312");
		if(html == null) return null;
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
		return convertHtmlToText(sb.toString());
	}
	
	public static Bitmap loadPic(Context context, String picId) {
		File f = new File(context.getCacheDir(), DIR_TOPICS + "/" + picId);
		Bitmap bm = null;
		if(f.exists()) {
			try {
				bm = BitmapFactory.decodeStream(new FileInputStream(f));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return bm;
	}
	
	public static void storePic(Context context, String picId, Bitmap bm) {
		File dir = new File(context.getCacheDir(), DIR_TOPICS);
		if(!dir.exists())
			dir.mkdirs();
		File f = new File(context.getCacheDir(), DIR_TOPICS + "/" + picId);
		try {
		       FileOutputStream out = new FileOutputStream(f);
		       bm.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		       e.printStackTrace();
		}
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
}
