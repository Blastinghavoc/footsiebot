package footsiebot.datagathering;

public interface IDataGathering {

  public ScrapeResult getData();

  public Article[] getNews(String company);

  public Article[] getNews(String[] company);


}
