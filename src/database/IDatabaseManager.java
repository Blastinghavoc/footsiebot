package footsiebot.databasecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.datagatheringcore.ScrapeResult;
import footsiebot.intelligencecore.*;
import java.time.LocalDateTime;

public interface IDatabaseManager {

  public boolean storeScraperResults(ScrapeResult sr);

  public boolean storeQuery(ParseResult pr, LocalDateTime date);

  public String[] getFTSE(ParseResult pr);

}
