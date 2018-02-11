package footsiebot.database;

import footsiebot.nlp.ParseResult;
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
            conn = DriverManager.getConnection("jdbc:sqlite:src/database/footsie_db.db");
            Statement statement = conn.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean storeScraperResults(ScrapeResult sr) {

        // need to delete old FTSE data

        int numCompanies = 100;//Constant
        LocalDateTime currentTime = LocalDateTime.now();
        String code = " ";
        Double price, absChange, percChange = 0.0;

        String group,name;


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

    public String convertScrapeResult(ScrapeResult sr) {
        return null;
    }

    public String convertQuery(ParseResult pr, LocalDateTime date) {
        return null;
    }

    public String convertFTSEQuery(ParseResult pr) {
        return null;
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
