package footsiebot.intelligencecore;

import java.util.ArrayList;

public class Group {
  private ArrayList<Company> companies;
  private String code;
  private float priority;
  private float irrelevantSuggestionWeight;


  public Group() {

  }

  public String getGroupCode() {
	   return code;
  }

  public Float getPriority() {
	   return null;
  }

  public void incrementPriority(float p) {

  }

  public void decrementPriority(float p) {

  }

  public float getIrrelevantSuggestionWeight() {
    return irrelevantSuggestionWeight;
  }




}
