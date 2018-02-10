package footsiebot.intelligencecore;

import footsiebot.nlpcore.Intent;

import java.lang.Comparable;
import java.lang.Float;
import java.util.ArrayList;


public class IntentData implements Comparable<IntentData> {

  private Intent intent;
  // This could be changed to a linked list if we only ever acces the last value
  private ArrayList<Float> values;
  private float priority;
  private float irrelevantSuggestionWeight;

  public IntentData(Intent intent, ArrayList<Float> values, float priority, float irrelevantSuggestionWeight) {
    this.intent = intent;
    this.values = values;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;
  }


  public Intent getIntent() {
	return null;
  }

  public ArrayList<Float> getValues() {
    return values;
  }

  public float getLastValue() {
    float res = 0.0f;
    Float f = values.get(values.size() - 1);
    res = f.floatValue();
    return res;
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
    float r = i.getPriority() - this.getPriority();
    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }


}
