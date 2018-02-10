package footsiebot.intelligencecore;

import java.util.ArrayList;
import java.lang.Comparable;

public class Group implements Comparable {
  private ArrayList<Company> companies;
  private String code;
  private float priority;
  private float irrelevantSuggestionWeight;


  public Group(ArrayList<Company> companies, String code, float priority, float irrelevantSuggestionWeight) {
    this.companies = companies;
    this.code = code;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;

  }

  public String getGroupCode() {
	   return code;
  }

  public Float getPriority() {
	   return null;
  }

  public void incrementPriority(float increment) {
    priority+= increment;
  }

  public void decrementPriority(float decrements) {
    priority-= decrement;
  }

  public float getIrrelevantSuggestionWeight() {
    return irrelevantSuggestionWeight;
  }

  @Override
  public int compareTo(Group g) {
    return g.getPriority() - this.getPriority();
  }



}
