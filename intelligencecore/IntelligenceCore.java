package footsiebot.intelligencecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.nlpcore.Intent;
import java.util.ArrayList;
import footsiebot.databasecore.*;



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
   private DatabaseCore db;

   public IntelligenceCore(double startupHour, DatabaseCore db) {
     this.startupHour = startupHour;
     this.db = db;
   }


   public String getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority

     // increment news counter if asked for news
	 return null;

   }

   public String onUpdatedDatabase() {
	return null;
   }

   public void onShutdown() {
     storeAICompanies(companies);
     storeAIGroups(groups);
   }

   public void onStartUp() {
     // Fetch from database
     companies = db.getAICompanies();
     groups = db.getAIGroups();
   }

   /**
    * User has reported that a suggestion has not been relevant
    * ajust weights accordingly
    * @param  String companyOrGroup
    * @return
    */
   public String onSuggestionIrrelevant(String companyOrGroup) {
     String alert = "";
     // check if it is a company or a group
     for(Company c: companies) {
       if(c.getCode().equals(companyOrGroup)) {
         c.decrementPriority(c.getIrrelevantSuggestionWeight());
         alert+= "Company " + companyOrGroup + " has been adjusted priority accordingly ";
         return alert;
       }
     }
     // is a group
     for(Group g: group) {
       if(g.getGroupCode().equals(companyOrGroup)) {
         g.decrementPriority(g.getIrrelevantSuggestionWeight());
         alert+= "Group " + companyOrGroup + "has been adjusted priority accordingly";
         return alert;
       }
     }

     return "Error, no company nor group matching found";
   }

   public String onNewsTime() {
     // show report about 5 top companies
     // just returns the companies to core ?
	   return null;
   }

   private boolean detectedImportantChange() {
	return false;
   }

   private Company[] getTopCompanies() {
	return null;
   }

   private Suggestion suggestIntent(Company company) {


	   return null;
   }

   private Suggestion suggestNews(Company company) {
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
