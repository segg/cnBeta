package gg.cnbeta.activity;

import gg.cnbeta.data.Const;
import gg.cnbeta.data.DAO;
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
		        	content = DAO.fetchNewsContent(id);
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
}
