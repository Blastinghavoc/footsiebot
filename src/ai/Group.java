package footsiebot.ai;

import java.util.ArrayList;
import java.lang.Comparable;

public class Group implements Comparable<Group> {
  private ArrayList<Company> companies;
  private String code;
  private Float priority;
  private Float irrelevantSuggestionWeight;


  public Group(String code) {
    this.code = code;
  }

  public Group(ArrayList<Company> companies, String code, Float priority, Float irrelevantSuggestionWeight) {
    this.companies = companies;
    this.code = code;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;

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

  public void setPriority(Float priority) {
    this.priority = priority;
  }

  public void setIrrelevantSuggestionWeight(Float irrelevantSuggestionWeights) {
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;
  }

  public Float getPriority() {
    return priority;
  }

  public void incrementPriority(Float increment) {
    priority+= increment;
  }

  public void decrementPriority(Float decrement) {
    priority-= decrement;
  }

  public Float getIrrelevantSuggestionWeight() {
    return irrelevantSuggestionWeight;
  }

  @Override
  public int compareTo(Group g) {
    Float r =  g.getPriority() - this.getPriority();

    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }



}
