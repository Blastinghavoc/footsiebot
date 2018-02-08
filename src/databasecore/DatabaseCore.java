package footsiebot.databasecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.datagatheringcore.ScrapeResult;
import footsiebot.intelligencecore.*;
import java.time.LocalDateTime;

public class DatabaseCore implements IDatabaseManager {

  public boolean storeScraperResults(ScrapeResult sr) {
	return false;
  }

  public boolean storeQuery(ParseResult pr, LocalDateTime date) {
	return false;
  }

  public String[] getFTSE(ParseResult pr) {
	return null;
  }

  private String convertScrapeResult(ScrapeResult sr) {
	return null;
  }

  private String convertQuery(ParseResult pr, LocalDateTime date) {
	return null;
  }

  private String convertFTSEQuery(ParseResult pr) {
	return null;
  }

  private Company[] getAICompany() {
	return null;
  }

  private Company[] getAIGroup() {
	return null;
  }

  private IntentData getIntentForCompany() {
	return null;
  }

  private void storeAICompanies(Company[] companies) {

  }

  private void storeAIGroups(Group[] groups) {

  }



}
