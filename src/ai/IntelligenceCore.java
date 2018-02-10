package footsiebot.intelligencecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.nlpcore.Intent;
import java.util.ArrayList;


public class IntelligenceCore implements IIntelligenceUnit {
  /**
   *
   */
   // To be possibly set by the user
   private int TOP = 5;
   private ArrayList<Company> companies = new ArrayList<>(100);
   private ArrayList<Group> groups = new ArrayList<>(41);
   private Company[] topCompanies = new Company[TOP];
   private double startupHour;
   private Suggestion lastSuggestion;



   public String getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority

     // increment news counter if asked for news
	 return null;

   }



   public String onUpdatedDatabase() {
	return null;
   }

   public void onShutdown() {

   }
   /**
    * User has reported that a suggestion has not been relevant
    * ajust weights accordingly
    * @param  String companyOrGroup
    * @return
    */
   public String onSuggestionIrrelevant(String companyOrGroup) {
     // check if it is a company or a group

     String desc = "For compilation only";
     for(Company c: companies) {
       if(c.getCode().equals(desc)) {
         c.decrementPriority(c.getIrrelevantSuggestionWeight());
       }
     }
	 return null;
   }

   public String onNewsTime() {
	return null;
   }

   private boolean detectedImportantChange() {
	return false;
   }

   private Company[] getTopCompanies() {
	return null;
   }

   private String suggestIntent(Company company) {
	return null;
   }

   private String suggestNews(Company company) {
	return null;
   }

   private String suggestNews(Group group) {
	return null;
   }

   private void createSuggestions(Company company) {

   }

   private void createSuggestions(Group group) {

   }

   private String createStartupReport() {
	return null;
   }

   private void updateLastSuggestion() {

   }










}
