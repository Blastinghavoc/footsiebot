package footsiebot.ai;

public class Suggestion {

  private Company company;
  private Group group;
  private boolean isNews;
  private String reason;
  private String description;


  public Suggestion(String r, Company c, boolean isNews) {
    reason = r;
    company = c;
    this.isNews = isNews;
  }
  // with description (for intents)
  public Suggestion(String r, Company c, boolean isNews, String desc) {
    reason = r;
    company = c;
    this.isNews = isNews;
    description = desc;
  }

  public Suggestion(String r, Group g) {
    reason = r;
    group = g;
    isNews = true;
  }

  public void setDesc(String d) {
    description = d;
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

  public String getDescription(){
      return description;
  }





}
