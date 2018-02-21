package footsiebot.ai;

import java.lang.Comparable;
import java.util.ArrayList;

public class Company implements Comparable<Company> {

  private String code;
  private ArrayList<IntentData> intents;
  private Float newsCounter;
  private Float priority;
  private Float irrelevantSuggestionWeight;


  public Company(String code, ArrayList<IntentData> intents, Float newsCounter, Float priority, Float irrelevantSuggestionWeight) {
    this.code = code;
    this.intents = intents;
    this.newsCounter = newsCounter;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;

  }
  //TOTEST 
  public void decrementPriorityOfIntent(AIIntent i) {
    for(IntentData id: intents) {
      if(id.getIntent().equals(i)) {
        id.decrementPriority(id.getIrrelevantSuggestionWeight());
      }
    }
  }

  public Float getIrrelevantSuggestionWeight() {
	return irrelevantSuggestionWeight;
  }

  public String getCode() {
	return code;
  }

  public IntentData getTopIntentData() {
    java.util.Collections.sort(intents);
	  return intents.get(0);
  }

  public Float getNewsCount() {
	   return newsCounter;
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

  @Override
  public int compareTo(Company c) {
    Float r = c.getPriority() - this.getPriority();
    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }






}
