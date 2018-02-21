package footsiebot.ai;
import footsiebot.nlp.ParseResult;


public interface IIntelligenceUnit {



   public Suggestion getSuggestion(ParseResult pr);

   public Suggestion[] onUpdatedDatabase();

   public void onShutdown();
   /**
    * User has reported that a suggestion has not been relevant
    * ajust weights accordingly
    * @param  String companyOrGroup
    * @return
    */
   public void onSuggestionIrrelevant(Suggestion suggestion);

   public Company[] onNewsTime();



}
