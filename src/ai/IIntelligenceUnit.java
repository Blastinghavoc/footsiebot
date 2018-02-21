package footsiebot.ai;
import footsiebot.nlp.ParseResult;


public interface IIntelligenceUnit {



   public Suggestion getSuggestion(ParseResult pr);

   public String onUpdatedDatabase();

   public void onShutdown();

   public void onSuggestionIrrelevant(Suggestion suggestion);

   public Company[] onNewsTime();



}
