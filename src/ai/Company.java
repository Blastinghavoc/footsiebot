package footsiebot.ai;

import java.lang.Comparable;
import java.util.*;

public class Company implements Comparable<Company> {
  /**
   * Stores a unique company code
   * a mapping from intents to their counters and adjustments
   * stored in array of two floats
   * Also has counter and adjustmet for news
   */
  private String code;
  private HashMap<AIIntent,Float[]> mapping;
  private Float intentScale;
  private Float newsScale;

  private Float newsCount;
  private Float newsAdj;
  /**
   * Constructor for company object
   * @param  String                    code          unique identifier
   * @param  HashMap<AIIntent,Float[]> mapping       mapping from intents to their counters and adjustments
   * @param  Float                     intentScale   scalar for intent priority
   * @param  Float                     newsScale     scalar for news priority
   * @param  Float                     newsCount     counter for news
   * @param  Float                     newsAdj       counter for adjustments
   * @return                           [description]
   */
  public Company(String code, HashMap<AIIntent,Float[]> mapping, Float intentScale, Float newsScale, Float newsCount, Float newsAdj) {
    this.code = code;
    this.mapping = mapping;
    this.intentScale = intentScale;
    this.newsScale = newsScale;
    this.newsCount = newsCount;
    this.newsAdj = newsAdj;
  }
  /**
   * Decrements the priority of an intent
   * as a result of a suggestion being marked irrelevant
   * the decrement is exponential so that it is still effective
   * after a high number of times the intent has been queried
   * @param AIIntent i intent to be decremented
   * @param Boolean isNews Whether or not to decrement news.
   */
  public void decrementPriorityOfIntent(AIIntent i,Boolean isNews) {
    // if(isNews){
    //     newsAdj = 1.5f * newsAdj;
    //     return;
    // }
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
  /**
   * Gets the most important intent (currently) for this company
   * this should not be equal to the same intent that comes from the user query
   * @param  AIIntent notToSuggestIntent    intent that comes from the user query
   * @return          the top intent and its data
   */
  public AbstractMap.SimpleEntry<AIIntent,Float> getTopIntent(AIIntent notToSuggestIntent) {
    // get most requested intent
    Set<Map.Entry<AIIntent,Float[]>> entrySet = mapping.entrySet();

    AIIntent currMax = AIIntent.SPOT_PRICE;
    float startMaxValue = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1];

    AbstractMap.SimpleEntry<AIIntent,Float> result = new AbstractMap.SimpleEntry<AIIntent, Float>(currMax, startMaxValue);

    for(Map.Entry<AIIntent,Float[]> e: entrySet) {
      float value = e.getValue()[0] - e.getValue()[1];
      if(value > startMaxValue && e.getKey() != notToSuggestIntent) {
        currMax = e.getKey();
        startMaxValue = value;
        result = new AbstractMap.SimpleEntry<AIIntent, Float>(currMax,startMaxValue);
      }
    }
    return result;
  }
  /**
   * The total value of how many times the intents have been queried
   * @return
   */
  public float getIntentsCount() {
    float spotPriority = mapping.get(AIIntent.SPOT_PRICE)[0] - mapping.get(AIIntent.SPOT_PRICE)[1] ;
    float openingPriority = mapping.get(AIIntent.OPENING_PRICE)[0] - mapping.get(AIIntent.OPENING_PRICE)[1] ;
    float closingPriority = mapping.get(AIIntent.CLOSING_PRICE)[0] - mapping.get(AIIntent.CLOSING_PRICE)[1] ;
    float absoluteChangePriority = mapping.get(AIIntent.ABSOLUTE_CHANGE)[0] - mapping.get(AIIntent.ABSOLUTE_CHANGE)[1] ;
    float percentageChangePriority = mapping.get(AIIntent.PERCENT_CHANGE)[0] - mapping.get(AIIntent.PERCENT_CHANGE)[1];
    float trendPriority = mapping.get(AIIntent.TREND)[0] - mapping.get(AIIntent.TREND)[1];
    float volumePriority = mapping.get(AIIntent.TRADING_VOLUME)[0] - mapping.get(AIIntent.TRADING_VOLUME)[1];


    return (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority + trendPriority + volumePriority);

  }

  public float getIntentsPriority() {
    return getIntentsCount() * intentScale;
  }

  public Float getPriority() {
    return getIntentsPriority() + getNewsPriority();
  }
  /**
   * The total value of how many times the news have been queried
   * @return [description]
   */
  public Float getNewsCount() {
    float newsPriority = newsCount - newsAdj;
    return (newsPriority);
  }

  public Float getNewsPriority() {
    return getNewsCount() * newsScale;
  }
  /**
   * Method used for sorting companies by priority.
   * @param  Company c
   * @return
   */
  @Override
  public int compareTo(Company c) {
    //System.out.println(c.getCode() + " has priority "+ c.getPriority() + " . this has priority " + this.getPriority());
    if(c.getPriority() < this.getPriority()) {
        //System.out.println("this is greater");
      return 1;
  } else if (c.getPriority().equals(this.getPriority())) {
      //System.out.println("these are equal");
      return 0;
    } else {
        //System.out.println("this is less");
      return -1;
    }
  }

}
