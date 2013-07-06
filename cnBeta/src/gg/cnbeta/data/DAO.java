package gg.cnbeta.data;

import gg.cnbeta.activity.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;

public class DAO {

	public static final String FILENAME_READ = "read.txt";
  
	private static Set<String> reads;
	private static void initReads(Context context) {
		reads = new HashSet<String>();
		String line = null;
		try {
		    BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(FILENAME_READ)));
		    line = br.readLine();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if (line != null) {
			String tokens[] = line.split(",");
			reads.addAll(Arrays.asList(tokens));
		}
	}
	private static void saveReads(Context context) {
		StringBuffer sb = new StringBuffer();
		for (String read : reads) {
			sb.append(read);
			sb.append(",");
		}
		
		PrintWriter pw;
		try {
			pw = new PrintWriter(new PrintWriter(context.openFileOutput(FILENAME_READ, 0)));
			pw.println(sb.toString());
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Returns true if the article of the id has been read.
	public static boolean checkRead(Context context, String id) {
		if (reads == null) {
			initReads(context);		
		}
		return reads.contains(id);
	}
	public static void markRead(Context context, String id) {
	    if (reads == null) {
            initReads(context);     
        }
		reads.add(id);
		Log.d("mark read " + id + "  total read " + reads.size());
		saveReads(context);
	}
}
