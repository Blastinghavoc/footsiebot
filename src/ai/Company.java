package footsiebot.ai;

import java.lang.Comparable;
import java.util.*;

public class Company implements Comparable<Company> {

  private String code;
  private ArrayList<IntentData> intents;
  private HashMap<AIIntent,Float[]> mapping;
  private Float intentScale;
  private Float newsScale;

  private Float newsCount;
  private Float newsAdj;

  public Company(String code, ArrayList<IntentData> intents, HashMap<AIIntent,Float[]> mapping, Float intentScale, Float newsScale, Float newsCount, Float newsAdj) {
    this.code = code;
    this.intents = intents;
    this.mapping = mapping;
    this.intentScale = intentScale;
    this.newsScale = newsScale;
    this.newsCount = newsCount;
    this.newsAdj = newsAdj;
  }
  //TOTEST
  public void decrementPriorityOfIntent(AIIntent i) {
    for(IntentData id: intents) {
      if(id.getIntent().equals(i)) {
        // TODO
        id.decrementPriority(0.5f * (id.getAdjustment()) );
      }
    }
  }

  public String getCode() {
	return code;
  }

  public IntentData getTopIntentData() {
    java.util.Collections.sort(intents);
	  return intents.get(0);
  }

  public Float getPriority() {
    float spotPriority = mapping.get(AIIntent.SPOT_PRICE)[1] - mapping.get(AIIntent.SPOT_PRICE)[2] ;
    float openingPriority = mapping.get(AIIntent.OPENING_PRICE)[1] - mapping.get(AIIntent.OPENING_PRICE)[2] ;
    float closingPriority = mapping.get(AIIntent.CLOSING_PRICE)[1] - mapping.get(AIIntent.CLOSING_PRICE)[2] ;
    float absoluteChangePriority = mapping.get(AIIntent.ABSOLUTE_CHANGE)[1] - mapping.get(AIIntent.ABSOLUTE_CHANGE)[2] ;
    float percentageChangePriority = mapping.get(AIIntent.PERCENT_CHANGE)[1] - mapping.get(AIIntent.PERCENT_CHANGE)[2];

    float newsPriority = newsCount - newsAdj;

    return intentScale * (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority) + newsScale * (newsPriority);
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
