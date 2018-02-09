package footsiebot;

import footsiebot.nlpcore.*;
import footsiebot.intelligencecore.*;
import footsiebot.datagatheringcore.*;
import footsiebot.guicore.*;
import footsiebot.databasecore.*;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;


public class Core extends Application {
    private GUIcore ui;
    private INaturalLanguageProcessor nlp;
    private IDatabaseManager dbm;
    private static final String PATH_TO_GUI_FOLDER = "./footsiebot/guicore";

<<<<<<< HEAD:src/Core.java
  public static void main(String[] args) {
    INaturalLanguageProcessor p = new NLPCore();
    nlp = new NLPCore();
    // debugNLP();
    launch(args);
    // while(true){
    //   ParseResult result = p.parse(readEntry("Enter a query:\n"));
    //   if(result == null){
	// System.out.println("Sorry, I did not understand the query.");
    //   }
    //   System.out.println(result);
    // }

  }

  private static void debugNLP(){
	  while(true){
      ParseResult result = nlp.parse(readEntry("Enter a query:\n"));
      if(result == null){
		System.out.println("Sorry, I did not understand the query.");
      }
      System.out.println(result);
    }
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
          FileReader fRead = new FileReader("./guicore/config/settings.txt");
=======
    public static void main(String[] args) {
    Core c = new Core();

    if(args.length > 0){
        if(args[0].equals("nlp")){
            c.debugNLP();
            System.exit(0);
        }
    }

    //Initialise user interface
    launch(args);

    }

    public Core(){
        nlp = new NLPCore();
        dbm = new DatabaseCore();
    }

    public void onUserInput(String raw){

    }

    public void onNewDataAvailable(){

    }

    private void timingLoop(){
        //Functionality of this is likely to have to go elsewhere.
    }

    private void debugNLP(){
        Boolean cont = true;
        while(cont){
            String input = readEntry("Enter a query:\n");

            if (input.equals("exit")){
                cont = false;
                continue;
            }

            ParseResult result = nlp.parse(input);
            if(result == null){
                System.out.println("Sorry, I did not understand the query.");
            }
            System.out.println(result);
        }
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
          FileReader fRead = new FileReader(PATH_TO_GUI_FOLDER+"/config/settings.txt");
>>>>>>> c1c7b9b4e008d27ebc2e2c5f8fd3aa4aa78f6211:Core.java
          BufferedReader buffRead = new BufferedReader(fRead);
          String tmp = buffRead.readLine();
          if (tmp != null)
              ui = new GUIcore(primaryStage, tmp,PATH_TO_GUI_FOLDER);
          else
              ui = new GUIcore(primaryStage,PATH_TO_GUI_FOLDER);
        } catch (Exception e) { //if any exceptions, create with default styling
          // Alert err = new Alert()
          ui = new GUIcore(primaryStage,PATH_TO_GUI_FOLDER);
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
