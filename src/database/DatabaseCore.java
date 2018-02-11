package footsiebot.databasecore;

import footsiebot.nlpcore.ParseResult;
import footsiebot.datagatheringcore.ScrapeResult;
import footsiebot.intelligencecore.*;
import java.time.LocalDateTime;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseCore implements IDatabaseManager {

    public DatabaseCore() {

        // load the sqlite-JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        }
            catch (Exception e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:databasecore/footsie_db.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 seconds

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean storeScraperResults(ScrapeResult sr) {
        
        // need to delete old FTSE data

        int numCompanies = sr.codes.length;
        LocalDateTime currentTime = LocalDateTime.now();
        String code = " ";
        Double price, absChange, percChange = 0;

        // store all scraper data in database
        for (int i = 0; i < numCompanies; i++) {
            code = sr.getCode(i);
            group = sr.getGroup(i);
            name = sr.getName(i);
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
                String addScrapeResultQuery = "INSERT INTO FTSECompanySnapshots\n"
                                            + "VALUES(" + code + ", " + currentTime + ", " + price + ", " + percChange + ", " + absChange + ")";
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
        return null;
    }

    private String convertScrapeResult(ScrapeResult sr) {
        return null;
    }

    private String convertQuery(ParseResult pr, LocalDateTime date) {
        return null;
    }

    private String convertFTSEQuery(ParseResult pr) {
        return null;
    }

    private ArrayList<Company> getAICompany() {
        return null;
    }

    private ArrayList<Group> getAIGroup() {
        return null;
    }

    private IntentData getIntentForCompany() {
        return null;
    }

    private void storeAICompanies(ArrayList<Company> companies) {

    }

    private void storeAIGroups(ArrayList<Group> groups) {

    }

}