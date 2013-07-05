//TODO: move progress dialog to action bar
//TODO: add jsoup

package gg.cnbeta.activity;

import gg.cnbeta.data.DAO;
import gg.cnbeta.data.ImageManager;
import gg.cnbeta.data.News;
import gg.cnbeta.data.NewsListParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

public class NewsListActivity extends Activity {
	
	private ActionBar actionBar;
	
	private List<News> list;
	private ListView listView;
	private SimpleAdapter adapter;
	
	private String rawNewsList;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newslist);
        actionBar = (ActionBar)this.findViewById(R.id.actionbar);
        listView = (ListView)this.findViewById(R.id.list);
       
        /* ActionBar*/
        actionBar.setTitle("cnBeta - 资讯列表");
        actionBar.addAction(new AbstractAction(R.drawable.ic_title_refresh) {
            @Override
            public void performAction(View view) {
            	updateNewsList();
            }
        });
        // Add listener 
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//Toast.makeText(getApplicationContext(), ""+list.get(position).getId(), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(NewsListActivity.this, NewsActivity.class);
				intent.putExtra("id", list.get(position).getId());
				//DAO.markRead(NewsListActivity.this.getApplicationContext(), list.get(position).getId());
				startActivity(intent);
			}   	
        });
        
        // Restore if previous state exists
        if(savedInstanceState != null && savedInstanceState.containsKey("rawNewsList")) {
        	rawNewsList = savedInstanceState.getString("rawNewsList");
        	updateListView();
    		return;
        }
 
        // try to load from local cache if exist
		rawNewsList = DAO.loadRawNewsList(getApplicationContext());
		updateListView();
		
		// Do a quick update from network
		updateNewsList();
    }
    
    public void onSaveInstanceState (Bundle outState) {
    	if(rawNewsList != null) {
    		outState.putString("rawNewsList", rawNewsList);
    	}
    }
    
    private void updateNewsList() {
    	//final  ProgressDialog dialog = ProgressDialog.show(NewsListActivity.this, "资讯列表", 
        //        "加载中，请稍候...", true, true);
    	actionBar.setProgressBarVisibility(View.VISIBLE);
    	Thread t = new Thread() {
        	public void run() {			
        		// Try to fetch the latest news list and update local cache
        		String tmpRawNewsList = DAO.fetchRawNewsList(getApplicationContext());        		
        		if(tmpRawNewsList == null){	// Update failure due to network problem		
        			listView.post(new Runnable() {
    		        	public void run(){
    		        		Toast.makeText(getApplicationContext(), "网络失败！", Toast.LENGTH_LONG).show();
    		        	}
    		        });
        		}
        		else if(tmpRawNewsList.length() > 0) {
        		    // There is update, update the list view
        		    rawNewsList = tmpRawNewsList;
                    updateListView();
        		}	
        		listView.post(new Runnable() {
			        	public void run() {
			        		//dialog.cancel();
			        		actionBar.setProgressBarVisibility(View.GONE);
			        	}
			       });
        		
        	}        
        };
        t.start();
    }
    
    // parse rawNewsList, create new adapter, and put it in listview
    private void updateListView() {
		if(rawNewsList != null) {
			list =NewsListParser.parse(rawNewsList);
			adapter = new SimpleAdapter( 
	        		NewsListActivity.this, 
	        		getMapList(list),
	        		R.layout.listitem_twoline_image,
	        		new String[] { "title","brief","image_uri" },
	        		new int[] {R.id.text1, R.id.text2, R.id.imageView } );
			adapter.setViewBinder(new MyViewBinder());
	        listView.post(new Runnable() {
	        	public void run() {
	        		listView.setAdapter(adapter);	
	        	}
	        });
		}
    }
    
    // generate a map for the listview based on the list of news instances
    private List<Map<String, Object>> getMapList(List<News> list) {
    	List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    	for(News news : list) {
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("title", news.getTitle());
    		map.put("brief", news.getTime()+" "+news.getBrief());
    		map.put("image_uri", news.getPicId());
    		ret.add(map);
    	}
    	return ret;
    }   
    
    /* Customized ViewBinder to help fetch topic picture and store in local cache*/
    private class MyViewBinder implements ViewBinder {
     	@Override
 		public boolean setViewValue(View view, Object data,
 				String textRepresentation) {
 			if( (view instanceof ImageView) & (data instanceof String) ) {
 				final ImageView iv = (ImageView) view;
 				ImageManager.getInstance().SetImageView(iv, (String)data);
 				return true;
 			} 
 			/*else if (view.getId() == R.id.text1) {
 				final TextView tv = (TextView) view;
 				final String s = (String) data;
 				tv.setText(s.substring(1));
 				if (s.startsWith("1")) {
 					tv.setTextColor(NewsListActivity.this.getResources().getColor(R.color.listitem_title_read));
 				}
 				return true;
 			} else if (view.getId() == R.id.text2) {
 				final TextView tv = (TextView) view;
 				final String s = (String) data;
 				tv.setText(s.substring(1));
 				if (s.startsWith("1")) {
 				    tv.setTextColor(NewsListActivity.this.getResources().getColor(R.color.listitem_brief_read));
 				}
 				return true;
 			}
 			*/
 			return false;
 		}	
    }
    
    /*
    // Create an intent for starting itself
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, NewsListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
    */
}