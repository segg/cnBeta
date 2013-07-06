package gg.cnbeta.data;

import gg.cnbeta.activity.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.Context;


/*
 * A singleton class to manage news list.
 * Responsible for maintaining a List of News objects
 */
public class NewsListManager {
    public static final String FILENAME_NEWS_LIST = "newslist.txt";
    public static final int DEFAULT_SIZE = 50;
    
    public static final int MAX_NEWS_LIST_SIZE = 500;

    private static NewsListManager mInstance;
    
    private SortedMap<String, News> mNewss;
    
    static {
        mInstance = new NewsListManager();
    }
    
    private NewsListManager() {
        mNewss = new TreeMap<String, News>(new Comparator<String>() {
            /* revert the natural order to have the largest first */
            @Override
            public int compare(String lhs, String rhs) {
                return rhs.compareTo(lhs);
            }
        });
    }
    
    public static NewsListManager getInstance() {
        return mInstance;
    }
    
    public List<News> getLatestNewsList() {
        Log.d("Latest " + mNewss.size());
        List<News> list = new ArrayList<News>();
        int count = 0;
        for (Map.Entry<String, News> entry : mNewss.entrySet()) {
            list.add(entry.getValue());
            if (++count >= DEFAULT_SIZE) {
                break;
            }
        }
        return list;
    }
    
    /*
     * Returns a list of news with Id from startId DESC.
     * If there is startId is greater than the most recent news, start from the
     * most recent news.
     */
    public List<News> getNewsList(String startId, Context context) {
        Log.d("newslist " + mNewss.size());
        List<News> list = new ArrayList<News>();
        SortedMap<String, News> map = mNewss.tailMap(startId);
        if (map.size() < DEFAULT_SIZE) {
            updateNewsListForMore(startId, context);
        }
        map = mNewss.tailMap(startId);
        int count = 0;
        boolean first = true;
        for (Map.Entry<String, News> entry : map.entrySet()) {
            if (first) {
                first = false;
                continue;
            }
            list.add(entry.getValue());
            if (++count >= DEFAULT_SIZE) {
                break;
            }
        }
        return list;
    }
    
    private String updateNewsListForMore(String fromId, Context context) {
        String rawNewsList = fetchRawNewsListForMore(fromId);
        if (rawNewsList != null && rawNewsList.length() > 0) {
            updateNewsMap(rawNewsList);
            saveNewsListToFile(context);
        }
        return rawNewsList;
    }
    
    public String updateNewsListForLatest(Context context) {
        String rawNewsList = fetchRawNewsList();
        if (rawNewsList != null && rawNewsList.length() > 0) {
            updateNewsMap(rawNewsList);
            saveNewsListToFile(context);
        }
        return rawNewsList;
    }
/*    
    class FetchNewsListTask implements Runnable {

        private ReentrantLock mLock;
        private Condition mCondition;
        private boolean mDone;
        
        public FetchNewsListTask() {
            mDone = false;
            mCondition = mLock.newCondition();
        }

        @Override
        public void run() {
           
        }
        
        public void Finish() {
            mLock.lock();
            try {
                mDone = true;
                mCondition.signal();
            } finally {
                mLock.unlock();
            }
        }
        
        public void WaitUntilFinish() {
            mLock.lock();
            try {
                while (!mDone) {
                    mCondition.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mLock.unlock();
            }
        }
        
    }
 */   
    private void updateNewsMap(String rawNewsList) {
        List<News> list = NewsListParser.parse(rawNewsList);
        if (list == null) {
            return;
        }
        for (News n : list) {
            mNewss.put(n.getId(), n);
        }
    }
    
    private void saveNewsListToFile(Context context) {
        List<News> list = new ArrayList<News>();
        int count = 0;
        for (Map.Entry<String, News> entry : mNewss.entrySet()) {
            list.add(entry.getValue());
            if (++count >= MAX_NEWS_LIST_SIZE) {
                break;
            }
        }
        PrintWriter pw;
        try {
            pw = new PrintWriter(new PrintWriter(context.openFileOutput(FILENAME_NEWS_LIST, 0)));
            String toPrint = NewsListParser.build(list);
            pw.println(toPrint);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean mLoaded = false;
    public void loadNewsListFromFile(Context context) {
        if (mLoaded) {
            return;
        }
        mLoaded = true;
        String line = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(FILENAME_NEWS_LIST)));
            line = br.readLine(); 
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (line != null) {
            updateNewsMap(line);
        }
    }
    
    // First try GAE
    // Then try cnbeta.com
    private String fetchRawNewsList()
    {   
        String firstArticleId = mNewss.size() > 0 ? mNewss.firstKey() : "0";
        String newsList = Const.CHINA ? null : fetchRawNewsListFromUrl(Const.URL_GAE_NEWS_LIST, firstArticleId);
        if(newsList == null) {
            newsList = fetchRawNewsListFromUrl(Const.URL_PROXY_NEWS_LIST, firstArticleId);
        }
        return newsList;
        
    }
    
    // First try GAE
    // Then try cnbeta.com
    private String fetchRawNewsListForMore(String id)
    {   
        String newsList = Const.CHINA ? null : fetchRawNewsListFromUrl(Const.URL_GAE_NEWS_LIST_MORE, id);
        if(newsList == null) {
            newsList = fetchRawNewsListFromUrl(Const.URL_PROXY_NEWS_LIST_MORE, id);
        }
        return newsList;
        
    }
    
    // Check with server using firstArticleId
    // Return news list if any update
    // Return "" if no change
    // Return null if any network problem
    private String fetchRawNewsListFromUrl(String url, String firstArticleId)
    {
        String html = null;
        html = NetworkUtil.fetchHtml(url + firstArticleId, null);       
        if(html == null)
            return null;    // Network problem
        if(html.equals(firstArticleId))
            return "";  
        return html;
    }
}
