package footsiebot.intelligencecore;

import footsiebot.nlpcore.Intent;
import java.lang.Comparable;

public class Company implements Comparable {

  private String code;
  private ArrayList<IntentData> intents;
  private float newsCounter;
  private float priority;
  private Double irrelevantSuggestionWeight;


  public Company(String code, ArrayList<IntentData> intents, ) {

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
	return null;
  }

  public void incrementPriority(float increment) {
    priority+= increment;
  }

  public void decrementPriority(float decrements) {
    priority-= decrement;
  }

  @Override
  public int compareTo(Company c) {
    return c.getPriority() - this.getPriority();
  }






}
