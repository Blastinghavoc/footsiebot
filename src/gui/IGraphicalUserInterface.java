package footsiebot.gui;

import footsiebot.datagathering.Article;

public interface IGraphicalUserInterface {

  public void displayResults(String data, boolean isAI);

  public void displayResults(Article[] news, boolean isAI);

}
