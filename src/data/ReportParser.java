package data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class ReportParser {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Calling parseStockReports");
		parseStockReports("3IINFOTECH");
	}

	public static void parseStockReports(String stock) {
		BufferedReader in = null;

		try {
			in = new BufferedReader(new FileReader(
					"/home/sangram/NSE_stocks/results/downloads/" + stock
							+ "/downloadList"));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return;
		}

		final ArrayList<String> reportList = new ArrayList<String>();
		String line = null;
		try {
			while ((line = in.readLine()) != null) {
				String tokens[] = line.split(" ");
				reportList.add(tokens[1]);
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String report : reportList) {
			System.out.println("Calling exportCsvReport for " + report);
			extractCsvReport(report, "/dev/null");
		}
	}

	public static void extractCsvReport(String infile, String outfile) {
		ParserDelegator parserDelegator = new ParserDelegator();
		ParserCallback parserCallback = new ParserCallback() {
			private boolean insideTd = false;
			private boolean insideRequiredTable = false;
			private boolean rowStart = false;
			private boolean printData = false;
			private boolean previousWasImage = false;
			private boolean firstLine = true;
			private int columnNumber = 1;

			public void handleText(final char[] data, final int pos) {
				if (insideTd) {
					// if (this.insideRequiredTable) {
					// System.out.println("Text: " + new String(data));
					// }
					if (new String(data).equals("Description")) {
						this.insideRequiredTable = true;
						// System.out.println("Table start: " + new
						// String(data));
					} else if (this.insideRequiredTable && this.printData) {
						if (this.columnNumber == 2) {
							System.out.print("\",");
						} else if (!this.previousWasImage && !this.firstLine
								&& this.columnNumber == 1) {
							System.out.print("\"\n");
						} else {
							if (!this.previousWasImage && !this.firstLine) {
								System.err
										.println("ERROR: shouldn't be coming here columnNumber = "
												+ columnNumber);
							}
						}
						String s = new String(data);
						s = s.replaceAll(",", "");
						System.out.printf("%s%s",
								(this.previousWasImage ? " Rs." : "\""), s);
						if (this.previousWasImage) {
							this.previousWasImage = false;
						}
						this.firstLine = false;
					}
				}
			}

			public void handleStartTag(Tag t, MutableAttributeSet attribute,
					int pos) {
				// System.out.println("received start tag: " + t.toString());
				if (!this.insideTd && t == Tag.TD) {
					this.insideTd = true;
					// System.out.println("inside td pos = " + pos);
					if (this.rowStart) {
						this.columnNumber = 0;
						this.rowStart = false;
					}
					this.columnNumber++;
				}
				if (t == Tag.TR && this.insideRequiredTable) {
					this.printData = true;
					this.rowStart = true;
				}
			}

			public void handleEndTag(Tag t, final int pos) {
				// System.out.println("received end tag: " + t.toString());
				if (t == Tag.TD && this.insideTd) {
					this.insideTd = false;
					// System.out.println("td ended pos = " + pos);
				}
				if (this.insideRequiredTable && t == Tag.TABLE) {
					this.insideRequiredTable = false;
					System.out.print("\"\n");
				}
			}

			public void handleSimpleTag(Tag t, MutableAttributeSet a,
					final int pos) {
				if (this.insideRequiredTable && this.printData && t == Tag.IMG) {
					this.previousWasImage = true;
				}
			}

			public void handleComment(final char[] data, final int pos) {
			}

			public void handleError(final java.lang.String errMsg, final int pos) {
			}
		};
		try {
			parserDelegator
					.parse(new FileReader(infile), parserCallback, false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
