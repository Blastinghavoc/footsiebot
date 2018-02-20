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

    public static long DOWNLOAD_RATE = 60000;//Download new data every 60 seconds
    private volatile ScrapeResult lastestScrape;
    private Boolean freshData = false;
    private Boolean readingScrape = false;
    private Boolean writingScrape = false;

    private ArrayList<Intent> extraDataAddedToLastOutput;
    private String lastOperandOutput;

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
    * Launches the application
    *
    * Nothing else should go here. If you think it needs to go in main,
    * it probably needs to go in start.
    *
    * @param args command-line arguments
    */
    public static void main(String[] args) {
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

        // Article[] news = new Article[5];
        // news[0] = new Article("Barclays is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, Barclays, is closing down and all their customers will be left with nothing");
        // news[1] = new Article("HSBC is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, HSBC, is closing down and all their customers will be left with nothing");
        // news[2] = new Article("Santander is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, Santander, is closing down and all their customers will be left with nothing");
        // news[3] = new Article("Nationwide is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, Nationwide, is closing down and all their customers will be left with nothing");
        // news[4] = new Article("Lloyds is closing", "http://www.bbc.co.uk/news/world-asia-43057574", "One of the UK's main banks, Lloyds, is closing down and all their customers will be left with nothing");
        // ui.displayResults(news, true);
        // ui.displayMessage("AI suggestion", true);

        //onNewDataAvailable();//Call once on startup
    }

   /**
    * Performs shut down operations
    * TODO may need a handler for ctrl-C as well
    */
    @Override
    public void stop() {
        //TODO store the trading hour somewhere
        //TODO write volatile data to the Database
        ui.stopDataDownload();
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
        onNewDataAvailable();//Checks if new data. If not, does nothing
        ParseResult pr = nlp.parse(raw);
        if((pr == null)||(pr.getIntent()== null)||(pr.getOperand()== null)){
            ui.displayMessage("I'm sorry Dave, but I'm afraid I can't do that",false);
            return;
        }

        if(!checkParseResultValid(pr)){
            ui.displayMessage("Sorry, that was not a valid query.",false);
            return;
        }

        System.out.println(pr); //DEBUG
        Suggestion suggestion;

        Boolean managedToStoreQuery = dbm.storeQuery(pr,LocalDateTime.now());
        if(!managedToStoreQuery){
            System.out.println("Failed to store query!");
        }
        //Branch based on whether the intent is for news or data.
        if (pr.getIntent() == Intent.NEWS) {
            outputNews(pr,false);
        } else {
            outputFTSE(pr,false);
        }

        lastOperandOutput = pr.getOperand();

        suggestion = ic.getSuggestion(pr);
        if(suggestion != null){
            handleSuggestion(suggestion,pr);
        }
        else{
            System.out.println("Null suggestion");
        }
    }



    private String[] groupNameToCompanyList(String group) {
        return dbm.getCompaniesInGroup(group);
    }

    private String formatOutput(String[] data,ParseResult pr,Boolean wasSuggestion){
        String output = "Whoops, something went wrong!";
        switch(pr.getIntent()){
            case SPOT_PRICE:
                output = "The spot price of " + pr.getOperand().toUpperCase() + " is GBX "+ data[0] + " .";
                if(!wasSuggestion){
                    output = addExtraDataToOutput(output,data);
                }
                break;
            case TRADING_VOLUME:
                break;
            case PERCENT_CHANGE:
                output = "The percentage change of " + pr.getOperand().toUpperCase() + " is "+ data[0]+"% since the market opened.";
                if(!wasSuggestion){
                    output = addExtraDataToOutput(output,data);
                }
                break;
            case ABSOLUTE_CHANGE:
                output = "The absolute change of " + pr.getOperand().toUpperCase() + " is GBX "+ data[0] + " since the market opened.";
                if(!wasSuggestion){
                    output = addExtraDataToOutput(output,data);
                }
                break;
            case OPENING_PRICE:
                break;
            case CLOSING_PRICE:
                break;
            case TREND:
                break;
            case NEWS:
                break;
            case GROUP_FULL_SUMMARY:
                break;
            default:
            System.out.println("No cases ran in core");
            break;
        }

        if (wasSuggestion){
            output = "You may also want to know:\n" + output;
        }

        return output;
    }

    /*
    * Decodes a Suggestion and performs relevant output
    */
    private void handleSuggestion(Suggestion suggestion,ParseResult pr){

        if(suggestion.isNews()){
            outputNews(pr,true);
        }
        else{
            //System.out.println(suggestion.getParseResult());//DEBUG
            ParseResult suggPr = suggestion.getParseResult();
            if((extraDataAddedToLastOutput != null)&& lastOperandOutput.equals(suggPr.getOperand())){
                if((suggPr.getIntent() == pr.getIntent()) ||extraDataAddedToLastOutput.contains(suggPr.getIntent())){
                    //The intent suggested has already been displayed to the user.
                    return;
                }
            }
            outputFTSE(suggPr,true);
            System.out.println("Displayed suggestion for pr = "+suggPr.toString());//DEBUG
        }
    }

    private void outputNews(ParseResult pr,Boolean wasSuggestion){
        Article[] result;
        if (pr.isOperandGroup()) {
            String[] companies = groupNameToCompanyList(pr.getOperand());

            result = dgc.getNews(companies);
        } else {
            result = dgc.getNews(pr.getOperand());
        }
        ui.displayResults(result, wasSuggestion);
    }

    private void outputFTSE(ParseResult pr,Boolean wasSuggestion){
        /*
        NOTE: may wish to branch for groups, using an overloaded/modified method
        of getFTSE(ParseResult,Boolean).
        */
        String[] data = dbm.getFTSE(pr);

        String result;//NOTE: May convert to a different format for the UI

        if(data == null){
            System.out.println("NULL DATA!");
            if(wasSuggestion){
                ui.displayMessage("Sorry, something went wrong trying to give a suggestion for your query",false);
            }else{
                ui.displayMessage("Sorry, something went wrong trying to fetch data for your query",false);
            }
            return;
        }

        if (pr.isOperandGroup()) {
            result = formatOutput(data,pr,wasSuggestion);
            ui.displayMessage(result,wasSuggestion);
        } else {
            result = formatOutput(data,pr,wasSuggestion);
            ui.displayMessage(result,wasSuggestion);
        }
    }

    private String addExtraDataToOutput(String output,String[] data){
        extraDataAddedToLastOutput = null;
        if (data.length > 1){
            output += "\n";
            extraDataAddedToLastOutput = new ArrayList<Intent>();
            output += "Related data about this company:";
            String[] temp;
            for(int i = 1; i < data.length;i++){
                temp = data[i].split(",");//relying on data being in csv form
                output += "\n" + temp[0] + " = " + temp[1];
                extraDataAddedToLastOutput.add(convertColumnNameToIntent(temp[0]));//NOTE: NEEDS TESTING
            }
        }
        return output;
    }

    private Intent convertColumnNameToIntent(String s){
        //NOTE: probably highly inefficient, may not even work! Needs testing.
        Intent in = nlp.parse(s).getIntent();
        return in;
    }

    private Boolean checkParseResultValid(ParseResult pr){
        switch (pr.getIntent()){
            case SPOT_PRICE:
                if(pr.isOperandGroup()){
                    return false;
                }
                if(pr.getTimeSpecifier() != TimeSpecifier.TODAY){
                    return false;
                }
            break;
            case TRADING_VOLUME:
                if(pr.isOperandGroup()){
                    return false;
                }
            break;
            case OPENING_PRICE:
                if(pr.isOperandGroup()){
                    return false;
                }
            break;
            case CLOSING_PRICE:
                if(pr.isOperandGroup()){
                    return false;
                }
                if(pr.getTimeSpecifier() == TimeSpecifier.TODAY){
                    return false;
                }
            break;
            case PERCENT_CHANGE:
                if(pr.isOperandGroup()){
                    return false;
                }
            break;
            case ABSOLUTE_CHANGE:
                if(pr.isOperandGroup()){
                    return false;
                }
            break;
            case TREND:
                if(pr.isOperandGroup()){
                    return false;
                }
            break;
            case NEWS:
            break;
            case GROUP_FULL_SUMMARY:
            break;
            default:
            return false;
        }
        return true;
    }

    /*
    Must only be called asynchronously from the GUIcore.
    Downloads new data to a local variable in the background.
    */
    public void downloadNewData(){
        System.out.println("Downloading new data");
        while(readingScrape){
            System.out.println("Waiting for data to be read");
            try{
                Thread.sleep(1000);
            }catch(Exception e){

            }
        }
        writingScrape = true;
        ScrapeResult temp = dgc.getData();
        if((lastestScrape == null)){
            lastestScrape = temp;
            freshData = true;
            System.out.println("Data downloaded successfully");
        }
        else if(temp.equals(lastestScrape)){
            System.out.println("Data hasn't changed since last download");
        }
        else{
            synchronized (lastestScrape){//Eliminates potential race conditions on setting/reading lastestScrape
                lastestScrape = temp;
                freshData = true;
            }
            System.out.println("Data downloaded successfully");
        }

        writingScrape = false;
    }

   /**
    * Fetches and stores the latest data, then calls onUpdatedDatabase() from
    * the IC
    */
    public void onNewDataAvailable() {
        if(freshData == false){
            return;
        }
        System.out.println("New data available!");//DEBUG

        if(writingScrape){
            System.out.println("Couldn't retrieve new data, as it was being written");
            return;
        }
        if(lastestScrape == null){
            return;
        }
        readingScrape = true;
        synchronized (lastestScrape){//Should make this section safe
            ScrapeResult sr = lastestScrape;
            // for(int i = 0; i < 101;i++){
            //     System.out.println("Entry " + i+ " is "+sr.getName(i) + " with code " + sr.getCode(i));
            // }
            System.out.println("Data collected.");
            if(!dbm.storeScraperResults(sr)){
                System.out.println("Couldn't store data to database");
            }
        }
        freshData = false;
        readingScrape = false;
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
