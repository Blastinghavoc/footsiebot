package footsiebot.gui;

import footsiebot.datagathering.Article;

public interface IGraphicalUserInterface {

  public void displayMessage(String msg, boolean isAI);

  public void displayResults(Article[] news, boolean isAI);

}
