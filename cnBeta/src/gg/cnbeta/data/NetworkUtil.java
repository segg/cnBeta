package gg.cnbeta.data;

import gg.cnbeta.activity.NewsListActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

public class NetworkUtil {
	
	public static String fetchHtml(String url, String encode) {

		if(encode == null)
			encode = NewsListActivity.ENCODING_DEFAULT;
		StringBuffer sb = new StringBuffer();
		try {
			URL u = new URL(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream(), encode));
			String line = null;
			while((line = br.readLine()) != null)
				sb.append(line);	
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		return sb.toString();
	}

	
}
