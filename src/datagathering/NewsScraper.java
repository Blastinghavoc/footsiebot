/* SOME NOTES
‣ This assumes the "company" argument for the scrapeNews method is the comapany nametag, not the company name
‣ This currently uses the "description" element of each article as the digest - this has worked so far
  in testing but the Yahoo! website states that "Many news items include an empty description element."
  so this might not be robust enough for the finished product.
‣ Each news article has a "pubDate" element so it is possible to filter out news articles that are deemed
  'too old' by some arbitrary age. This could be useful if the user asks for articles from a specific time
  period, e.g. "Get me news articles on AMD from the last week".
  pubDate is ignored at the moment but is something to keep in mind if we want to implement additional features
‣ When finding news for multiple companies, it just uses an RSS feed for all companies, which works for the
  most part but cannot guarantee that all companies specified will be included (for example, one of the
  companies asked for might not have been in the news for a very long time and thus won't be included).
  This can be changed quite easily if needs be.
  From the Yahoo! website: "Note that the feed simply returns the most recent 25 news items for all the
  ticker symbols in the request; it does not distinguish which item goes with which company."
‣ The array returned is not of a set length, it is simply as long as however many articles it finds (at most 20)
‣ This assumes that every <item> element contains all elements listed on the Yahoo! website, and consistently in
  the same order. This should always be the case, so I haven't performed any kind of validation and it will
  most likely break if there are any inconsistencies. I can easily add validation if needs be.
*/





package footsiebot.datagathering;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.time.*;

public class NewsScraper {

	public NewsScraper() {

	}

	public Article[] scrapeNews(String company) {
		//Get URL for the RSS feed specific for that company
		String url = "https://feeds.finance.yahoo.com/rss/2.0/headline?s=" + company+".L";//NOTE: appending .L to signify that the ticker used is the LSE meaning of the ticker
		return scrape(url);
	}

	public Article[] scrapeNews(String[] company) {
		//Get URL for the RSS feed specific for those companies
		String url = "https://feeds.finance.yahoo.com/rss/2.0/headline?s=";
		for (int i=0; i<company.length - 1; i++) {
			url = url + company[i] + ".L,";
		}
		url = url + company[company.length - 1]+".L";
		return scrape(url);
	}


	private Article[] scrape(String url) {

		String articleHeadline;
		String articleURL;
		String articleDigest;
		LocalDateTime articleDateTime;
		int startPos;
		int endPos;
		String temp;
		
		//ArrayList is used for ease
		ArrayList<Article> articles = new ArrayList<Article>(1);

		URL rssURL;

		try {
			rssURL = new URL(url);
		} catch(MalformedURLException e) {
			System.err.println("URL was malformed");
			return null;
		}

		try {

			//Open the reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(rssURL.openStream()));

			/*
			The result of the Yahoo! RSS feed is as follows:
			A series of <item>s, containing:
				<description> - A description of the article
				<guid> - Unique identifier for the item
				<link> - The link to the article
				<pubDate> - The date and time this news item was posted
				<title> - The title of the article
			*/


			String line;
			//articleDetails is a string containing all elements of the article
			String articleDetails;
			while((line = reader.readLine()) !=null) {
				//Get the item
				if (line.contains("<item>")) {

					articleDetails = "";

					while(line.contains("</item>") == false) {
						line = reader.readLine();
						articleDetails += line;
					}

					//GET DESCRIPTION
					startPos = articleDetails.indexOf("<description>") + 13;
					endPos = articleDetails.indexOf("</description>");
					temp = articleDetails.substring(startPos, endPos);
					temp = StringUtils.unescapeHTML(temp);
					articleDigest = temp.trim();


					//GET LINK
					startPos = articleDetails.indexOf("<link>") + 6;
					endPos = articleDetails.indexOf("</link>");
					temp = articleDetails.substring(startPos, endPos);
					articleURL = temp.trim();


					//GET PUBDATE
					startPos = articleDetails.indexOf("<pubDate>") + 9;
					endPos = articleDetails.indexOf("</pubDate>");
					temp = articleDetails.substring(startPos, endPos);
					articleDateTime = stringToDateTime(temp.trim());


					//GET TITLE
					startPos = articleDetails.indexOf("<title>") + 7;
					endPos = articleDetails.indexOf("</title>");
					temp = articleDetails.substring(startPos, endPos);
					temp = StringUtils.unescapeHTML(temp);
					articleHeadline = temp.trim();
					
					//Add new article to the ArrayList
					articles.add(new Article(articleHeadline, articleURL, articleDigest, articleDateTime));

				}
			}

			//Close the reader
			reader.close();



		} catch (IOException e) {
    		System.err.println("Caught IOException: " + e.getMessage());
    		return null;
		}

		return articles.toArray(new Article[0]);
	}

	private LocalDateTime stringToDateTime(String input) {
		int year;
		int month = 0;
		int dayOfMonth;
		int hour;
		int minute;
		int second;
		long hourchange;
		long minutechange;

		//Remove day of week
		input = input.substring(5);

		//Get day of month
		dayOfMonth = Integer.parseInt(input.substring(0,2));
		//Get month
		switch (input.substring(3,6)) {
			case "Jan" :
				month = 1;
				break;
			case "Feb" :
				month = 2;
				break;
			case "Mar" :
				month = 3;
				break;
			case "Apr" :
				month = 4;
				break;
			case "May" :
				month = 5;
				break;
			case "Jun" :
				month = 6;
				break;
			case "Jul" :
				month = 7;
				break;
			case "Aug" :
				month = 8;
				break;
			case "Sep" :
				month = 9;
				break;
			case "Oct" :
				month = 10;
				break;
			case "Nov" :
				month = 11;
				break;
			case "Dec" :
				month = 12;
				break;
			default:
				month = 0;
		}
		//Get year
		year = Integer.parseInt(input.substring(7,11));
		//Get hour
		hour = Integer.parseInt(input.substring(12,14));
		//Get minute
		minute = Integer.parseInt(input.substring(15,17));
		//Get second
		second = Integer.parseInt(input.substring(18,20));
		//Get hour change
		hourchange = (long) Integer.parseInt(input.substring(22,24));
		//Get minute change
		minutechange = (long) Integer.parseInt(input.substring(24,26));

		//Create LocalDateTime with the values just obtained
		LocalDateTime temp = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);


		/*
		Change the LocalDateTime to account for difference in time zones.
		I can't think of any case where the minute would change but it is
		included just to make it more robust
		*/
		if (input.substring(21,22) == "+") {

			temp = temp.plusHours(hourchange);
			temp = temp.plusMinutes(minutechange);

		} else {

			temp = temp.minusHours(hourchange);
			temp = temp.minusMinutes(minutechange);

		}

		return temp;
	}

}