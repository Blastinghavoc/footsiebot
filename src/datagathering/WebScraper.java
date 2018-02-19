package footsiebot.datagathering;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.Float;
import java.util.ArrayList;

public class WebScraper {

    public WebScraper() {}

    public ScrapeResult scrape() {
        Document page;

        // try {
        //     page = Jsoup.connect("https://arcane-citadel-48781.herokuapp.com/").get();
        // } catch (IOException e) {
        //     e.printStackTrace();
        //     return null;
        // }

        // Elements entries = page.select("item").select("description");
        ArrayList<String> codelist = new ArrayList<String>();
        ArrayList<String> namelist = new ArrayList<String>();
        ArrayList<String> grouplist = new ArrayList<String>();
        ArrayList<Float> pricelist = new ArrayList<Float>();
        ArrayList<Float> abslist = new ArrayList<Float>();
        ArrayList<Float> perclist = new ArrayList<Float>();

        // int i = 0;
        // int j = 0;

        // for (Element entry : entries) {
        //     // System.out.println("entry " + i);
        //     // System.out.println(entry.text());
        //     String[] content = entry.text().split(",");
        //     codes[i] = content[j++];
        //     names[i] = content[j++];
        //     j++;
        //     groups[i] = "grouptest";
        //     if (content[j].contains(".")) prices[i] = Float.parseFloat(content[j++]);
        //     else {
        //         prices[i] = Float.parseFloat(content[j] + content[j+1]);
        //         j+=2;
        //     }
        //     absChange[i] = Float.parseFloat(content[j++]);
        //     percChange[i] = Float.parseFloat(content[j++]);
        //     j = 0;
        //     i++;
        // }

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
                            namelist.add("nametest");
                            grouplist.add("grouptest");
                            // Document summary;
                            // String surl;
                            // try {
                            //     summary = Jsoup.connect(surl).get();
                            // } catch (IOException e) {
                            //     e.printStackTrace();
                            //     return null;
                            // }
                            break;
                        case 3: break;
                        case 4: pricelist.add(Float.parseFloat(column.text().replace(",", "")));
                            break;
                        case 5: abslist.add(Float.parseFloat(column.text()));
                            break;
                        case 6: perclist.add(Float.parseFloat(column.text()));
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

        return new ScrapeResult(codes, names, groups, prices, absChange, percChange);
    }
}
