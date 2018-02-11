package footsiebot.ai;

import java.lang.Comparable;
import java.util.ArrayList;

public class Company implements Comparable<Company> {

  private String code;
  private ArrayList<IntentData> intents;
  private float newsCounter;
  private float priority;
  private float irrelevantSuggestionWeight;


  public Company(String code, ArrayList<IntentData> intents, float newsCounter, float priority, float irrelevantSuggestionWeight) {
    this.code = code;
    this.intents = intents;
    this.newsCounter = newsCounter;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;

  }

  public float getIrrelevantSuggestionWeight() {
	return irrelevantSuggestionWeight;
  }

  public String getCode() {
	return code;
  }

  public IntentData getTopIntentData() {
    java.util.Collections.sort(intents);
	  return intents.get(0);
  }

  public float getNewsCount() {
	return newsCounter;
  }

  public float getPriority() {
	return priority;
  }

  public void incrementPriority(float increment) {
    priority+= increment;
  }

  public void decrementPriority(float decrement) {
    priority-= decrement;
  }

  @Override
  public int compareTo(Company c) {
    float r = c.getPriority() - this.getPriority();
    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }






}
