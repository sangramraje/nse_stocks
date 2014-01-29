package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class ResultFetcher {

	static String baseAddress = "http://www.nseindia.com/marketinfo/companyinfo/eod/corp_res.jsp?symbol=";
	static String baseLocalFilename = "/home/sangram/NSE_stocks/results/downloads";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader("/home/sangram/NSE_stocks/stockList"));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}
		HashMap<String, String> monthMapping = new HashMap<String, String>();
		monthMapping.put("-JAN-", "01");
		monthMapping.put("-FEB-", "02");
		monthMapping.put("-MAR-", "03");
		monthMapping.put("-APR-", "04");
		monthMapping.put("-MAY-", "05");
		monthMapping.put("-JUN-", "06");
		monthMapping.put("-JUL-", "07");
		monthMapping.put("-AUG-", "08");
		monthMapping.put("-SEP-", "09");
		monthMapping.put("-OCT-", "10");
		monthMapping.put("-NOV-", "11");
		monthMapping.put("-DEC-", "12");
		String stock = null;
		try {
			while ((stock = in.readLine()) != null) {
				System.out.println("Working on stock " + stock);
				String pageAddress = baseAddress + stock;
				String stockDir = baseLocalFilename + "/" + stock;
				new File(stockDir).mkdirs();
				String filename = stockDir + "/resultPage.html";
				DataFetcher.download(pageAddress, filename);
				final ArrayList<String> list = new ArrayList<String>();

				ParserDelegator parserDelegator = new ParserDelegator();
				ParserCallback parserCallback = new ParserCallback() {
					public void handleText(final char[] data, final int pos) {
					}

					public void handleStartTag(Tag tag,
							MutableAttributeSet attribute, int pos) {
						if (tag == Tag.A) {
							String address = (String) attribute
									.getAttribute(Attribute.HREF);
							if (address != null
									&& address
											.contains("/marketinfo/companyinfo/eod/results.jsp")) {
								list.add("http://www.nseindia.com" + address);
							}
						}
					}

					public void handleEndTag(Tag t, final int pos) {
					}

					public void handleSimpleTag(Tag t, MutableAttributeSet a,
							final int pos) {
					}

					public void handleComment(final char[] data, final int pos) {
					}

					public void handleError(final java.lang.String errMsg,
							final int pos) {
					}
				};
				try {
					parserDelegator.parse(new FileReader(filename), parserCallback,
							false);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String downloadListFile = stockDir + "/downloadList";
				File ftest = new File(downloadListFile);
				HashSet<String> downloadedLinks = new HashSet<String>();
				if (ftest.canRead()) {
					BufferedReader fileIn = new BufferedReader(new FileReader(
							stockDir + "/downloadList"));
					// read all the links into a map, to look up and find
					// out if we really need to download anything at this point
					String nextLine = null;
					while ((nextLine = fileIn.readLine()) != null) {
						String thisLink = nextLine.split(" ")[0];
						downloadedLinks.add(thisLink);
					}
					fileIn.close();
				}
				FileWriter fw = new FileWriter(stockDir + "/downloadList", true);
				boolean somethingToDo = false;
				for (String s : list) {
					if (!downloadedLinks.contains(s)) {
						somethingToDo = true;
						System.err.println("Need to download: " + s);
						String startDate = s.substring(69, 80);
						startDate = startDate.substring(7, 11)
								+ monthMapping.get(startDate.substring(2, 7))
								+ startDate.substring(0, 2);
						String endDate = s.substring(80, 91);
						endDate = endDate.substring(7, 11)
								+ monthMapping.get(endDate.substring(2, 7))
								+ endDate.substring(0, 2);

						String description = s.substring(91, 98);
						String resultFile = stockDir + "/results." + startDate
								+ "." + endDate + "." + description;
						System.out.println("Downloading: " + s);
						fw.write(s + " " + resultFile + "\n");
						DataFetcher.download(s, resultFile);
					}
				}
				fw.close();
				if (!somethingToDo) {
					System.out.println("Nothing to be done for stock " + stock);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}