package footsiebot.nlpcore;

public class ParseResult implements IParseResult{

  private Intent in;

  private String raw;

  private String operand;

  private Boolean operandIsGroup;

  private TimeSpecifier ts;//Used to convey info such as "this morning" or "yesterday"

  public ParseResult(Intent i, String r, String o, Boolean oig, TimeSpecifier t){
    in = i;
    raw = r;
    operand = o;
    operandIsGroup = oig;
    ts = t;
  }

  public String toString(){
      String s = "Intent: '"+in+"' Operand: '"+operand+"' isGroup: '"+operandIsGroup+ "' Time: '"+ts+"'";
      return s;
  }

  public Intent getIntent(){return in;}

  public String getRaw(){return raw;}

  public String getOperand(){return operand;}

  public Boolean isOperandGroup(){return operandIsGroup;}

  public TimeSpecifier getTimeSpecifier(){return ts;}




}
