package footsiebot.ai;

import java.lang.Comparable;
import java.util.*;

public class Company implements Comparable<Company> {

  private String code;
  private HashMap<AIIntent,Float[]> mapping;
  private Float intentScale;
  private Float newsScale;

  private Float newsCount;
  private Float newsAdj;

  public Company(String code, HashMap<AIIntent,Float[]> mapping, Float intentScale, Float newsScale, Float newsCount, Float newsAdj) {
    this.code = code;
    this.mapping = mapping;
    this.intentScale = intentScale;
    this.newsScale = newsScale;
    this.newsCount = newsCount;
    this.newsAdj = newsAdj;

  }
  //TOTEST
  public void decrementPriorityOfIntent(AIIntent i) {

    for(Map.Entry<AIIntent,Float[]> e: mapping.entrySet()) {
      if(e.getKey().equals(i)) {
        Float[] current = mapping.get(i);
        float counter = current[0];
        float adjustment = current[1];
        mapping.put(i, new Float[]{counter, 1.0f + 0.5f * adjustment});
      }

    }

  }

  public String getCode() {
	return code;
  }

  public AbstractMap.SimpleEntry<AIIntent,Float> getTopIntent(AIIntent notToSuggestIntent) {
    // get most requested intent
    Set<Map.Entry<AIIntent,Float[]>> entrySet = mapping.entrySet();

    AIIntent currMax = AIIntent.SPOT_PRICE;
    float startMaxValue = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1];
    //DEBUG
    System.out.println(startMaxValue);

    AbstractMap.SimpleEntry<AIIntent,Float> result = new AbstractMap.SimpleEntry<AIIntent, Float>(currMax, startMaxValue);

    for(Map.Entry<AIIntent,Float[]> e: entrySet) {
      float value = e.getValue()[0] - e.getValue()[1];
      if(value > startMaxValue && e.getKey() != notToSuggestIntent) {
        currMax = e.getKey();
        startMaxValue = value;
        result = new AbstractMap.SimpleEntry<AIIntent, Float>(currMax,startMaxValue);
      }
      // DEBUG
      System.out.println(result.getKey());
      System.out.println(result.getValue());
    }
    return result;
  }

  public float getIntentsCount() {
    float spotPriority = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1] ;
    float openingPriority = mapping.get(AIIntent.OPENING_PRICE)[0] - mapping.get(AIIntent.OPENING_PRICE)[1] ;
    float closingPriority = mapping.get(AIIntent.CLOSING_PRICE)[0] - mapping.get(AIIntent.CLOSING_PRICE)[1] ;
    float absoluteChangePriority = mapping.get(AIIntent.ABSOLUTE_CHANGE)[0] - mapping.get(AIIntent.ABSOLUTE_CHANGE)[1] ;
    float percentageChangePriority = mapping.get(AIIntent.PERCENT_CHANGE)[0] - mapping.get(AIIntent.PERCENT_CHANGE)[1];

    return (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority);

  }

  public float getIntentsPriority() {
    return getIntentsCount() * intentScale;
  }

  public Float getPriority() {
    return getIntentsPriority() + getNewsPriority();
  }

  public Float getNewsCount() {
    float newsPriority = newsCount - newsAdj;
    return (newsPriority);
  }

  public Float getNewsPriority() {
    return getNewsCount() * newsScale;
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
