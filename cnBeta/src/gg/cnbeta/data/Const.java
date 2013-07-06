package gg.cnbeta.data;

public interface Const {
    public static final boolean DEBUG = false;
    public static final boolean CHINA = false;

    public static final String URL_APPSPOT = "http://gg--uu.appspot.com";
    public static final String URL_PROXY = "http://50.87.186.28/cgi-bin/cnbeta_newslist2.sh";
    public static final String URL_GAE_NEWS_LIST = URL_APPSPOT + "/feed?firstArticleId=";
    public static final String URL_GAE_NEWS_LIST_MORE = URL_APPSPOT + "/feed?l=";
    public static final String URL_PROXY_NEWS_LIST = URL_PROXY + "?firstArticleId=";
    public static final String URL_PROXY_NEWS_LIST_MORE = URL_PROXY + "?l=";
    public static final String URL_GAE_NEWS_CONTENT = URL_APPSPOT + "/feed?id=";
    public static final String ENCODING_DEFAULT = "UTF-8";
    public static final String URL_CNBETA = "http://cnbeta.com";
    public static final String URL_CNBETA_IMAGE = "http://static.cnbetacdn.com/topics/";
}
