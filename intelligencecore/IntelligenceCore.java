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

   }

   public String onUpdatedDatabase() {

   }

   public void onShutdown() {

   }

   public void onStartUp() {
     // Fetch from database
     for(Company c: companies) {
       c =
     }
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

     }
   }

   public String onNewsTime() {

   }

   private boolean detectedImportantChange() {

   }

   private Company[] getTopCompanies() {

   }

   private String suggestIntent(Company company) {

   }

   private String suggestNews(Company company) {

   }

   private String suggestNews(Group group) {

   }

   private void createSuggestions(Company company) {

   }

   private void createSuggestions(Group group) {

   }

   private String createStartupReport() {

   }

   private void updateLastSuggestion() {

   }










}
