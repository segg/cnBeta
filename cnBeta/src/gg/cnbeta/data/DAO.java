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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
		String firstArticleId = null;
		File f = new File(context.getCacheDir(), FILENAME_NEWS_LIST);
		if(f.exists())
			 firstArticleId = getFirstArticleId(f);
		String newsList = fetchRawNewsListFromUrl(NewsListActivity.URL_GAE_NEWS_LIST, firstArticleId);
		if(newsList == null)
			newsList = fetchRawNewsListFromUrl(NewsListActivity.URL_PROXY_NEWS_LIST, firstArticleId);
		// update local cache
		if(newsList != null && newsList.length() > 0)
			storeNewsList(context, newsList);	
		return newsList;
		
	}
	
	// Check with server using firstArticleId
	// Return news list if any update
	// Return "" if no change
	// Return null if any network problem
	private static String fetchRawNewsListFromUrl(String url, String firstArticleId)
	{
		String html = null;
		html = NetworkUtil.fetchHtml(url + firstArticleId, null);		
		if(html == null)
			return null;	// Network problem
		if(html.equals(firstArticleId))
			return "";	
		return html;
	}
	
	// Use first article id to check with GAE if any change to news list
	private static String getFirstArticleId(File f)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		char [] buf = new char[20];
		try {
			br.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String s = new String(buf);
		return s.split("<>")[0];
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
	private static String fetchNewsContentFromGAE(String id) {
		String content = NetworkUtil.fetchHtml(NewsListActivity.URL_GAE_NEWS_CONTENT + id, null);
		return content;
	}
	
	/* Return null if the news with the id does not exist */
	private static String fetchNewsContentFromCnbeta(String id) {
		String html = NetworkUtil.fetchHtml(NewsListActivity.URL_CNBETA + "/articles/" + id +".htm", null);
		if(html == null) return null;
		try {
			Document doc = Jsoup.parse(html);
			Element title = doc.getElementById("news_title");

			Element titleBar = doc.getElementsByClass("title_bar").get(0);
			Element date = titleBar.getElementsByClass("date").get(0);

			Element outterContent = doc.getElementsByClass("content").get(0);
			Element intro = outterContent.getElementsByClass("introduction").get(0);
			Element content = intro.nextElementSibling();

			Element brief = intro.getElementsByTag("p").get(0);

			// resize images
			content.select("[style]").removeAttr("style");
			content.select("[height]").removeAttr("height");
			content.select("[width]").removeAttr("width");
			content.select("img").attr("style", "width:100%;");
			content.select("iframe").attr("style", "width:100%;");

			StringBuffer sb = new StringBuffer();
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">");
			sb.append("<head>");
			sb.append("<meta name=\"HandheldFriendly\" content=\"true\" /><meta name=\"viewport\" content=\"width=device-width, height=device-height, user-scalable=no\" />");
			sb.append("</head>");
			sb.append("<body style=\"background:#fff;color:#595454;\">");
			title.attr("style", "color:#0099CC;font-size:120%;");
			sb.append(title.outerHtml());
			date.attr("style", "font-size:80%");
			sb.append(date.outerHtml());
			sb.append(brief.outerHtml());
			sb.append(content.outerHtml());
			sb.append("</body>");
			sb.append("</html>");
			return convertHtmlToText(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
}
