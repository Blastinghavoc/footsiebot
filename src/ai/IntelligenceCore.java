package footsiebot.ai;

import footsiebot.nlp.ParseResult;
import footsiebot.nlp.Intent;
import footsiebot.nlp.TimeSpecifier;
import footsiebot.database.*;

import java.util.*;
import java.lang.*;

public class IntelligenceCore implements IIntelligenceUnit {

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

     if(companies == null){
         System.out.println("companies was null, cannot make suggestion");//DEBUG
         return null;
     }
     // intent detect
     AIIntent notToSuggestIntent = null;
     Intent oldIntent = pr.getIntent();
     System.out.println("User has just asked : " + oldIntent);
     boolean doNotSuggestNews = false;
     switch (oldIntent) {
       case SPOT_PRICE: notToSuggestIntent = AIIntent.SPOT_PRICE;
       break;
       case OPENING_PRICE : notToSuggestIntent = AIIntent.OPENING_PRICE;
       break;
       case CLOSING_PRICE : notToSuggestIntent = AIIntent.CLOSING_PRICE;
       break;
       case PERCENT_CHANGE : notToSuggestIntent = AIIntent.PERCENT_CHANGE;
       break;
       case ABSOLUTE_CHANGE : notToSuggestIntent = AIIntent.ABSOLUTE_CHANGE;
       break;
       case NEWS : doNotSuggestNews = true;
       break;
     }

     String companyOrGroup = pr.getOperand();
     Group targetGroup = null;
     Company targetCompany = null;
     // TODO: UPDATE TALLIES FOR THIS COMPANY LOCALLY
     // If operand is a GROUP
     if(pr.isOperandGroup()) {
         // DOES NOT MAKE SENSE TO SUGGEST FOR GROUPS
        return null;
     } else {
       // operand is a company
       for(Company c: companies) {
         if(c.getCode().equals(companyOrGroup)) {
           targetCompany = c;
           break;
         }
       }
       if(targetCompany == null) {
           System.out.println("No company found for suggestion making");//DEBUG
           return null;
        }

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
         // DECIDING WHETHER to suggest news
         float newsPriority =  targetCompany.getNewsPriority();

         System.out.println("Should not suggest " + notToSuggestIntent);

         AbstractMap.SimpleEntry<AIIntent,Float> topIntentData = targetCompany.getTopIntent(notToSuggestIntent);
         AIIntent topIntent = topIntentData.getKey();
         System.out.println("Top intent is : " + topIntent);
         Float topIntentPriority = topIntentData.getValue();

         if(topIntentPriority > newsPriority || doNotSuggestNews) {
           lastSuggestion = suggestIntent(targetCompany,topIntent);
         } else {
           System.out.println("Suggesting news for " + targetCompany.getCode());
           lastSuggestion = suggestNews(targetCompany);
         }
         return lastSuggestion;
         // return Group to Core
       } else {
           System.out.println("Decided not to make a suggestion");//DEBUG
         return null;
       }
     }
   }

   //TODO return a suggestion object
   public Suggestion[] onUpdatedDatabase(Float threshold) {
     companies = db.getAICompanies();
     groups = db.getAIGroups();
     // DEBUG
     if(companies == null) {
       return null;
     }
     Collections.sort(companies);
     if(groups == null) {
       // DEBUG
       System.out.println("GROUPS ARE NULL");
       return null;
     }
     Collections.sort(groups);
     // TODO treshold
     ArrayList<Company> changed = detectedImportantChange(threshold);
     if((changed == null ) || (changed.size() == 0)) return null;

     ArrayList<Suggestion> res = new ArrayList<>();

     for(Company c: changed) {
       // NOTE parseresult is null
       System.out.println("Company " + c.getCode() + "has had a significant change ");
       res.add(new Suggestion("Detected important change", c, false, null));
     }

     return res.toArray(new Suggestion[res.size()]);
   }

   public void onShutdown() {
     db.storeAICompanies(companies);
     db.storeAIGroups(groups);
   }

   public void onStartUp() {
     // Fetch from database
     companies = db.getAICompanies();//NOTE: may not be necessary if onNewDataAvailable is called on startup
     groups = db.getAIGroups();
     if(companies != null){
         Collections.sort(companies);
     }
     if(groups  != null){
         Collections.sort(groups);
     }
   }

   /**
    * User has reported that a suggestion has not been relevant
    * ajust weights accordingly
    * @param  String companyOrGroup
    * @return
    */

   public void onSuggestionIrrelevant(Suggestion s) {
      AIIntent intent;
      boolean isNews = s.isNews();
      // company
      if(s.getCompany() != null) {

        Company c = s.getCompany();
        ParseResult pr = s.getParseResult();

        switch(pr.getIntent()) {
          case SPOT_PRICE : intent = AIIntent.SPOT_PRICE;
          break;
          case OPENING_PRICE : intent = AIIntent.OPENING_PRICE;
          break;
          case CLOSING_PRICE : intent = AIIntent.CLOSING_PRICE;
          break;
          case PERCENT_CHANGE : intent = AIIntent.PERCENT_CHANGE;
          break;
          case ABSOLUTE_CHANGE : intent = AIIntent.ABSOLUTE_CHANGE;
          break;
          default : return;
        }
        System.out.println("Priority is "+ c.getPriority());
        c.decrementPriorityOfIntent(intent);
        db.onSuggestionIrrelevant(c, intent, isNews);
        System.out.println("Priority is now "+ c.getPriority());
      } else {
        //groups

      }

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

   public Group[] onNewsTimeGroups() {
     Group[] result = new Group[TOP];
     for(int i = 0; i < TOP; i++) {
       result[i] = groups.get(i);
     }
    return result;
   }


   private ArrayList<Company> detectedImportantChange(Float treshold) {      
     ArrayList<String> names = db.detectedImportantChange(treshold);
     if((names == null)||(names.size() == 0)) return null;

     ArrayList<Company> winningCompanies = new ArrayList<>();

     for(String s: names) {
       for(Company c: companies) {
         if(s.equals(c.getCode())) {
           winningCompanies.add(c);
         }
       }
     }

     return winningCompanies;
   }

   /**
    *
    * @param  Company company       [description]
    * @return         [description]
    */
   private Suggestion suggestIntent(Company company ,AIIntent topIntent) {
     String reason = "Company is in top 5";
     // String description = "Suggesting ";

     // Create IParseResult
     TimeSpecifier tm = footsiebot.nlp.TimeSpecifier.TODAY;
     if(topIntent == AIIntent.CLOSING_PRICE) {
       tm = footsiebot.nlp.TimeSpecifier.YESTERDAY;
     }

     Intent i = null;

     switch(topIntent) {
       case SPOT_PRICE : i = footsiebot.nlp.Intent.SPOT_PRICE;
       break;
       case OPENING_PRICE : i = footsiebot.nlp.Intent.OPENING_PRICE;
       break;
       case CLOSING_PRICE : i = footsiebot.nlp.Intent.CLOSING_PRICE;
       break;
       case PERCENT_CHANGE : i = footsiebot.nlp.Intent.PERCENT_CHANGE;
       break;
       case ABSOLUTE_CHANGE : i = footsiebot.nlp.Intent.ABSOLUTE_CHANGE;
       break;
     }

     if(i == null) return null;

     ParseResult pr = new ParseResult(i, "", company.getCode(), false, tm);

     // false == suggestion is not news
     Suggestion result = new Suggestion(reason, company, false, pr);
     return result;
   }

   private Suggestion suggestNews(Company company) {
     String reason = "Company is in top 5";
     ParseResult pr = new ParseResult(footsiebot.nlp.Intent.NEWS, "", company.getCode(), false, footsiebot.nlp.TimeSpecifier.TODAY);
     Suggestion result = new Suggestion(reason, company, true, pr);
     return result;
   }

   private Suggestion suggestNews(Group group) {
     String reason = "Group is in top 5";
     ParseResult pr = new ParseResult(footsiebot.nlp.Intent.NEWS, "", group.getGroupCode(), true,footsiebot.nlp.TimeSpecifier.TODAY );
     Suggestion result = new Suggestion(reason, group, pr );
     return result;
   }


}
