package footsiebot.intelligencecore;


public class IntelligenceCore implements IntelligenceUnit {
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


   /*

    */
   public static void main(String[] args) {


     onUpdatedDatabase();

   }

   public String getSuggestion(ParseResult pr) {
     // Fetch operand and intent and increment intent priority

     // increment news counter if asked for news

   }



   public String onUpdatedDatabase() {

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
     for(Company c: companies) {
       if(c.getCode().equals(desc)) {
         c.decrementPriority(c.getIrrelevantSuggestionWeight());
       }
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

   private updateLastSuggestion() {

   }










}
