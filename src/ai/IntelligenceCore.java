package footsiebot.intelligencecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.nlpcore.Intent;
import footsiebot.databasecore.*;

import java.util.*;
import java.lang.*;


public class IntelligenceCore implements IIntelligenceUnit {
  /**
   *
   */

   // To be possibly set by the user
   private byte TOP = 5;
   private ArrayList<Company> companies;
   private ArrayList<Group> groups;
   private double startupHour;
   private Suggestion lastSuggestion;
   private IDatabaseManager db;

   public IntelligenceCore(IDatabaseManager db) {
     this.db = db;
     onStartUp();
   }


   public Suggestion getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority
     // TODO needs converting to AIIntent

     if(companies == null || groups == null){
         return null;
     }

     String companyOrGroup = pr.getOperand();
     Group targetGroup = null;
     Company targetCompany = null;
     // TODO
     UPDATE TALLIES FOR THIS COMPANY LOCALLY
     // If operand is a group
     if(pr.isOperandGroup()) {
       // search in groups if valid group
       for(Group g: groups) {
         if(g.getGroupCode().equals(companyOrGroup)) {
           targetGroup = g;
           break;
         }
       }
       // if error will return null
       if(targetGroup == null) return null;
       // for group only suggest news
       boolean doSuggestion = false;
       // check if group is in top 5
       for(int i = 0; i < TOP; i++) {
         if(targetGroup.equals(groups.get(i))) {
           doSuggestion = true;
         }
       }
       if(doSuggestion) {
         lastSuggestion = suggestNews(targetGroup);
         return lastSuggestion;
         // return Group to Core
       } else {
         return null;
       }
     } else {
       // operand is a company
       for(Company c: companies) {
         if(c.getCode().equals(companyOrGroup)) {
           targetCompany = c;
           break;
         }
       }
       if(targetCompany == null) return null;
       boolean doSuggestion = false;
       for(int i = 0; i < TOP; i++) {
         if(targetCompany.equals(companies.get(i))) {
           doSuggestion = true;
         }
       }

       if(doSuggestion) {
         // This will need to be modified as
         // it just suggests an intent now
         // but could decide to suggest news
         lastSuggestion = suggestIntent(targetCompany);
         return lastSuggestion;
         // return Group to Core
       } else {
         return null;
       }
     }
   }

   public String onUpdatedDatabase() {
     companies = db.getAICompanies();
     groups = db.getAIGroups();
     // DEBUG
     if(companies == null || groups == null ) return "ERROR";
     Collections.sort(companies);
     Collections.sort(groups);
     // What to return here ?
     return "";
   }

   public void onShutdown() {
     db.storeAICompanies(companies);
     db.storeAIGroups(groups);
   }

   public void onStartUp() {
     // Fetch from database
     companies = db.getAICompanies();
     groups = db.getAIGroups();
     if((groups != null) && (companies != null)){
         Collections.sort(companies);
         Collections.sort(groups);
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
     for(Group g: groups) {
       if(g.getGroupCode().equals(companyOrGroup)) {
         g.decrementPriority(g.getIrrelevantSuggestionWeight());
         alert+= "Group " + companyOrGroup + "has been adjusted priority accordingly";
         return alert;
       }
     }

     return "Error, no company nor group matching found";
   }

   /**
    *
    * @return [description]
    */
   public Company[] onNewsTime() {
     // show report about 5 top companies
     // just returns the companies to core ?
     Company[] result = new Company[TOP];
     for(int i = 0; i < TOP; i++) {
       result[i] = companies.get(i);
     }
	   return result;
   }

   // TODO
   private boolean detectedImportantChange() {
	 return false;
   }

   /**
    *
    * @param  Company company       [description]
    * @return         [description]
    */
   private Suggestion suggestIntent(Company company) {
     String reason = "Company is in top 5";
     String description = "Suggesting ";

     IntentData topIntent = company.getTopIntentData();
     Float topIntentValue = topIntent.getLastValue();

     description += topIntent.toString() + "has value " + topIntentValue;
     // false == suggestion is not news
     Suggestion result = new Suggestion(reason, company, false, description);
     return result;
   }

   private Suggestion suggestNews(Company company) {
     String reason = "Company is in top 5";
     Suggestion result = new Suggestion(reason, company, true);
     return result;
   }

   private Suggestion suggestNews(Group group) {
     String reason = "Group is in top 5";
     Suggestion result = new Suggestion(reason, group);
     return result;
   }

   private void updateLastSuggestion() {

   }










}
