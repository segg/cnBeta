package gg.feed;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class FeedServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(FeedServlet.class
      .getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/plain");

    String id = req.getParameter("id");
    String crawl = req.getParameter("crawl");
    String firstArticleId = req.getParameter("firstArticleId");
    String l = req.getParameter("l");
    String deleteId = req.getParameter("deleteId");

    if (deleteId != null) {
      DAO.delete(deleteId);
      resp.getWriter().println(deleteId + " deleted");
    } else if (crawl != null) {
      boolean success = DAO.updateNewss();
      resp.getWriter().println("Crawl latest news: " + success);
    } else if (id != null) {
      resp.setContentType("text/html");
      resp.getWriter().println(DAO.escape(DAO.getNewsContent(id)));
    } else if (l != null) {
        NewsList nl = DAO.getNewsList(l);
        if (nl == null) {
            log.warning("Failed to get news list");
            return;
        }
        resp.getWriter().println(DAO.escape(nl.newsList));
    } else {
      NewsList nl = DAO.getNewsList();
      if (nl == null) {
        log.warning("Failed to get news list");
        return;
      }
      if (firstArticleId != null && firstArticleId.equals(nl.firstArticleId)) {
        // Reply with firstArticleId if no change
        resp.getWriter().println(nl.firstArticleId);
      } else {
        resp.getWriter().println(DAO.escape(nl.newsList));
      }
    }
  }
}
