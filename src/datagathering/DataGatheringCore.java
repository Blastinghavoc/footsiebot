package footsiebot.datagathering;

/**
 * Gathers data required for the Footsiebot - FTSE 100 data and news articles about
 * companies.
 */
public class DataGatheringCore implements IDataGathering {
    private WebScraper webScraper;
    private NewsScraper newsScraper;
    
    /**
     * Constructor method that initialises the WebScraper and NewsScraper components.
     * @return a DataGatheringCore object.
     * @see    WebScraper
     * @see    NewsScraper
     */
    public DataGatheringCore(){
        webScraper = new WebScraper();
        newsScraper = new NewsScraper();
    }

    /**
     * Calls the WebScrapers scrape() method.
     * @return A ScrapeResult filled with FTSE 100 data.
     * @see    ScrapeResult
     * @see    WebScraper
     */
    public ScrapeResult getData() {
      return webScraper.scrape();  
    }  

    /**
     * Calls the NewsScrapers scrapeNews method for an individual
     * company.
     * @param company the name of the company to be searched for.
     * @return        An array of Article objects containing news
     *                article data.
     * @see           Article
     * @see           NewsScraper
     */
    public Article[] getNews(String company) {
      return newsScraper.scrapeNews(company);  
    }  

    /**
     * Calls the NewsScrapers scrapeNews() method for groups
     * of companies.
     * @param companies an array of companies to be searched for.
     * @return          An array of Article objects containing news
     *                  article data.
     * @see             Article
     * @see             NewsScraper
     */
    public Article[] getNews(String[] companies) {
      return newsScraper.scrapeNews(companies);  
    }  

}
