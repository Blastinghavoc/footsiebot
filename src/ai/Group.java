package footsiebot.ai;

import java.util.ArrayList;
import java.lang.Comparable;

public class Group implements Comparable<Group> {
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

  public void decrementPriority(float decrement) {
    priority-= decrement;
  }

  public float getIrrelevantSuggestionWeight() {
    return irrelevantSuggestionWeight;
  }

  @Override
  public int compareTo(Group g) {
    float r =  g.getPriority() - this.getPriority();

    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }



}
