package footsiebot.datagathering;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.Float;
import java.lang.Integer;
import java.util.ArrayList;

public class WebScraper {

    public WebScraper() {}

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
        for (int i = 1; i < 7; i++) {
            try {
                page = Jsoup.connect(url + i).get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Elements entries = page.select(".table_dati tbody tr");
            if (entries == null) continue;

            for (Element entry : entries) {
                Elements columns = entry.select("td");
                int j = 1;
                elementloop: for (Element column : columns) {
                    switch (j) {
                        case 1: codelist.add(column.text());
                            break;
                        case 2:

                            // namelist.add("nametest");
                            // grouplist.add("grouptest");
                            // vollist.add(null);
                            Document summary;
                            String surl = column.select("a").first().attr("abs:href");
                            // String name, group, volume; 
                            try {
                                summary = Jsoup.connect(surl).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                            String nametext = summary.select(".tesummary").first().ownText();
                            int index = nametext.indexOf(" ORD");
                            if (index == -1) {
                                index = nametext.indexOf(" $");
                            }
                            if (index == -1) {
                                namelist.add(nametext);
                                // name = nametext;
                            } else {
                                // name = nametext.substring(0, index);
                                namelist.add(nametext.substring(0, index));
                            }

                            // group = summary.select("td:contains(sector) ~ td").first().text();
                            // volume = summary.select("td:contains(volume) ~ td").first().text().replace(",", "");
                            grouplist.add(summary.select("td:contains(sector) ~ td").first().text());
                            vollist.add(Integer.parseInt(summary.select("td:contains(volume) ~ td").first().text().replace(",", "")));
                            // System.out.println("name: " + name);
                            // System.out.println("group: " + group);
                            // System.out.println("trading volume: " + volume);                            

                            break;
                        case 3: break;
                        case 4: pricelist.add(Float.parseFloat(column.text().replace(",", "")));
                            break;
                        case 5: abslist.add(Float.parseFloat(column.ownText()));
                            break;
                        case 6: perclist.add(Float.parseFloat(column.ownText()));
                            break;
                        default: break elementloop;
                    }
                    j++;
                }
            }
        }

        String[] codes = codelist.toArray(new String[0]);
        String[] names = namelist.toArray(new String[0]);
        String[] groups = grouplist.toArray(new String[0]);
        Float[] prices = pricelist.toArray(new Float[0]);
        Float[] absChange = abslist.toArray(new Float[0]);
        Float[] percChange = perclist.toArray(new Float[0]);
        Integer[] tradeVolume = vollist.toArray(new Integer[0]);

        return new ScrapeResult(codes, names, groups, prices, absChange, percChange, tradeVolume);
    }
}
