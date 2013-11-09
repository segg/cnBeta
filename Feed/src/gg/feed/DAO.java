package gg.feed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

public class DAO {

  public static final String BASE_URL = "http://www.cnbeta.com";
  public static final String NEWS_LIST_ID = "NEWS_LIST_ID";
  public static final int DEFAULT_SIZE = 60;
  
  private static final Logger log = Logger.getLogger(FeedServlet.class
      .getName());

  static {
    ObjectifyService.register(News.class);
    ObjectifyService.register(NewsList.class);
  }

  private static String fetchHtml(String url, String encode) {
    log.info(url);
    StringBuffer sb = new StringBuffer();
    try {
      URL u = new URL(url);
      BufferedReader br = new BufferedReader(new InputStreamReader(
          u.openStream(), encode));
      String line = null;

      while ((line = br.readLine()) != null)
        sb.append(line);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }
 
  public static NewsList getNewsList() {
    Objectify ofy = ObjectifyService.begin();
    NewsList list = ofy.find(NewsList.class, NEWS_LIST_ID);
    if (list != null) {
      return list;
    }
    updateNewsList();
    return ofy.find(NewsList.class, NEWS_LIST_ID);
  }
  
  public static NewsList getNewsList(String id) {
      Objectify ofy = ObjectifyService.begin();
      NewsList list = ofy.find(NewsList.class, id);
      if (list != null) {
        return list;
      }
      updateNewsList(id);
      return ofy.find(NewsList.class, id);
    }
  
  public static String getNewsContent(String id) {
    Objectify ofy = ObjectifyService.begin();
    News news = ofy.find(News.class, id);
    if (news != null) {
      return news.getContent();
    }
    news = fetchNews(id);
    return news == null ? "" : news.getContent();
  }

  public static void updateNewsList(String id) {
      log.info("update news list for id " + id);
      Objectify ofy = ObjectifyService.begin();
      Query<News> newss = ofy.query(News.class).filter("id <= ", id).order("-id").limit(DEFAULT_SIZE);
      StringBuffer sb = new StringBuffer();
      String firstArticleId = "0";
      for (News news : newss) {
        if (firstArticleId.equals("0")) {
          firstArticleId = news.getId();
        }
        sb.append(news.toString());
        sb.append("<<>>");
      } 
      NewsList nl = new NewsList(id, firstArticleId, sb.toString());
      ofy.put(nl);
  }
  
  public static void updateNewsList() {
    log.info("update news list");
    Objectify ofy = ObjectifyService.begin();
    Query<News> newss = ofy.query(News.class).order("-id").limit(DEFAULT_SIZE);
    StringBuffer sb = new StringBuffer();
    String firstArticleId = "0";
    for (News news : newss) {
      if (firstArticleId.equals("0")) {
        firstArticleId = news.getId();
      }
      sb.append(news.toString());
      sb.append("<<>>");
    } 
    NewsList nl = new NewsList(NEWS_LIST_ID, firstArticleId, sb.toString());
    ofy.put(nl);
  }
  
  public static boolean updateNewss() {
    try {
      log.info("update newss");
      String html = fetchHtml(BASE_URL, "UTF8");
      Document doc = Jsoup.parse(html);

      Element realtimeList = doc.getElementsByClass("realtime_list").get(0);

      Elements links = realtimeList.getElementsByTag("a");
      Set<String> linkset = new HashSet<String>();
      boolean need_update_newslist = false;
      String firstArticleId = getNewsList().firstArticleId;
      Objectify ofy = ObjectifyService.begin();
      for (Element e : links) {
        String id = e.attr("href").replaceAll(".*/", "").replace(".htm", "");
        if (!id.isEmpty() && !linkset.contains(id)) {
          linkset.add(id);
          if (id.compareTo(firstArticleId) > 0) {
            need_update_newslist = true;
          }
          if (ofy.find(News.class, id) == null) {
            if (fetchNews(id) != null) {
              need_update_newslist = true;
            }
          }
        }
      }
      if (need_update_newslist) {
        updateNewsList();
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  
  // Fetch and persist news for the given id.
  // Returns null on failure.
  public static News fetchNews(String id) {
    try {
      String html = fetchHtml(BASE_URL + "/articles/" + id + ".htm", "UTF8");
      Document doc = Jsoup.parse(html);

      Element title = doc.getElementById("news_title");

      Element titleBar = doc.getElementsByClass("title_bar").get(0);
      Element date = titleBar.getElementsByClass("date").get(0);

      Element outterContent = doc.getElementsByClass("article_content").get(0);
      Element intro = outterContent.getElementsByClass("introduction").get(0);
      Element content = intro.nextElementSibling();

      Element img = intro.getElementsByTag("img").get(0);
      String picId = img.attr("src").replaceAll(".*/", "");
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
      String description = brief.text() + " " + content.text();
      description = description.substring(0, Math.min(400, description.length()));
      News news = new News(id, date.text(), "", title.text(),
          description, sb.toString(), "", picId);
      Objectify ofy = ObjectifyService.begin();
      ofy.put(news);
      return news;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // If id = -1 delete NewsList
  public static void delete(String id) {
    Objectify ofy = ObjectifyService.begin();
    if (id.equals("-1")) {
      ofy.delete(NewsList.class, NEWS_LIST_ID);
    } else {
      ofy.delete(News.class, id);
    }
  }
  
  public static String escape(String s) {
    return s.replace("&nbsp;", " ").replace("&quot;", "\"")
        .replace("&amp;", "&").replace("&middot;", "·");
  }
}
