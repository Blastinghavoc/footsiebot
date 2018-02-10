package footsiebot.datagatheringcore;

public interface IDataGathering {

  public ScrapeResult getData();

  public Article[] getNews(String company);

  public Article[] getNews(String[] company);


}
