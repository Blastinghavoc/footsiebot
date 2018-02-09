package footsiebot.intelligencecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.nlpcore.Intent;
import footsiebot.databasecore.*;

import java.util.*;



public class IntelligenceCore implements IIntelligenceUnit {
  /**
   *
   */
   // To be possibly set by the user
   private int TOP = 5;
   private ArrayList<Company> companies = new ArrayList<>(100);
   private ArrayList<Group> groups = new ArrayList<>(41);
   private double startupHour;
   private Suggestion lastSuggestion;
   private DatabaseCore db;

   public IntelligenceCore(double startupHour, DatabaseCore db) {
     this.startupHour = startupHour;
     this.db = db;
   }


   public String getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority
     Intent intent = pr.getIntent();
     String companyOrGroup = pr.getOperand();
     Group targetGroup = null;
     Company targetCompany = null;

     if(pr.isOperandGroup()) {
       // search in groups
       for(Group g: group) {
         if(g.getGroupCode().equals(companyOrGroup)) {
           targetGroup = g;
           break;
         }
       }
       if(targetGroup == null) return "Error group not found";
       // for group only suggest news
       lastSuggestion = suggestNews(targetGroup);
       // return Group to Core


     } else {
       for(Company c: companies) {
         if(c.getCode().equals(companyOrGroup)) {
           targetCompany = c;
         }
       }
     }

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
     Collections.sort(companies);
     Collections.sort(groups);
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

   public Company[] onNewsTime() {
     // show report about 5 top companies
     // just returns the companies to core ?
     Company result = new Company[TOP];
     for(int i = 0; i < TOP; i++) {
       result[i] = companies.get(i);
     }
	   return result;
   }

   private boolean detectedImportantChange() {
	 return false;
   }

   private Suggestion suggestIntent(Company company) {
     // false == suggestion is not news
     Suggestion result = new Suggestion("Reason ?", company, false);
     


	 return null;
   }

   private Suggestion suggestNews(Company company) {
	return null;
   }

   private Suggestion suggestNews(Group group) {
     Suggestion result = new Suggestion("Reason ? ", group);
     return result;
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
