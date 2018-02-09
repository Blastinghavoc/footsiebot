package footsiebot.intelligencecore;

public class Suggestion {

  private Company company;
  private Group group;
  private boolean isNews;
  private String reason;


  public Suggestion(String r, Company c, boolean isNews) {
    reason = r;
    company = c;
    this.isNews = isNews;
  }


  public Suggestion(String r, Group g) {
    reason = r;
    group = g;
    isNews = true;
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
    
  }



  public String getReason() {
    return reason;
  }





}
