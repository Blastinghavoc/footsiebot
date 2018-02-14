package footsiebot.datagathering;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.lang.Float;

public class WebScraper {

    public WebScraper() {}

    public ScrapeResult scrape() {
        Document page;

        try {
            page = Jsoup.connect("https://arcane-citadel-48781.herokuapp.com/").get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Elements entries = page.select("item").select("description");
        String[] codes = new String[101];
        String[] names = new String[101];
        String[] groups = new String[101];
        Float[] prices = new Float[101];
        Float[] absChange = new Float[101];
        Float[] percChange = new Float[101];
        int i = 0;
        int j = 0;

        for (Element entry : entries) {
            // System.out.println("entry " + i);
            // System.out.println(entry.text());
            String[] content = entry.text().split(",");
            codes[i] = content[j++];
            names[i] = content[j++];
            j++;
            groups[i] = "grouptest";
            if (content[j].contains(".")) prices[i] = Float.parseFloat(content[j++]);
            else {
                prices[i] = Float.parseFloat(content[j] + content[j+1]);
                j+=2;
            }
            absChange[i] = Float.parseFloat(content[j++]);
            percChange[i] = Float.parseFloat(content[j++]);
            j = 0;
            i++;
        }

        return new ScrapeResult(codes, names, groups, prices, absChange, percChange);
    }
}
