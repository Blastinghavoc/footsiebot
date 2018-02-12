package footsiebot.database;

import footsiebot.nlp.ParseResult;
import footsiebot.nlp.Intent;
import footsiebot.nlp.TimeSpecifier;

import footsiebot.datagathering.ScrapeResult;
import footsiebot.ai.*;
import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseCore implements IDatabaseManager {
    private Connection conn;

    public DatabaseCore() {

        // load the sqlite-JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        }
            catch (Exception e) {
            e.printStackTrace();
        }

        conn = null;
        try {
            // create a database connection
            conn = DriverManager.getConnection("jdbc:sqlite:databasecore/footsie_db.db");
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 seconds

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean storeScraperResults(ScrapeResult sr) {

        // ---------------------- need to delete old FTSE data -----------------------------------------

        int numCompanies = 100; // constant
        Float price, absChange, percChange = 0.0f;
        String code, group, name = " ";

        // store all scraper data in database
        for (int i = 0; i < numCompanies; i++) {
            code = sr.getCode(i);
            group = sr.getGroup(i).toLowerCase();
            name = sr.getName(i).toLowerCase();
            price = sr.getPrice(i);
            absChange = sr.getAbsChange(i);
            percChange = sr.getPercChange(i);

            try {

                // if the company is a new FTSE company, add it to the FTSECompanies and FTSEGroupMappings table
                String checkNewCompanyQuery = "SELECT * FROM FTSECompanies WHERE CompanyCode = " + code;
                Statement s1 = conn.createStatement();
                ResultSet companyCheck = s1.executeQuery(checkNewCompanyQuery);
                if (!companyCheck.next()) {
                    String addNewCompanyQuery   = "INSERT INTO FTSECompanies\n"
                                                + "VALUES(" + code + ", " + name + ")";
                    Statement s2 = conn.createStatement();
                    s2.executeQuery(addNewCompanyQuery);

                    String addCompanyGroupQuery = "INSERT INTO FTSEGroupMappings\n"
                                                + "VALUES(" + group + ", " + code + ")";
                    Statement s3 = conn.createStatement();
                    s3.executeQuery(addCompanyGroupQuery);
                }

                // add the company data into the FTSECompanySnapshots table
                String addScrapeResultQuery = "INSERT INTO FTSECompanySnapshots(CompanyCode, SpotPrice, PercentageChange, AbsoluteChange)\n"
                                            + "VALUES(" + code + ", " + price + ", " + percChange + ", " + absChange + ")";
                Statement s4 = conn.createStatement();
                s4.executeQuery(addScrapeResultQuery);

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Couldn't store FTSE data");
                return false;
            }
        }

        return true;
    }

    public boolean storeQuery(ParseResult pr, LocalDateTime date) {
        return false;
    }

    public String[] getFTSE(ParseResult pr) {

        String FTSEQuery = convertFTSEQuery(pr); 
        Statement s1 = conn.createStatement();
        Resultset results = s1.executeQuery(FTSEQuery);
        

        return null;
    }

    public String convertScrapeResult(ScrapeResult sr) {
        return null;
    }

    public String convertQuery(ParseResult pr, LocalDateTime date) {
        return null;
    }

    public String convertFTSEQuery(ParseResult pr) {
        nlp.Intent intent = pr.getIntent();
        nlp.TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String operand = pr.getOperand(); 
        Boolean isGroup = pr.isOperandGroup();

        String query = "";
        String timeSpecifierSQL = "";
        String companyCode;
        Boolean isFetchCurrentQuery = false; // if the query is fetch current data from a column in database
        String colName = "";

        // if not the operand is not a group, get the company code
        if (!isGroup) {
            String getCompanyCodeQuery = "SELECT CompanyCode FROM FTSECompanies WHERE CompanyName = " + operand;
            try {
                Statement s1 = conn.createStatement();
                Resultset code = s1.executeQuery(getCompanyCodeQuery);
                while (code.next()) {
                    companyCode = code.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        switch (intent) {
            case SPOT_PRICE:
                isFetchCurrentQuery = true;
                colName = "SpotPrice";
                break;
            case TRADING_VOLUME: // haven't got this column yet so won't work
                isFetchCurrentQuery = true;
                colName = "TradingVolume";
                break;
            case PERCENT_CHANGE:
                isFetchCurrentQuery = true;
                colName = "PercentageChange";
                break;
            case ABSOLUTE_CHANGE:
                isFetchCurrentQuery = true;
                colName = "AbsoluteChange";
                break;
            case OPENING_PRICE:
                
            case CLOSING_PRICE:

            case TREND:

            case NEWS:

            case GROUP_FULL_SUMMARY:

        }

        // need to make sure you get last record added for current data 
        if (isFetchCurrentQuery) {
            query = "SELECT " + colName + " FROM FTSECompanySnapshots WHERE CompanyCode = " + companyCode + "ORDER BY TimeOfData DESC LIMIT 1";

        }

        return query;
    }

    public ArrayList<Company> getAICompanies() {
        return null;
    }

    public ArrayList<Group> getAIGroups() {
        return null;
    }

    public IntentData getIntentForCompany() {
        return null;
    }

    public void storeAICompanies(ArrayList<Company> companies) {

    }

    public void storeAIGroups(ArrayList<Group> groups) {

    }

}
