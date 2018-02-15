package footsiebot;

import footsiebot.nlp.*;
import footsiebot.ai.*;
import footsiebot.datagathering.*;
import footsiebot.gui.*;
import footsiebot.database.*;
import javafx.application.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


public class Core extends Application {
    private GUIcore ui;
    private INaturalLanguageProcessor nlp;
    private IDatabaseManager dbm;
    private IDataGathering dgc;
    private IIntelligenceUnit ic;
    public static final long DATA_REFRESH_RATE = 900000; //Rate to call onNewDataAvailable in milliseconds
    public static long TRADING_TIME = 50000000; //The time of day in milliseconds to call onTradingHour.

   /**
    * Constructor for the core
    */
    public Core() {
        nlp = new NLPCore();
        dbm = new DatabaseCore();
        dgc = new DataGatheringCore();
        ic = new IntelligenceCore(dbm);
    }

   /**
    * Nothing else should go here. If you think it needs to go in main,
    * it probably needs to go in start.
    *
    * @param args command-line arguments
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
        if (args.size() > 0) {
            if (args.get(0).equals("nlp")) {
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
            Alert err = new Alert(Alert.AlertType.ERROR, "Styling could not be found", ButtonType.OK);
            err.showAndWait();
            // System.out.println(e.getMessage()); //DEBUG
            ui = new GUIcore(primaryStage, this);
        }

        Article[] news = new Article[1];
        news[0] = new Article("Barclays is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, Barclays, is closing down and all their customers will be left with nothing");
        ui.displayResults(news, true);

        // onNewDataAvailable();//Call once on startup
    }

   /**
    * Performs shut down operations
    * TODO may need a handler for ctrl-C as well
    */
    @Override
    public void stop() {
        //TODO store the trading hour somewhere
        //TODO write volatile data to the Database
        ic.onShutdown();
        System.out.println("Safely closed the program.");
    }

    private static String readEntry(String prompt) { //Nicked from Databases worksheets, can't be included in final submission DEBUG
        try {
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
        catch (IOException e) {
            return "";
        }
    }

   /**
    * Executes the request input by the user
    *
    * @param raw the String input by the user
    */
    public void onUserInput(String raw) {
        ParseResult pr = nlp.parse(raw);
        System.out.println(pr); //DEBUG

        //Branch based on whether the intent is for news or data.
        if (pr.getIntent() == Intent.NEWS) {
            Article[] result;
            if (pr.isOperandGroup()) {
                String[] companies = groupNameToCompanyList(pr.getOperand());
                //TODO resolve a group name into a list of companies
                result = dgc.getNews(companies);
            } else {
                result = dgc.getNews(pr.getOperand());
            }
            dbm.storeQuery(pr,LocalDateTime.now());
            Suggestion suggestion = ic.getSuggestion(pr);
            //TODO send result and suggestion to ui
            ui.displayResults(result, false);
        } else {
            /*
            NOTE: may wish to branch for groups, using an overloaded/modified method
            of getFTSE(ParseResult,Boolean).
            */
            String[] data = dbm.getFTSE(pr);
            dbm.storeQuery(pr,LocalDateTime.now());
            Suggestion suggestion = ic.getSuggestion(pr);

            String result;//NOTE: May convert to a different format for the UI

            if (pr.isOperandGroup()) {
                //Format result based on data
            } else {
                //format result based on data
            }

            //TODO send result and suggestion to ui
        }
        ui.displayMessage(pr.toString(), false);//DEBUG
    }


    //TODO: implement
    private String[] groupNameToCompanyList(String group) {
        return dbm.getCompaniesInGroup(group);
    }

   /**
    * Fetches and stores the latest data, then calls onUpdatedDatabase() from
    * the IC
    */
    public void onNewDataAvailable() {
        System.out.println("New data available!");//DEBUG
        ScrapeResult sr = dgc.getData();
        for(int i = 0; i < 101;i++){
            System.out.println("Entry " + i+ " is "+sr.getName(i) + " with code " + sr.getCode(i));
        }
        System.out.println("Data collected.");
        dbm.storeScraperResults(sr);
        ic.onUpdatedDatabase();
    }

    public void onTradingHour() {
        System.out.println("It's time for your daily news summary!");//DEBUG
        ic.onNewsTime();
    }

    private void debugNLP() {
        Boolean cont = true;
        while (cont) {
            String input = readEntry("Enter a query:\n");

            if (input.equals("exit")) {
                cont = false;
                continue;
            }

            ParseResult result = nlp.parse(input);
            if (result == null) {
                System.out.println("Sorry, I did not understand the query.");
            }
            System.out.println(result);
        }
    }

    public void openWebpage(String url) {
        getHostServices().showDocument(url);
    }

}
