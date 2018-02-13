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
        Float price, absChange, percChange = 0.0f;

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

    public ArrayList<Company> getAICompanies() throws SQLException {
      // Get Counts for each intent
      String query = "";
      // Fetch company code and counters
      query+= "SELECT CompanyCode,NewsCount,SpotPriceCount,OpeningPriceCount,";
      query+= "AbsoluteChangeCount,ClosingPriceCount,percentageChangeCount, ";
      // and also adjustments
      query+= "newsAdjustment";
      query+= "SpotPriceAdjustment";
      query+= "OpeningPriceAdjustment";
      query+= "AbsoluteChangeAdjustment";
      query+= "ClosingPriceAdjustment";
      query+= "percentageChangeAdjustment";
      // Join
      query+= "NATURAL JOIN CompanyNewsCount ";
      query+= "NATURAL JOIN CompanySpotPriceCount ";
      query+= "NATURAL JOIN CompanyOpeningPriceCount ";
      query+= "NATURAL JOIN CompanyAbsoluteChangeCount ";
      query+= "NATURAL JOIN CompanyClosingPriceCount ";
      query+= "NATURAL JOIN PercentageChangeCount ";

      Statement stmt = null;
      ResultSet rs = null;

      ArrayList<Company> companies = new ArrayList<>();

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        while(rs.next()) {
          // Create list of intents for each company
          ArrayList<IntentData> intents = new ArrayList<>();
          // News counter
          float newsCount = (float) rs.getInt("NewsCount");
          // Intents
          float spot = (float) rs.getInt("SpotPriceCount");
          float opening = (float) rs.getInt("OpeningPriceCount");
          float absoluteChange = (float) rs.getInt("AbsoluteChangeCount");
          float closing = (float) rs.getInt("ClosingPriceCount");
          float percentageChange = (float) rs.getInt("percentageChangeCount");
          // Now the  adjustments
          // for news
          float newsAdj = (float) rs.getInt("newsAdjustment");
          // and for intents
          float spotAdj = (float) rs.getInt("SpotPriceAdjustment");
          float openingAdj = (float) rs.getInt("OpeningPriceAdjustment");
          float absoluteChangeAdj = (float) rs.getInt("AbsoluteChangeAdjustment");
          float closingPriceAdj = (float) rs.getInt("ClosingPriceAdjustment");
          float percentageChangeAdj = (float) rs.getInt("percentageChangeAdjustment");

          // Instantiate IntentData List for this company
          intents.add(new IntentData(AIIntent.SPOT_PRICE, spot, spotAdj));
          intents.add(new IntentData(AIIntent.OPENING_PRICE, opening, openingAdj));
          intents.add(new IntentData(AIIntent.ABSOLUTE_CHANGE, absoluteChange, absoluteChangeAdj));
          intents.add(new IntentData(AIIntent.CLOSING_PRICE, closing, closingPriceAdj));
          intents.add(new IntentData(AIIntent.PERCENT_CHANGE, percentageChange, percentageChangeAdj));

          // Calculate priority for each company
          // it is the sum of all the counts
          float priority = spot + opening + absoluteChange + closing + percentageChange;
          // average of all intent's irrelevantSuggestionWeight
          float irrelevantSuggestionWeightForCompany = (spotAdj + openingAdj + absoluteChangeAdj + closingPriceAdj + percentageChangeAdj) / 5;
          companies.add(new Company(rs.getString("CompanyCode"), intents, newsCount, priority,  irrelevantSuggestionWeightForCompany ));
        }

        if(companies.size() != 0) {
          return companies;
        } else {
          return null;
        }

      } catch (SQLException e) {
        printSQLException(e);
        return null;
      } finally {
        if (stmt != null) { stmt.close(); }
      }

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

    public String[] getCompaniesInGroup(String groupName){
        groupName.toLowerCase();
        ArrayList<String> companies = new ArrayList<String>();
        try {
            String query = "SELECT CompanyName from FTSECompanies ";
            query += "INNER JOIN FTSEGroupMappings ON (FTSECompanies.CompanyCode = FTSEGroupMappings.CompanyCode) ";
            query += "WHERE FTSEGroupMappings.GroupName = '"+groupName+"'";
            Statement s1 = conn.createStatement();
            ResultSet r1 = s1.executeQuery(query);
            while(r1.next()){
                companies.add(r1.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Couldn't resolve group name");
            return null;
        }
        return companies.toArray(new String[1]);
    }
    /**
     *  Nicked from JDBC tutorial
     *  Found it useful for debugging
     * @param SQLException
     */
    public void printSQLException(SQLException ex) {

      for (Throwable e : ex) {
          if (e instanceof SQLException) {
              if (ignoreSQLException(
                  ((SQLException)e).
                  getSQLState()) == false) {

                  e.printStackTrace(System.err);
                  System.err.println("SQLState: " +
                      ((SQLException)e).getSQLState());

                  System.err.println("Error Code: " +
                      ((SQLException)e).getErrorCode());

                  System.err.println("Message: " + e.getMessage());

                  Throwable t = ex.getCause();
                  while(t != null) {
                      System.out.println("Cause: " + t);
                      t = t.getCause();
                  }
              }
          }
      }
    }

}
