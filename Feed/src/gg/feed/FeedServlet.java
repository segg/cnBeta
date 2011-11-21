package gg.feed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

@SuppressWarnings("serial")
public class FeedServlet extends HttpServlet {
	
	 private static final Logger log = Logger.getLogger(FeedServlet.class.getName());
	 
	 
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setCharacterEncoding("UTF-8");
		String id = req.getParameter("id");
		String hash = req.getParameter("hash");
		String deleteDatabase = req.getParameter("deleteDatabase");
		if(deleteDatabase != null)
		{
			DAO.deleteDatabase();
			resp.getWriter().println("done");
		}
		else if(hash != null && hash.equals("0"))	// fetch news list and update datastore
		{
			resp.getWriter().println(DAO.updateNewsList());
		}
		else if(id == null)
		{
			resp.setContentType("text/plain");
			NewsList nl = DAO.getOrCreateNewsList();
			if(nl == null) 
				return;
			//System.out.println(hash+" "+nl.hash);
			if(hash != null && hash.equals(nl.hash))
				resp.getWriter().println(nl.hash);	// only write back id if no change
			else
				resp.getWriter().println(nl.newsList);	// write back whole list if any change
		}
		else
		{
			resp.setContentType("text/html");
			resp.getWriter().println(DAO.loadOrFetchNewsContent(id));
		}
		
	}
	

}
