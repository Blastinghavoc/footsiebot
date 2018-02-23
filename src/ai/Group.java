package footsiebot.ai;

import java.util.ArrayList;
import java.lang.Comparable;

public class Group implements Comparable<Group> {
  private ArrayList<Company> companies;
  private String code;
  private Float groupCount;
  private Float adjustment;


  public Group(String code) {
    this.code = code;
  }

  public Group(ArrayList<Company> companies, String code, Float groupCount, Float adjustment) {
    this.companies = companies;
    this.code = code;
    this.groupCount = groupCount;
    this.adjustment = adjustment;

  }

  public void addCompanies(ArrayList<Company> companies) {
    this.companies = companies;
  }

  public ArrayList<Company> getCompanies() {
    return companies;
  }

  public String getGroupCode() {
	   return code;
  }

  public void setGroupCount(Float groupCount) {
    this.groupCount = groupCount;
  }

  public void setAdjustment(Float adjustments) {
    this.adjustment = adjustment;
  }

  public Float getGroupPriority() {
    return getGroupCount() - adjustment;
  }

  public Float getGroupCount() {
    return groupCount;
  }

  public Float getAdjustment() {
    return adjustment;
  }

  @Override
  public int compareTo(Group g) {
    Float r =  g.getGroupPriority() - this.getGroupPriority();

    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }



}
