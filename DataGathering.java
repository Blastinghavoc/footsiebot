package footsiebot.datagatheringcore;

public interface DataGathering {
  
  public ScrapeResult getData();

  public String getNews(String company);

  public String getNews(String[] company);


}
