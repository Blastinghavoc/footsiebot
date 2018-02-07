package footsiebot;

import footsiebot.nlpcore.*;
import footsiebot.intelligencecore.*;
import footsiebot.datagatheringcore.*;
import footsiebot.guicore.*;
import footsiebot.databasecore.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.*;


public class Core extends Application {

  public static void main(String[] args) {
    INaturalLanguageProcessor p = new NLPCore();
    while(true){
      ParseResult result = p.parse(readEntry("Enter a query:\n"));
      if(result == null){
	System.out.println("Sorry, I did not understand the query.");
      }
      System.out.println(result);
    }

    //Initialise user interface
    launch(args);

    //connect();

  }

  /**
  * Starts the application
  *
  * @param primaryStage the inital stage of the application
  */
  @Override
  public void start(Stage primaryStage) {
      //construct UI
      try { //attempt to use custom styling
          FileReader fRead = new FileReader("./config/settings.txt");
          BufferedReader buffRead = new BufferedReader(fRead);
          String tmp = buffRead.readLine();
          if (tmp != null)
              ui = new UI(primaryStage, tmp);
          else
              ui = new UI(primaryStage);
      } catch (Exception e) { //if any exceptions, create with default styling
          // Alert err = new Alert()
          ui = new UI(primaryStage);
      }
  }

  private static String readEntry(String prompt) //Nicked from Databases worksheets, can't be included in final submission DEBUG
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			System.out.print(prompt);
			System.out.flush();
			int c = System.in.read();
			while(c != '\n' && c != -1) {
				buffer.append((char)c);
				c = System.in.read();
			}
			return buffer.toString().trim();
		}
		catch (IOException e)
		{
			return "";
		}
 	}


}