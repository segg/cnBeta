//TODO: move progress dialog to action bar
//TODO: add jsoup

package gg.cnbeta.activity;

import gg.cnbeta.data.ImageManager;
import gg.cnbeta.data.News;
import gg.cnbeta.data.NewsListManager;
import gg.cnbeta.data.NewsListParser;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
        // Footer view must be set before adapter
        addFooter(listView);
        listView.setAdapter(mAdapter);
       
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
        	String rawNewsList = savedInstanceState.getString("rawNewsList");
        	List<News> tl = NewsListParser.parse(rawNewsList);
        	updateListView(tl, false);
    		return;
        }

        NewsListManager.getInstance().loadNewsListFromFile(getApplicationContext());
		updateListView(NewsListManager.getInstance().getLatestNewsList(), true);
		
		// Do a quick update from network
		updateNewsList();
    }

    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("rawNewsList", NewsListParser.build(list));
    }

    private void addFooter(ListView listView) {
        mMoreButton = new Button(getApplicationContext());
        mMoreButton.setText("加载中...");
        mMoreButton.setClickable(false);
        mMoreButton.setTextColor(getResources().getColor((R.color.listitem_title)));
        mMoreButton.setBackgroundColor(Color.WHITE);
        mMoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoreToNewsList();
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
    	        //List<News> moreList = NewsListManager.getInstance().getNewsList(list.get(list.size() - 1).getId(), getApplicationContext());
    	        if(tmpRawNewsList == null){	// Update failure due to network problem		
    	            runOnUiThread(new Runnable() {
    	                public void run(){
    	                    Toast.makeText(getApplicationContext(), "网络失败！", Toast.LENGTH_LONG).show();
    	                }
    	            });
    	        } else if(tmpRawNewsList.length() > 0) {
    	            runOnUiThread(new Runnable() {
    	                public void run() {
    	                    //dialog.cancel();
    	                    updateListView(NewsListManager.getInstance().getLatestNewsList(), true);
    	                }
    	            });	    
    	        }
    	        runOnUiThread(new Runnable() {
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
    private void addMoreToNewsList() {
        //final  ProgressDialog dialog = ProgressDialog.show(NewsListActivity.this, "资讯列表", 
        //        "加载中，请稍候...", true, true);
        actionBar.setProgressBarVisibility(View.VISIBLE);
        mMoreButton.setText("加载中...");
        mMoreButton.setClickable(false);
        new Thread() {
            public void run() {
                if (list.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            //dialog.cancel();
                            updateNewsList();
                        }
                    });
                    return;
                }
                // Try to fetch the latest news list and update local cache          
                final List<News> moreList = NewsListManager.getInstance().getNewsList(list.get(list.size() - 1).getId(), getApplicationContext());
                if(moreList == null){ // Update failure due to network problem        
                    runOnUiThread(new Runnable() {
                        public void run(){
                            Toast.makeText(getApplicationContext(), "网络失败！", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if(moreList.size() > 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {     
                            list.addAll(moreList); 
                            mAdapter.notifyDataSetChanged();
                            Log.d("append more to listview!!");
                        }
                    });     
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        //dialog.cancel();
                        actionBar.setProgressBarVisibility(View.GONE);
                        mMoreButton.setText("点击加载更多资讯");
                        mMoreButton.setClickable(true);
                        if (list.size() >= NewsListManager.MAX_NEWS_LIST_SIZE) {
                            mMoreButton.setText("~~ 到底啦 ~~");
                            mMoreButton.setClickable(false);
                        }
                    }
                });

            }        
        }.start();
    }

    /*
     * Must run in UI thread.
     */
    private void updateListView(List<News> tl, boolean scrollToTop) {
        if (tl != null && tl.size() > 0) {
            list.clear();
            list.addAll(tl);
            mAdapter.notifyDataSetChanged();
            if (scrollToTop) {
                listView.setSelectionAfterHeaderView();
            }
            mMoreButton.setText("点击加载更多资讯");
            mMoreButton.setClickable(true);
        }

        Log.d("listview refreshed!!");
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
