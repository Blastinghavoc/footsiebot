package footsiebot.ai;

import java.lang.Comparable;
import java.lang.Float;
import java.util.ArrayList;


public class IntentData implements Comparable<IntentData> {

  private AIIntent intent;
  // This could be changed to a linked list if we only ever acces the last value
  private Float priority;
  private Float irrelevantSuggestionWeight;

  public IntentData(AIIntent intent, Float priority, Float irrelevantSuggestionWeight) {
    this.intent = intent;
    this.priority = priority;
    this.irrelevantSuggestionWeight = irrelevantSuggestionWeight;
  }


  public AIIntent getIntent() {
	   return intent;
  }

  // public Float getLastValue() {
  //   //Float res = 0.0f;
  //   Float f = values.get(values.size() - 1);
  //   //res = f.FloatValue();
  //   return f;//res;
  // }

  public Float getPriority() {
    return priority;
  }

  public void incrementPriority(Float p) {
    priority-= p;
  }

  public void decrementPriority(Float p) {
    priority-= p;
  }

  @Override
  public int compareTo(IntentData i) {
    Float r = i.getPriority() - this.getPriority();
    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }


}
