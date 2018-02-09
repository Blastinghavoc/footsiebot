package footsiebot.intelligencecore;
import footsiebot.nlpcore.ParseResult;


public interface IIntelligenceUnit {
     public String getSuggestion(ParseResult pr);



   public String onUpdatedDatabase();

   public void onShutdown();
   /**
    * User has reported that a suggestion has not been relevant
    * ajust weights accordingly
    * @param  String companyOrGroup
    * @return
    */
   public String onSuggestionIrrelevant(String companyOrGroup);

   public String onNewsTime();



}
