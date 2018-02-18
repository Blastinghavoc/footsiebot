package footsiebot.ai;
import footsiebot.nlp.ParseResult;

public class Suggestion {

  private Company company;
  private Group group;
  private boolean isNews;
  private String reason;
  private ParseResult pr;


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
  
  public void update() {

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

}
