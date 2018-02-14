package footsiebot.database;

import footsiebot.nlp.*;
// import footsiebot.nlp.ParseResult;
// import footsiebot.nlp.Intent;
// import footsiebot.nlp.TimeSpecifier;

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
            Statement statement = conn.createStatement();//NOTE: What is the purpose of this?
            statement.setQueryTimeout(30);  // set timeout to 30 seconds

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

        String group, name;

        Statement s1 = null;
        ResultSet companyCheck = null;
        Statement s2 = null;
        Statement s3 = null;
        Statement s4 = null;


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
                s1 = conn.createStatement();
                companyCheck = s1.executeQuery(checkNewCompanyQuery);
                if (!companyCheck.next()) {
                    String addNewCompanyQuery   = "INSERT INTO FTSECompanies\n"
                                                + "VALUES(" + code + ", " + name + ")";
                    s2 = conn.createStatement();
                    s2.executeQuery(addNewCompanyQuery);

                    String addCompanyGroupQuery = "INSERT INTO FTSEGroupMappings\n"
                                                + "VALUES(" + group + ", " + code + ")";
                    s3 = conn.createStatement();
                    s3.executeQuery(addCompanyGroupQuery);
                }

                // add the company data into the FTSECompanySnapshots table
                String addScrapeResultQuery = "INSERT INTO FTSECompanySnapshots(CompanyCode, SpotPrice, PercentageChange, AbsoluteChange)\n"
                                            + "VALUES(" + code + ", " + price + ", " + percChange + ", " + absChange + ")";
                s4 = conn.createStatement();
                s4.executeQuery(addScrapeResultQuery);

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Couldn't store FTSE data");
                tryClose(s1);
                tryClose(companyCheck);
                tryClose(s2);
                tryClose(s3);
                tryClose(s4);
                return false;
            }
        }

        tryClose(s1);
        tryClose(companyCheck);
        tryClose(s2);
        tryClose(s3);
        tryClose(s4);
        return true;
    }

    public boolean storeQuery(ParseResult pr, LocalDateTime date) {
        return false;
    }

    public String[] getFTSE(ParseResult pr) {

        String FTSEQuery = convertFTSEQuery(pr);
        Statement s1 = null;
        ResultSet results = null;

        try {
            s1 = conn.createStatement();
            results = s1.executeQuery(FTSEQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(FTSEQuery);//DEBUG
            tryClose(s1,results);
        }


        return null;
    }

    public String convertScrapeResult(ScrapeResult sr) {
        return null;
    }

    public String convertQuery(ParseResult pr, LocalDateTime date) {
        return null;
    }

    public String convertFTSEQuery(ParseResult pr) {
        footsiebot.nlp.Intent intent = pr.getIntent();
        footsiebot.nlp.TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String operand = pr.getOperand();
        Boolean isGroup = pr.isOperandGroup();

        operand = "'"+operand+"'";//Ensuring operand in correct string representation

        String query = "";
        String timeSpecifierSQL = "";
        String companyCode = "";
        Boolean isFetchCurrentQuery = false; // if the query is fetch current data from a column in database
        String colName = "";

        Statement s1 = null;
        ResultSet code = null;

        // if not the operand is not a group, get the company code
        if (!isGroup) {
            String getCompanyCodeQuery = "SELECT CompanyCode FROM FTSECompanies WHERE CompanyName = " + operand;
            try {
                s1 = conn.createStatement();
                code = s1.executeQuery(getCompanyCodeQuery);
                while (code.next()) {
                    companyCode = code.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                tryClose(s1,code);
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

        tryClose(s1,code);

        return query;
    }

    public ArrayList<Company> getAICompanies() {
      // Get Counts for each intent
      String query = "";
      // Fetch company code and counters
      query+= "SELECT CompanyCode,NewsCount,SpotPriceCount,OpeningPriceCount,";
      query+= "AbsoluteChangeCount,ClosingPriceCount,percentageChangeCount, ";
      // and also adjustments
      query+= "newsAdjustment, ";
      query+= "SpotPriceAdjustment, ";
      query+= "OpeningPriceAdjustment,";
      query+= "AbsoluteChangeAdjustment,";
      query+= "ClosingPriceAdjustment,";
      query+= "percentageChangeAdjustment,";
      // Join
      query+= "FROM FTSECompanies ";
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
          float newsAdj = (float) rs.getFloat("newsAdjustment");
          // and for intents
          float spotAdj = (float) rs.getFloat("SpotPriceAdjustment");
          float openingAdj = (float) rs.getFloat("OpeningPriceAdjustment");
          float absoluteChangeAdj = (float) rs.getFloat("AbsoluteChangeAdjustment");
          float closingPriceAdj = (float) rs.getFloat("ClosingPriceAdjustment");
          float percentageChangeAdj = (float) rs.getFloat("percentageChangeAdjustment");

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
        if (stmt != null) { tryClose(stmt); }
      }
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
              if (true/*ignoreSQLException(((SQLException)e).getSQLState()) == false*/) {

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

    //Handy methods to close statements and ResultSets
    private void tryClose(Statement s){
        try{
            s.close();
        }
        catch(Exception e){
            //Do nothing
        }
    }

    private void tryClose(ResultSet s){
        try{
            s.close();
        }
        catch(Exception e){
            //Do nothing
        }
    }

    private void tryClose(Statement s,ResultSet rs){
        tryClose(s);
        tryClose(rs);
    }

}
