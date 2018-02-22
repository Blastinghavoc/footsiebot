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
    public static long TRADING_TIME = 54000000; //The time of day in milliseconds to call onTradingHour.

    public static long DOWNLOAD_RATE = 120000;//Download new data every 120 seconds
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
        Boolean runTradingHourTest = false;
        if (args.size() > 0) {
            if (args.get(0).equals("nlp")) {
                debugNLP();
                System.exit(0);
            }
            else if (args.get(0).equals("tradinghour")){
                runTradingHourTest = true;
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

        // onTradingHour();

        if(runTradingHourTest){
            try{
                onTradingHour();//DEBUG
            }catch(Exception e){
                e.printStackTrace();
            }
        }
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
            ui.displayMessage("I'm sorry Dave, but I'm afraid I can't do that");
            return;
        }

        if(!checkParseResultValid(pr)){
            ui.displayMessage("Sorry, that was not a valid query.");
            return;
        }

        System.out.println(pr); //DEBUG

        extraDataAddedToLastOutput = null;//Reseting this.

        Suggestion suggestion;

        Boolean managedToStoreQuery = dbm.storeQuery(pr,LocalDateTime.now());
        if(!managedToStoreQuery){
            System.out.println("Failed to store query!");
        }
        //Branch based on whether the intent is for news or data.
        if (pr.getIntent() == Intent.NEWS) {
            outputNews(pr,null);
        } else {
            outputFTSE(pr,null);
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

   /**
    *
    */
    private String[] groupNameToCompanyList(String group) {
        return dbm.getCompaniesInGroup(group);
    }

    private String formatOutput(String[] data,ParseResult pr,Boolean wasSuggestion){
        String output = "Whoops, something went wrong!";
        switch(pr.getIntent()){
            case SPOT_PRICE:
                output = "The spot price of " + pr.getOperand().toUpperCase() + " is GBX "+ data[0];
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
                {
                    String date = " ("+data[1].split(",")[1].trim()+")";
                    output = "The opening price of "+ pr.getOperand()+" was GBX " + data[0] + " "+ pr.getTimeSpecifier().toString().toLowerCase().replace("_"," ") + date;
                    if(!wasSuggestion){
                        String[] remainingData = Arrays.copyOfRange(data, 1, data.length);
                        output = addExtraDataToOutput(output,remainingData);
                    }
                }
                break;
            case CLOSING_PRICE:
                {
                    String date = " ("+data[1].split(",")[1].trim()+")";
                    output = "The closing price of "+ pr.getOperand()+" was GBX " + data[0] + " " + pr.getTimeSpecifier().toString().toLowerCase().replace("_"," ")+ date;
                    if(!wasSuggestion){
                        String[] remainingData = Arrays.copyOfRange(data, 1, data.length);
                        output = addExtraDataToOutput(output,remainingData);
                    }
                }
                break;
            case TREND:
                if(pr.getTimeSpecifier() == TimeSpecifier.TODAY){
                    output = "So far today, "+ pr.getOperand() + " is ";
                    switch(data[1]){
                        case "rose":
                        output += "rising";
                        break;
                        case "fell":
                        output += "falling";
                        break;
                        case "had no overall change":
                        output += "displaying no net change";
                        break;
                        default:
                        output += "indeterminate";
                        break;
                    }
                    output += " with a net change of "+data[0].trim().substring(0,data[0].indexOf(".")+3) + "%.\n";
                    output += "The opening price was GBX "+ data[2] + " and the most recent price is GBX "+ data[3] + ".";
                    //NOTE: net change is truncated to 3 decimal places. Possibly round in database?
                }
                else{
                    output = pr.getTimeSpecifier().toString().toLowerCase().replace("_"," ")+", "+ pr.getOperand();
                    output += " "+data[1];
                    output += " with a net change of "+data[0].trim().substring(0,data[0].indexOf(".")+3) + "%.\n";
                    output += "The opening price was GBX "+ data[2] + " and the closing price was GBX "+ data[3] + ".";
                }
                break;
            case NEWS:
                //Nothing to do here, should never run, TODO remove
                break;
            case GROUP_FULL_SUMMARY:
                if(pr.getTimeSpecifier() == TimeSpecifier.TODAY){
                    output = "So far today, " + pr.getOperand() + " are ";
                    switch(data[1]){
                        case "rose":
                        output += "rising";
                        break;
                        case "fell":
                        output += "falling";
                        break;
                        case "had no overall change":
                        output += "displaying no net change";
                        break;
                        default:
                        output += "indeterminate";
                        break;
                    }
                    output += " with a net change of "+data[0].trim().substring(0,data[0].indexOf(".")+3) + "%.\n";
                    String[] high = data[2].split(",");
                    output += high[0].trim() + " has the highest spot price at GBX " + high[1].trim() + ".\n";
                    String[] low = data[3].split(",");
                    output += low[0].trim() + " has the lowest spot price at GBX " + low[1].trim()+ ".\n";
                    String[] mostRising = data[4].split(",");
                    output += mostRising[0].trim() + " has the greatest percentage change at " + mostRising[1].trim().substring(0,mostRising[1].indexOf(".")+3)+ "%.\n";
                    String[] mostFalling = data[5].split(",");
                    output += mostFalling[0].trim() + " has the lowest percentage change at " + mostFalling[1].trim().substring(0,mostFalling[1].indexOf(".")+3)+ "%.";
                }
                else{
                    output = pr.getTimeSpecifier().toString().toLowerCase().replace("_"," ")+", "+ pr.getOperand();
                    output += data[1] + " with a net change of "+data[0].substring(0,data[0].indexOf(".")+4) + "%.\n";
                    String[] high = data[2].split(",");
                    output += high[0].trim() + " had the highest closing price at GBX " + high[1].trim() + ".\n";
                    String[] low = data[3].split(",");
                    output += low[0].trim() + " had the lowest closing price at GBX " + low[1].trim()+ ".\n";
                    String[] mostRising = data[4].split(",");
                    output += mostRising[0].trim() + " had the greatest percentage change at " + mostRising[1].trim().substring(0,mostRising[1].indexOf(".")+3)+ "%.\n";
                    String[] mostFalling = data[5].split(",");
                    output += mostFalling[0].trim() + " had the lowest percentage change at " + mostFalling[1].trim().substring(0,mostFalling[1].indexOf(".")+3)+ "%.";
                }
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

   /**
    * Decodes a Suggestion and performs relevant output
    */
    private void handleSuggestion(Suggestion suggestion,ParseResult pr){

        if(suggestion.isNews()){
            outputNews(pr,suggestion);
        }
        else{
            //System.out.println(suggestion.getParseResult());//DEBUG
            ParseResult suggPr = suggestion.getParseResult();
            if((extraDataAddedToLastOutput != null)&& lastOperandOutput.equals(suggPr.getOperand())){
                //System.out.println(suggPr.getIntent()+" vs "+pr.getIntent());//DEBUG
                if((suggPr.getIntent() == pr.getIntent()) ||extraDataAddedToLastOutput.contains(suggPr.getIntent())){
                    //The intent suggested has already been displayed to the user.
                    return;
                }
            }
            if(pr.getIntent()== Intent.TREND){
                if(pr.getTimeSpecifier()== TimeSpecifier.TODAY){
                    if((suggPr.getIntent() == Intent.SPOT_PRICE)||(suggPr.getIntent() == Intent.OPENING_PRICE)){
                        return;//A trend includes spot price and opening price, so don't bother suggesting these.
                    }
                }
            }
            outputFTSE(suggPr,suggestion);
            System.out.println("Displayed suggestion for pr = "+suggPr.toString());//DEBUG
        }
    }

   /**
    *
    */
    private void outputNews(ParseResult pr,Suggestion s){
        Article[] result;
        if (pr.isOperandGroup()) {
            String[] companies = groupNameToCompanyList(pr.getOperand());

            result = dgc.getNews(companies);
        } else {
            result = dgc.getNews(pr.getOperand());
        }
        ui.displayResults(result, s);
    }


    private void outputFTSE(ParseResult pr,Suggestion s){
        /*
        NOTE: may wish to branch for groups, using an overloaded/modified method
        of getFTSE(ParseResult,Boolean).
        */
        String[] data = dbm.getFTSE(pr);

        String result;//NOTE: May convert to a different format for the UI
        Boolean wasSuggestion = (s!= null);
        if(data == null){
            System.out.println("NULL DATA!");
            if(wasSuggestion){
                ui.displayMessage("Sorry, something went wrong trying to give a suggestion for your query");
            }else{
                ui.displayMessage("Sorry, something went wrong trying to fetch data for your query");
            }
            return;
        }

        if (pr.isOperandGroup()) {
            result = formatOutput(data,pr,wasSuggestion);
            ui.displayMessage(result,s);
        } else {
            result = formatOutput(data,pr,wasSuggestion);
            ui.displayMessage(result,s);
        }
    }

    private String addExtraDataToOutput(String output,String[] data){
        extraDataAddedToLastOutput = null;
        if (data.length > 1){
            output += "\n\n";
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

   /**
    *
    */
    public void onTradingHour() {
        System.out.println("It's time for your daily news summary!");//DEBUG
        Company[] companies = ic.onNewsTime();
        String[] companyCodes = new String[companies.length];
        String output = "Hi Dave, it's time for your daily summary!\nI've detected the following companies as important to you:";
        if((companies == null) || (companies.length < 1)){
            return;
        }
        output += "\ncode  : spot     abs      perc     ";//"open  ";
        for(int i = 0;i < companies.length;i++){
            Company c = companies[i];
            String resizedCode = c.getCode();
            companyCodes[i] = resizedCode;
            while(resizedCode.length() <= 5){
                resizedCode += " ";
            }
            output += "\n"+resizedCode + " : ";
            String[] data = dbm.getFTSE(new ParseResult(Intent.SPOT_PRICE,"trading hour",c.getCode(),false,TimeSpecifier.TODAY));
            String[] temp;
            for(String s:data){
                temp = s.split(",");
                String val;
                if(temp.length < 2){
                    val = temp[0];
                }
                else{
                    val = temp[1];
                }

                while(val.length() < 8){
                    val += " ";
                }
                output += val+" ";
            }
        }
        output += "\n\nYou may also view the latest news for these companies in the news pane";
        Article[] news = dgc.getNews(companyCodes);
        ui.displayResults(news,null);
        ui.displayMessage(output,null);
    }

   /**
    *
    */
    public void suggestionIrrelevant(Suggestion s){
        System.out.println("A suggestion was marked irrelevant");
        ic.onSuggestionIrrelevant(s);
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

    public void updateSettings(String time, Double change) {
        System.out.println("Updating the settings with a time of " + time + " and a change of " + change.toString());
    }

   /**
    * Opens the webpage at the url given on the user's default browser
    *
    * @param url the url of the webpage
    */
    public void openWebpage(String url) {
        getHostServices().showDocument(url);
    }

}
