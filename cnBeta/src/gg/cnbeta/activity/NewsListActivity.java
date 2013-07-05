//TODO: move progress dialog to action bar
//TODO: add jsoup

package gg.cnbeta.activity;

import gg.cnbeta.data.DAO;
import gg.cnbeta.data.ImageManager;
import gg.cnbeta.data.News;
import gg.cnbeta.data.NewsListManager;
import gg.cnbeta.data.NewsListParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

public class NewsListActivity extends Activity {
	
	private ActionBar actionBar;
	
	private ListView listView;
	
	private Button mMoreButton;
	
	private List<News> list;
	private ArrayAdapter<News> mAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newslist);
        actionBar = (ActionBar)this.findViewById(R.id.actionbar);
        listView = (ListView)this.findViewById(R.id.list);
        list = new ArrayList<News>();
        mAdapter = new NewsListAdapter(getApplicationContext(), 0, list);
        listView.setAdapter(mAdapter);
        addFooter(listView);
       
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
        /*
        if(savedInstanceState != null && savedInstanceState.containsKey("rawNewsList")) {
        	rawNewsList = savedInstanceState.getString("rawNewsList");
        	updateListView();
    		return;
        }
 */
        NewsListManager.getInstance().loadNewsListFromFile(getApplicationContext());
		updateListView();
		
		// Do a quick update from network
		updateNewsList();
    }
   /* 
    public void onSaveInstanceState (Bundle outState) {
    	if(rawNewsList != null) {
    		outState.putString("rawNewsList", rawNewsList);
    	}
    }
    */
    private void addFooter(ListView listView) {
        mMoreButton = new Button(getApplicationContext());
        mMoreButton.setText("点击加载更多新闻");
        mMoreButton.setTextColor(getResources().getColor((R.color.listitem_title)));
        mMoreButton.setBackgroundColor(Color.WHITE);
        mMoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "加载", Toast.LENGTH_LONG).show();
                updateListView();
            }
            
        });
        listView.addFooterView(mMoreButton);
    }
    
    static class ViewHolder {
        TextView title;
        TextView brief;
        ImageView icon;
    }
    
    class NewsListAdapter extends ArrayAdapter<News> {

        private Context mContext;
        private List<News> mObjects;

        public NewsListAdapter(Context context, int textViewResourceId, List<News> objects) {
            super(context, textViewResourceId, objects);
            mContext = context;
            mObjects = objects;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = null;
            ViewHolder views = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.listitem_twoline_image, parent, false);
                views = new ViewHolder();
                views.title = (TextView) rowView.findViewById(R.id.title);
                views.brief = (TextView) rowView.findViewById(R.id.brief);
                views.icon = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(views);
            } else {
                rowView = convertView;
                views = (ViewHolder) convertView.getTag();
            }
           
            News news = mObjects.get(position);
            views.title.setText(news.getTitle());
            views.brief.setText(news.getTime() + " " + news.getBrief());
            ImageManager.getInstance().SetImageView(views.icon, news.getPicId());
            return rowView;
        }
        
    }
    
    /*
     * Must run in UI thread.
     */
    private void updateNewsList() {
    	//final  ProgressDialog dialog = ProgressDialog.show(NewsListActivity.this, "资讯列表", 
        //        "加载中，请稍候...", true, true);
    	actionBar.setProgressBarVisibility(View.VISIBLE);
    	new Thread() {
    	    public void run() {			
    	        // Try to fetch the latest news list and update local cache
    	        String tmpRawNewsList = NewsListManager.getInstance().updateNewsListForLatest(getApplicationContext());        		
    	        if(tmpRawNewsList == null){	// Update failure due to network problem		
    	            listView.post(new Runnable() {
    	                public void run(){
    	                    Toast.makeText(getApplicationContext(), "网络失败！", Toast.LENGTH_LONG).show();
    	                }
    	            });
    	        } else if(tmpRawNewsList.length() > 0) {
    	            listView.post(new Runnable() {
    	                public void run() {
    	                    //dialog.cancel();
    	                    updateListView();
    	                }
    	            });	    
    	        }
    	        listView.post(new Runnable() {
    	            public void run() {
    	                //dialog.cancel();
    	                actionBar.setProgressBarVisibility(View.GONE);
    	            }
    	        });

    	    }        
    	}.start();
    }

    /*
     * Must run in UI thread.
     */
    private void updateListView() {
        List<News> tl = NewsListManager.getInstance().getLatestNewsList();
        if (tl.size() > 0) {
            list.clear();
            list.addAll(tl);
        }

        Log.d("GG", "listview refreshed!!");
    }

}    
			/*
			mAdapter = new NewsListAdapter(getApplicationContext(), 0, list);
	        		
	        listView.post(new Runnable() {
	        	public void run() {
	        	 // save index and top position
	        	    int index = listView.getFirstVisiblePosition();
	        	    View v = listView.getChildAt(0);
	        	    int top = (v == null) ? 0 : v.getTop();

	        	    listView.setAdapter(mAdapter);

	        	    // restore
	        	    listView.setSelectionFromTop(index, top);
	        		
	        	}
	        });
	        */

    
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
 			
    /*
    // Create an intent for starting itself
    public static Intent createIntent(Context context) {
        Intent i = new Intent(context, NewsListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
    */
