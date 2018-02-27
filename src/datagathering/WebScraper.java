package footsiebot.datagathering;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.Float;
import java.lang.Integer;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Accesses the LSE website to extract FTSE 100 data.
 */
public class WebScraper {

    /**
     * Constructor method for Webscraper.
     * @return A Webscraper object.
     */
    public WebScraper() {}

    /**
     * "Scrapes" the LSE website for FTSE 100 data.
     * @return A ScrapeResult object occupied with up to date FTSE 100 data.
     * @see    ScrapeResult
     */
    public ScrapeResult scrape() {
        Document page;
        ArrayList<String> codelist = new ArrayList<String>();
        ArrayList<String> namelist = new ArrayList<String>();
        ArrayList<String> grouplist = new ArrayList<String>();
        ArrayList<Float> pricelist = new ArrayList<Float>();
        ArrayList<Float> abslist = new ArrayList<Float>();
        ArrayList<Float> perclist = new ArrayList<Float>();
        ArrayList<Integer> vollist = new ArrayList<Integer>();

        String url = "http://www.londonstockexchange.com/exchange/prices-and-markets/stocks/indices/summary/summary-indices-constituents.html?index=UKX&page=";

        // for each possible LSE summary page
        for (int i = 1; i < 7; i++) {
            // checks if webscraper thread has been interrupted
            if (Thread.interrupted() || Thread.currentThread().isInterrupted() || Thread.currentThread().getName().equals("closing")) {
                System.out.println("Scraper interrupted");
                return null;
            }

            // attempts connection
            try {
                page = Jsoup.connect(url + i).timeout(60000).get();
            } catch (IOException e) {
                System.out.println("It appears something's gone wrong with the internet connection.");
                e.printStackTrace();
                return null;
            }

            // grabs summary table contents, continuing to the next loop if there is no
            // table. (should only happen on last page, if ever)
            Elements entries = page.select(".table_dati tbody tr");
            if (entries == null) continue;

            // for each table entry
            for (Element entry : entries) {
                // checks if webscraper thread has been interrupted
                if (Thread.interrupted() || Thread.currentThread().isInterrupted() || Thread.currentThread().getName().equals("closing")) {
                    System.out.println("Scraper interrupted");
                    return null;
                }

                // seperates each row into its columns
                Elements columns = entry.select("td");
                // integer variable used to keep track of column number.
                int j = 1;

                // for each column (up to the percentage change column)
                elementloop: for (Element column : columns) {
                    // checks if webscraper thread has been interrupted
                    if (Thread.interrupted() || Thread.currentThread().isInterrupted() || Thread.currentThread().getName().equals("closing")) {
                        System.out.println("Scraper interrupted");
                        return null;
                    }
                    // extracts stored information and stores it into
                    // the relevant ArrayList
                    switch (j) {
                        case 1:
                            // extracts company code
                            codelist.add(column.text());
                            break;
                        case 2:
                            // attempts connection to the companies summary page.
                            Document summary;
                            Element current;
                            String surl = column.select("a").first().attr("abs:href");
                            try {
                                summary = Jsoup.connect(surl).timeout(60000).get();
                            } catch (IOException e) {
                                System.out.println("It appears something's gone wrong with the internet connection.");
                                e.printStackTrace();
                                return null;
                            }

                            // extracts the company name, removing all surplus information
                            current = summary.select(".tesummary").first();
                            if (current == null) namelist.add("NAME N/A");
                            else {
                                String nametext = current.ownText();
                                int index = nametext.indexOf(" ORD");
                                if (index == -1) {
                                    index = nametext.indexOf(" $");
                                }
                                if (index == -1) {
                                    namelist.add(nametext);
                                } else {
                                    namelist.add(nametext.substring(0, index));
                                }
                            }

                            // extracts the sector name
                            current = summary.select("td:contains(sector) ~ td").first();
                            if (current == null) grouplist.add("GROUP N/A");
                            else grouplist.add(current.text());

                            // extracts the trading volume
                            current = summary.select("td:contains(volume) ~ td").first();
                            if (current == null) vollist.add(null);
                            else vollist.add(Integer.parseInt(clean(current.text())));
                            break;
                        case 3: break;
                        case 4: 
                            // Extracts spot price
                            pricelist.add(Float.parseFloat(clean(column.text())));
                            break;
                        case 5: 
                            // Extracts absolute change
                            abslist.add(Float.parseFloat(clean(column.ownText())));
                            break;
                        case 6: 
                            // Extracts percentage change                        
                            perclist.add(Float.parseFloat(clean(column.ownText())));
                            break;
                        default: break elementloop;
                    }
                    j++;
                }
            }
        }

        // converts arraylists to arrays.
        String[] codes = codelist.toArray(new String[0]);
        String[] names = namelist.toArray(new String[0]);
        String[] groups = grouplist.toArray(new String[0]);
        Float[] prices = pricelist.toArray(new Float[0]);
        Float[] absChange = abslist.toArray(new Float[0]);
        Float[] percChange = perclist.toArray(new Float[0]);
        Integer[] tradeVolume = vollist.toArray(new Integer[0]);

        // returns an occupied scraperesult
        return new ScrapeResult(codes, names, groups, prices, absChange, percChange, tradeVolume);
    }

    /**
     * "Cleans" an input string of html tags, commas and trailing whitespace.
     * @param input A input string that may contain invalid characters or 
     *              trailing whitespace.
     * @return      The input string with the invalid characters and/or 
     *              trailing whitespace removed.
     */
    public String clean(String input) {
        return input.replaceAll("<.*?>", "").replaceAll(",", "").trim();
    }
}
