package footsiebot.databasecore;


public interface DatabaseManager {

  public boolean storeScraperResults(ScrapeResult sr);

  public boolean storeQuery(ParseResult pr, DateTime date);

  public String[] getFTSE(ParseResult pr);

}
