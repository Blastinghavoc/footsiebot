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
    private IDataGathering dgc;
    private IIntelligenceUnit ic;
    public static final long DATA_REFRESH_RATE = 900000;//Rate to call onNewDataAvailable in milliseconds
    public static long TRADING_TIME = 50000000;//The time of day in milliseconds to call onTradingHour.

    public Core() {
        nlp = new NLPCore();
        dbm = new DatabaseCore();
        dgc = new DataGatheringCore();
        ic = new IntelligenceCore();
    }

    /*
    Nothing else should go here. If you think it needs to go in main,
    it probably needs to go in start.
    */
    public static void main(String[] args) {
        //Initialise user interface
        launch(args);
    }

    /**
    * Starts the application
    *
    * @param primaryStage the inital stage of the application
    */
    @Override
    public void start(Stage primaryStage) {
        List<String> args = getParameters().getRaw();
        //Allows running of tests.
        if(args.size() > 0){
            if(args.get(0).equals("nlp")){
                debugNLP();
                System.exit(0);
            }
        }

      //construct UI
      try { //attempt to use custom styling
          FileReader fRead = new FileReader("./src/gui/config/settings.txt");
          BufferedReader buffRead = new BufferedReader(fRead);
          String tmp = buffRead.readLine();
          if (tmp != null)
              ui = new GUIcore(primaryStage, tmp, this);
          else
              ui = new GUIcore(primaryStage, this);
        } catch (Exception e) { //if any exceptions, create with default styling
          // Alert err = new Alert()
          System.out.println(e.getMessage());
        //   System.out.println("Styling exception");
          ui = new GUIcore(primaryStage, this);
        }

    }

    /*
    Performs shut down operations.
    May need a handler for ctrl-C as well.
    */
    @Override
    public void stop(){
        //Store the trading hour somewhere
        //Write volatile data to the Database
        ic.onShutdown();
        System.out.println("Safely closed the program.");
    }

    private static String readEntry(String prompt) {//Nicked from Databases worksheets, can't be included in final submission DEBUG
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

    public void onUserInput(String raw){
        //
    }

    public void onNewDataAvailable(){
        System.out.println("New data available!");//DEBUG
        ScrapeResult sr = dgc.getData();
        dbm.storeScraperResults(sr);
        ic.onUpdatedDatabase();
    }

    public void onTradingHour(){
        System.out.println("It's time for your daily news summary!");//DEBUG
        ic.onNewsTime();
    }

    private void timingLoop(){
        //Functionality of this is in guicore now.
    }

    private void debugNLP(){
        Boolean cont = true;
        while (cont) {
            String input = readEntry("Enter a query:\n");

            if (input.equals("exit")){
                cont = false;
                continue;
            }

            ParseResult result = nlp.parse(input);
            if(result == null) {
                System.out.println("Sorry, I did not understand the query.");
            }
            System.out.println(result);
        }
    }

}
