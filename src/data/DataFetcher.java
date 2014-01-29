package data;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

public class DataFetcher {
	
	static HashMap<String, String> LOCATION_IN_HOME = new HashMap<String, String>();
	static HashMap<String, String> FILENAME_SUFFIX = new HashMap<String, String>(); 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataFetcher.initLocations();
		System.out.println("This program attempts to download data files between a given range of dates");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	    int startDate = 20100101, endDate = 20110530;
	    String dataType = "";
	    try {
	    	System.out.println("Enter the type of data you desire (bhavcopy/MTO/FandO/all): ");
	    	dataType = in.readLine();
	    	while (!DataFetcher.LOCATION_IN_HOME.containsKey(dataType) &&
	    		   !dataType.equals("all")) {
	    		System.out.println("Type of data, again: ");
	    		dataType = in.readLine();
	    	}
	    	System.out.println("Please enter start date: ");
			startDate = Integer.parseInt(in.readLine());			
			System.out.println("Please enter end date: ");
			endDate = Integer.parseInt(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vector<String> dataTypesToFetch = new Vector<String>();
		if (dataType.equals("all")) {
			for (String s : DataFetcher.LOCATION_IN_HOME.keySet()) {
				dataTypesToFetch.add(s);
			}
		} else {
			dataTypesToFetch.add(dataType);
		}
		int currentDate = getNextDate(startDate, true, true);
		do {
			int year = currentDate / 10000;
			System.out.println("Working on: " + currentDate);
			for (String d : dataTypesToFetch) {
				String address = getDateFileString(currentDate, d);
				String localDir = DataFetcher.LOCATION_IN_HOME.get(d)
						+ "/" + year;
				new File(localDir).mkdirs();
				String localFile = localDir + "/" + currentDate
						+ DataFetcher.FILENAME_SUFFIX.get(d);
				// System.out.println("Address: " + address + " local: " +
				// localFile);
				download(address, localFile);
			}
			currentDate = getNextDate(currentDate, false, true);
		} while (currentDate <= endDate);
	}
	
	public static void initLocations() {
		String baseDir = "/home/sangram/NSE_stocks/data/downloads";
		DataFetcher.LOCATION_IN_HOME.put("bhavcopy", baseDir + "/bhavcopy");
		DataFetcher.LOCATION_IN_HOME.put("MTO", baseDir + "/MTO");
		DataFetcher.LOCATION_IN_HOME.put("FandO", baseDir + "/FandO");
		DataFetcher.FILENAME_SUFFIX.put("bhavcopy", ".csv.zip");
		DataFetcher.FILENAME_SUFFIX.put("MTO", ".dat");
		DataFetcher.FILENAME_SUFFIX.put("FandO", ".csv.zip");
	}
	
	/**
	 * 
	 * @param date - the date in format YYYYMMDD
	 * @param returnSame - if this is true, we return the same day, if its not a weekend and ignoreWeekend is true, or same date itself
	 * @param ignoreWeekend - skip Saturdays and Sundays
	 * @return next date as defined above
	 */
	
	public static int getNextDate(int date, boolean returnSame, boolean ignoreWeekend) {
		int nextDate;
		Calendar c = Calendar.getInstance();
		int day = date % 100;
		date = date / 100;
		int month = date % 100 - 1;
		date = date / 100;
		int year = date;
		c.set(year, month, day);
		if (!returnSame) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		while (ignoreWeekend && c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		nextDate = getIntDateFromCalendar(c);
		return nextDate;
	}
	
	public static int getIntDateFromCalendar(Calendar c) {
		return c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH)+1) * 100 + c.get(Calendar.DAY_OF_MONTH);
	}
	
	public static String getDateFileString(int date, String dataType) {
		int year = date / 10000;
		int month = date / 100; 
		month = month % 100;
		String months[] = {"", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
		int day = date % 100;
		String address = "";
		if (dataType.equals("bhavcopy")) {
			String baseAddress = "http://www.nseindia.com/content/historical/EQUITIES";
			address = String.format("%s/%d/%s/cm%02d%s%dbhav.csv.zip", baseAddress, year, months[month], day, months[month], year);
		} else if (dataType.equals("MTO")) {
			String baseAddress = "http://www.nseindia.com/archives/equities/mto";
			address = String.format("%s/MTO_%02d%02d%d.DAT", baseAddress, day, month, year);
		} else if (dataType.equals("FandO")) {
			String baseAddress = "http://www.nseindia.com/content/historical/DERIVATIVES";
			address = String.format("%s/%d/%s/fo%02d%s%dbhav.csv.zip", baseAddress, year, months[month], day, months[month], year);
		}
		return address;
	}
	
	public static void download(String address, String localFileName) {
	    OutputStream out = null;
	    URLConnection conn = null;
	    InputStream in = null;
	    try {
	        // Get the URL
	        URL url = new URL(address);
	        // Open an output stream to the destination file on our local filesystem
	        out = new BufferedOutputStream(new FileOutputStream(localFileName));
	        conn = url.openConnection();
	        in = conn.getInputStream();
	        	 
	        // Get the data
	        byte[] buffer = new byte[1024];
	        int numRead;
	        while ((numRead = in.read(buffer)) != -1) {
	            out.write(buffer, 0, numRead);
	        }            
	        // Done! Just clean up and get out
	    } catch (Exception exception) {
	        System.err.println("Can't download file from " + address + " -- possibly a holiday");
	        File f = new File(localFileName);
	        f.delete();
	        if (!(address.equals(exception.getMessage()))) {
	        	System.err.println("No, it was not a holiday! ERROR = " + exception.getMessage());
	        	try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	        	
	        }
	    } finally {
	        try {
	            if (in != null) {
	                in.close();
	            }
	            if (out != null) {
	                out.close();
	            }
	        } catch (IOException ioe) {
	            // Shouldn't happen, maybe add some logging here if you are not 
	            // fooling around ;)
	        }
	    }
	}

}
