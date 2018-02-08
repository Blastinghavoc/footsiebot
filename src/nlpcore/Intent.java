package footsiebot.nlpcore;

public enum Intent{
  SPOT_PRICE,
  TRADING_VOLUME,
  OPENING_PRICE,
  CLOSING_PRICE,
  PERCENT_CHANGE,
  ABSOLUTE_CHANGE,
  TREND,//rising or falling
  //GROUP_TREND,//Are X rising or falling
  //Posibly don't need these two
  //GROUP_GET_RISING,//List those X that are rising
  //GROUP_GET_FALLING,

  GROUP_FULL_SUMMARY//Summary of group X

}
