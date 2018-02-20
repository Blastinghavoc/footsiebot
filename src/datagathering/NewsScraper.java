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

public class NewsScraper {

	public NewsScraper() {

	}

	public Article[] scrapeNews(String company) {
		//Get URL for the RSS feed specific for that company
		String url = "https://feeds.finance.yahoo.com/rss/2.0/headline?s=" + company+".L";//NOTE: appending .L to signify that the ticker used is the LSE meaning of the ticker
		System.out.println(url);//DEBUG
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
			while((line = reader.readLine()) !=null) {
				//Get the item
				if (line.contains("<item>")) {
					//GET DESCRIPTION
					line = reader.readLine();
					temp = line;
					temp = temp.replace("<description>","");
					temp = temp.replace("</description>","");
					if (temp.contains("&amp;apos;")) {
						temp = temp.replace("&amp;apos;","'");
					}
					articleDigest = temp;

					//GET GUID
					line = reader.readLine();

					//GET LINK
					line = reader.readLine();
					temp = line;
					temp = temp.replace("<link>","");
					temp = temp.replace("</link>","");
					articleURL = temp;

					//GET PUBDATE
					line = reader.readLine();

					//GET TITLE
					line = reader.readLine();
					temp = line;
					temp = temp.replace("<title>","");
					temp = temp.replace("</title>","");
					if (temp.contains("&amp;apos;")) {
						temp = temp.replace("&amp;apos;","'");
					}
					articleHeadline = temp;

					//Add new article to the ArrayList
					articles.add(new Article(articleHeadline, articleURL, articleDigest));
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

}
