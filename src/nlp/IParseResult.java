package footsiebot.nlpcore;

public interface IParseResult{

  public Intent getIntent();

  public String getRaw();

  public String getOperand();

  public Boolean isOperandGroup();

  public TimeSpecifier getTimeSpecifier();

}
