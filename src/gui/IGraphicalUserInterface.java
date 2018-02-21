package footsiebot.gui;

import footsiebot.datagathering.Article;
import footsiebot.ai.Suggestion;

public interface IGraphicalUserInterface {

  public void displayMessage(String msg,Suggestion s);

  public void displayResults(Article[] news, Suggestion s);

}
