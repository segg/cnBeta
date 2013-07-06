package gg.cnbeta.activity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import gg.cnbeta.data.Const;
import gg.cnbeta.data.NetworkUtil;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class NewsActivity extends Activity {
	
	private WebView webView;
	private ActionBar actionBar;
	private String content;
	
	// An back action that go back to home and close the current activity.
	private class BackAction implements Action {

	    @Override
	    public int getDrawable() {
	        return R.drawable.ic_title_back;
	    }

	    @Override
	    public void performAction(View view) {
	        onBackPressed();
	        finish();
	    }

	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        actionBar = (ActionBar)this.findViewById(R.id.actionbar);
       
        /* ActionBar*/
        actionBar.setTitle("cnBeta - 资讯内容");
        actionBar.setHomeAction(new BackAction());
        actionBar.setDisplayHomeAsUpEnabled(false);
        /*
        actionBar.addAction(new AbstractAction(R.drawable.ic_title_refresh) {
            @Override
            public void performAction(View view) {
            	updateNews();
            }

        });
        */
        webView = (WebView)findViewById(R.id.webview);
        //webView.setBackgroundColor(Color.BLACK);
        //webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);	//black scroll bar
    	//webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);	//disable horizontal scroll bar
    	
        // Restore previous state
        if(savedInstanceState != null && savedInstanceState.containsKey("content")) {
        	content = savedInstanceState.getString("content");
        	webView.loadDataWithBaseURL(Const.URL_CNBETA, content, "text/html", Const.ENCODING_DEFAULT, null);
        } else {
        	// do a fetch and update UI
        	updateNews();
        }
        
    }
    
    // Save state
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    	if(content != null) {
    		outState.putString("content", content);
    	}
    }
    
    private void updateNews() {
    	//final ProgressDialog dialog = ProgressDialog.show(this, "资讯内容", 
        //        "加载中，请稍候...", true, true);
    	actionBar.setProgressBarVisibility(View.VISIBLE);
		Thread t = new Thread() {    	
			public void run() {
		        Bundle extras = getIntent().getExtras();
		        if(extras != null) {
		        	String id = extras.getString("id");
		        	// Fetch news content from GAE or cnBeta.com
		        	content = fetchNewsContent(id);
		        	if(content != null)
		        	    runOnUiThread(new Runnable(){public void run(){
		        			webView.loadDataWithBaseURL(Const.URL_CNBETA, content, "text/html", Const.ENCODING_DEFAULT, null);
		        		}});
		        	else
		        	    runOnUiThread(new Runnable(){public void run(){
		        			Toast.makeText(getApplicationContext(), "网络失败！", Toast.LENGTH_LONG).show();
		        		}});
		        }
		        //dialog.cancel();
		        runOnUiThread(new Runnable(){
		        	public void run() {
		        		//dialog.cancel();
		        		actionBar.setProgressBarVisibility(View.GONE);
		        	}
		       });
			}
       };
       t.start();
    }
    
    public static String convertHtmlToText(String s) {
        return s.replace("&nbsp;", " ").replace("&quot;", "\"").replace("&amp;", "&").replace("&middot;", "·");     
    }
    
    // First try GAE
    // Then try cnbeta.com
    public String fetchNewsContent(String id)
    {
        String content = Const.CHINA ? null : fetchNewsContentFromGAE(id);
        if(content == null) {
            content = fetchNewsContentFromCnbeta(id);
        }
        return content;
    }
    private String fetchNewsContentFromGAE(String id) {
        if (Const.CHINA) {
            return null;   
        }
        String content = NetworkUtil.fetchHtml(Const.URL_GAE_NEWS_CONTENT + id, null);
        return content;
    }
    
    /* Return null if the news with the id does not exist */
    private String fetchNewsContentFromCnbeta(String id) {
        String html = NetworkUtil.fetchHtml(Const.URL_CNBETA + "/articles/" + id +".htm", null);
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
}
