package gg.cnbeta.data;

public class DAO {
	
	public static final String FILENAME_READ = "read.txt";

	/*
	private static Set<String> reads;
	private static void initReads(Context context) {
		reads = new HashSet<String>();
		File f = new File(context.getCacheDir(), FILENAME_READ);
		String line = null;
		if(f.exists()) {	
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				line = br.readLine();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		File f = new File(context.getFilesDir(), FILENAME_READ);
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(f));
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
		reads.add(id);
		saveReads(context);
	}
	*/
}
