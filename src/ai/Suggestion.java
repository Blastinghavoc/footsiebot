package footsiebot.ai;
import footsiebot.nlp.ParseResult;

public class Suggestion {

  private Company company;
  private Group group;
  private boolean isNews;
  private String reason;
  private ParseResult pr;

  /**
   * The sugegstion takes a string as the reason
   * a company for which the suggestion has to be made
   * a boolean to detect whether it is for news
   * and a parse result with information about the intent
   * @param  String      r
   * @param  Company     c
   * @param  boolean     isNews
   * @param  ParseResult pr
   * @return             [description]
   */
  public Suggestion(String r, Company c, boolean isNews, ParseResult pr) {
    reason = r;
    company = c;
    this.isNews = isNews;
    this.pr = pr;
  }

  public Suggestion(String r, Group g, ParseResult pr) {
    reason = r;
    group = g;
    isNews = true;
    this.pr = pr;
  }

  public Company getCompany() {
    return company;
  }

  public Group getGroup() {
    return group;
  }

  public boolean isNews() {
    return isNews;
  }

  public boolean isGroup() {
    if(group != null) return true;
    return false;
  }

  public String getReason() {
    return reason;
  }

  public ParseResult getParseResult(){
      return pr;
  }

}
