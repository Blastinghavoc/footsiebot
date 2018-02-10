package footsiebot.intelligencecore;

import footsiebot.nlpcore.Intent;

import java.lang.Comparable;


public class IntentData implements Comparable {

  private Intent intent;
  private double[] values;
  private float priority;
  private float irrelevantSuggestionWeight;

  public IntentData(Intent intent, double[] values, float priority, float irrelevantSuggestionWeight) {
    this.intent = intent;
    this.values = values;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;
  }


  public Intent getIntent() {
	return null;
  }

  public Float getPriority() {
	return null;
  }

  public void incrementPriority(Float p) {

  }

  public void decrementPriority(Float p) {

  }

  @Override
  public int compareTo(IntentData i) {
    return i.getPriority() - this.getPriority();
  }


}
