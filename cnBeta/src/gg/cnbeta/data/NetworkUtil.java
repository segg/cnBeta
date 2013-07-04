package gg.cnbeta.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;


public class NetworkUtil {
	
	public static String fetchHtml(String url, String encode) {

		if(encode == null)
			encode = Const.ENCODING_DEFAULT;
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
