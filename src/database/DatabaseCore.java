package footsiebot.database;

import footsiebot.nlp.*;
// import footsiebot.nlp.ParseResult;
// import footsiebot.nlp.Intent;
// import footsiebot.nlp.TimeSpecifier;

import footsiebot.datagathering.ScrapeResult;
import footsiebot.ai.*;
import java.time.LocalDateTime;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
import java.sql.*;
import java.util.*;
import java.lang.*;

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
        PreparedStatement s2 = null;
        PreparedStatement s3 = null;
        Statement s4 = null;

        String checkNewCompanyQuery = null;
        String addNewCompanyQuery = null;
        String addCompanyGroupQuery = null;
        String addScrapeResultQuery = null;

        trySetAutoCommit(false);//Will treat the following as a transaction, so that it can be rolled back if it fails

        // store all scraper data in database
        for (int i = 0; i < numCompanies; i++) {
            code = sr.getCode(i).toLowerCase();

            if(code.endsWith(".")){//Remove punctuation if present
                code = code.substring(0, code.length() - 1);
            }

            group = sr.getGroup(i).toLowerCase();
            name = sr.getName(i).toLowerCase();
            price = sr.getPrice(i);
            absChange = sr.getAbsChange(i);
            percChange = sr.getPercChange(i);

            checkNewCompanyQuery = null;//Reseting
            addNewCompanyQuery = null;
            addCompanyGroupQuery = null;
            addScrapeResultQuery = null;

            try {

                // if the company is a new FTSE company, add it to the FTSECompanies and FTSEGroupMappings table
                checkNewCompanyQuery = "SELECT * FROM FTSECompanies WHERE CompanyCode = '" + code+"'";
                s1 = conn.createStatement();
                companyCheck = s1.executeQuery(checkNewCompanyQuery);
                if (!companyCheck.next()) {
                    addNewCompanyQuery   = "INSERT INTO FTSECompanies "
                                                + "VALUES(?,?)";
                    s2 = conn.prepareStatement(addNewCompanyQuery);//Must be prepared statement to deal with names with quotes in
                    s2.setString(1,code);
                    s2.setString(2,name);
                    s2.executeUpdate();

                    addCompanyGroupQuery = "INSERT INTO FTSEGroupMappings "
                                                + "VALUES(?,?)";
                    s3 = conn.prepareStatement(addCompanyGroupQuery);
                    s3.setString(1,group);
                    s3.setString(2,code);
                    s3.executeUpdate();
                }

                // add the company data into the FTSECompanySnapshots table
                addScrapeResultQuery = "INSERT INTO FTSECompanySnapshots(CompanyCode, SpotPrice, PercentageChange, AbsoluteChange) "
                                            + "VALUES('" + code + "', " + price + ", " + percChange + ", " + absChange + ")";
                s4 = conn.createStatement();
                s4.executeUpdate(addScrapeResultQuery);

            } catch (SQLException e) {
                e.printStackTrace();
                tryRollback();
                trySetAutoCommit(true);
                System.out.println("Couldn't store FTSE data. Rolled back");
                System.out.println("Queries were:\ncheckNewCompanyQuery: "+checkNewCompanyQuery+"\naddNewCompanyQuery: "+addNewCompanyQuery+"\naddCompanyGroupQuery: "+addCompanyGroupQuery+"\naddScrapeResultQuery: "+addScrapeResultQuery);//DEBUG
                tryClose(s1);
                tryClose(companyCheck);
                tryClose(s2);
                tryClose(s3);
                tryClose(s4);
                return false;
            }
        }

        tryCommit();
        trySetAutoCommit(true);

        tryClose(s1);
        tryClose(companyCheck);
        tryClose(s2);
        tryClose(s3);
        tryClose(s4);
        return true;
    }

    /*Probably doesn't actually need the time. The database can do that
    automatically
    */
    public boolean storeQuery(ParseResult pr, LocalDateTime date) {

        // if("DEBUG".equals("DEBUG")){
        //     return false;//DEBUG
        // }
        String companyCode = pr.getOperand();
        String intent = pr.getIntent().toString();
        String timeSpecifier = pr.getTimeSpecifier().toString();

        String query = "INSERT INTO Queries(CompanyCode,Intent,TimeSpecifier) VALUES('"+companyCode+"','"+intent+"','"+timeSpecifier+"')";
        Statement s1 = null;
        ResultSet r1 = null;
        String table = intentToTableName(pr.getIntent());
        if(table == null){
            return false;
        }
        String rowName = table.replace("Company","");//The count row in the tables has the same name as the table, minus the prefix "Company"
        trySetAutoCommit(false);
        try{
            s1 = conn.createStatement();
            s1.executeUpdate(query);
            /*
            check if a row with the relevant CompanyCode exists in the relevant
            count table. If not, create that row.
            If it does, increment the value of the relevant count row (rowName)
            */
            query = "SELECT * FROM "+table+" WHERE CompanyCode = '" + companyCode+"'";
            r1 = s1.executeQuery(query);
            //If the row does not exist, create it.
            if(!r1.next()){
                query = "INSERT INTO "+table+" VALUES ('"+companyCode+"',1,0)";
                s1.executeUpdate(query);
            }
            else{
                //Row does exist, so just increment the count.
                query = "UPDATE "+table+" SET "+rowName+" = "+rowName+" + 1 WHERE CompanyCode = '"+companyCode+"'";
                s1.executeUpdate(query);
            }
            tryCommit();
        }catch(SQLException e){
            e.printStackTrace();
            tryClose(s1,r1);
            tryRollback();
            trySetAutoCommit(true);
            return false;
        }
        tryClose(s1,r1);
        trySetAutoCommit(true);
        return true;
    }

    public String[] getFTSE(ParseResult pr) {

        String FTSEQuery = convertFTSEQuery(pr);
        Statement s1 = null;
        ResultSet results = null;
        ArrayList<String> output = new ArrayList<String>();

        // add asked for data to first index of array
        try {
            s1 = conn.createStatement();
            results = s1.executeQuery(FTSEQuery);
            while (results.next()) {
                output.add(((Float)results.getFloat(1)).toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            //System.out.println(FTSEQuery); //DEBUG
            tryClose(s1,results);
        }

        // add other data about company to other indexes of the array
        output.addAll(getAllCompanyInfo(pr));

        return output.toArray(new String[1]);
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
        String companyCode = pr.getOperand();
        Boolean isGroup = pr.isOperandGroup();

        String query = "";
        String timeSpecifierSQL = "";
        Boolean isFetchCurrentQuery = false; // if the query is fetch current data from a column in database
        String colName = "";

        PreparedStatement s1 = null;

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
                System.out.println("No cases ran");
            	break;
        }

        // get current data requested from database
        if (isFetchCurrentQuery) {
            query = "SELECT " + colName + " FROM FTSECompanySnapshots WHERE CompanyCode = '" + companyCode + "' ORDER BY TimeOfData DESC LIMIT 1";

        }

        tryClose(s1);

        return query;
    }

    private ArrayList<String> getAllCompanyInfo(ParseResult pr) {

    	Statement s1 = null;
    	ResultSet results = null;
    	ArrayList<String> rs = new ArrayList<String>();

    	String companyCode = pr.getOperand();
    	footsiebot.nlp.Intent intent = pr.getIntent();
    	ArrayList<String> columns = new ArrayList<String>();

    	// get columns needed in query
        switch(intent) {
            case SPOT_PRICE:
                columns.add("PercentageChange");
                // columns.add("TradingVolume");
                columns.add("AbsoluteChange");
                break;
            case TRADING_VOLUME:
                columns.add("SpotPrice");
                columns.add("PercentageChange");
                columns.add("AbsoluteChange");
                break;
            case PERCENT_CHANGE:
                columns.add("SpotPrice");
                //columns.add("TradingVolume");
                columns.add("AbsoluteChange");
                break;
            case ABSOLUTE_CHANGE:
                columns.add("SpotPrice");
                //columns.add("TradingVolume");
                columns.add("PercentageChange");
                break;
            case OPENING_PRICE:
            case CLOSING_PRICE:
                columns.add("SpotPrice");
                // columns.add("TradingVolume");
                columns.add("PercentageChange");
                columns.add("AbsoluteChange");
                break;
            default:
                break;
        }

    	// create query
    	String query = "SELECT ";
    	for (int i = 0; i < columns.size(); i++) {
    		query += columns.get(i);
    		// don't add comma after last column
    		if (i != columns.size() -1) {
    			query += ", ";
    		}
    	}
    	query += " FROM FTSECompanySnapshots WHERE CompanyCode = '" + companyCode + "' ORDER BY TimeOfData DESC LIMIT 1";

    	System.out.println(query);//DEBUG

    	// execute and store query results
    	try {
    		s1 = conn.createStatement();
    		results = s1.executeQuery(query);
    		ResultSetMetaData rsmd = results.getMetaData();
    		int columnCount = rsmd.getColumnCount();

    		for (int i = 1; i <= columnCount; i++) {
    			String colName = rsmd.getColumnName(i);
    			rs.add(colName + ", " + ((Float)results.getFloat(i)).toString());
    		}

    	} catch (SQLException e) {
    		e.printStackTrace();
    		tryClose(s1, results);
    	}

    	return rs;
    }

    private String intentToTableName(Intent i){
        String name = null;
        switch (i) {
            case SPOT_PRICE:
                name = "CompanySpotPriceCount";
                break;
            case TRADING_VOLUME:
                name = null;//Not implemented yet
                break;
            case PERCENT_CHANGE:
                name = "CompanyPercentageChangeCount";
                break;
            case ABSOLUTE_CHANGE:
                name = "CompanyAbsoluteChangeCount";
                break;
            case OPENING_PRICE:
                name = "CompanyOpeningPriceCount";
                break;
            case CLOSING_PRICE:
                name = "CompanyClosingPriceCount";
                break;
            case TREND://May need a table for this
                name = null;
                break;
            case NEWS:
                name = "CompanyNewsCount";
                break;
            case GROUP_FULL_SUMMARY://No table for this, return null;
                name = null;
                break;
            default:
            System.out.println("Could not resolve intent to column name");
            break;
        }
        return  name;
    }



    public ArrayList<Company> getAICompanies() {

      ArrayList<Company> companies = new ArrayList<Company>();
      // Get Counts for each intent
      String query = ""
        + "SELECT ftc.CompanyCode,coalesce(NewsCount,0),coalesce(SpotPriceCount,0),coalesce(OpeningPriceCount,0),coalesce(AbsoluteChangeCount,0),coalesce(ClosingPriceCount,0),coalesce(percentageChangeCount,0),coalesce(newsAdjustment,0),coalesce(SpotPriceAdjustment,0),coalesce(OpeningPriceAdjustment,0),coalesce(AbsoluteChangeAdjustment,0),coalesce(ClosingPriceAdjustment,0),coalesce(percentageChangeAdjustment,0) "
        + "FROM FTSECompanies ftc "
        + "LEFT OUTER JOIN CompanyNewsCount cnc ON (cnc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanySpotPriceCount csc ON (csc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyOpeningPriceCount coc ON (coc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyAbsoluteChangeCount cac ON (cac.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyClosingPriceCount ccc ON (ccc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyPercentageChangeCount cpc ON (cpc.CompanyCode = ftc.CompanyCode)";


      Statement stmt = null;
      ResultSet rs = null;

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        while(rs.next()) {
          // Create list of intents for each company
          ArrayList<IntentData> intents = new ArrayList<>();
          // News counter
          float newsCount = (float) rs.getInt("coalesce(NewsCount,0)");
          // Intents
          float spot = (float) rs.getInt("coalesce(SpotPriceCount,0)");
          float opening = (float) rs.getInt("coalesce(OpeningPriceCount,0)");
          float absoluteChange = (float) rs.getInt("coalesce(AbsoluteChangeCount,0)");
          float closing = (float) rs.getInt("coalesce(ClosingPriceCount,0)");
          float percentageChange = (float) rs.getInt("coalesce(percentageChangeCount,0)");
          // Now the  adjustments
          // for news
          float newsAdj =  rs.getFloat("coalesce(newsAdjustment,0)");
          // and for intents
          float spotAdj =  rs.getFloat("coalesce(SpotPriceAdjustment,0)");
          float openingAdj =  rs.getFloat("coalesce(OpeningPriceAdjustment,0)");
          float absoluteChangeAdj =  rs.getFloat("coalesce(AbsoluteChangeAdjustment,0)");
          float closingPriceAdj =  rs.getFloat("coalesce(ClosingPriceAdjustment,0)");
          float percentageChangeAdj =  rs.getFloat("coalesce(percentageChangeAdjustment,0)");

          // Instantiate IntentData List for this company
          // TODO not having values for each intent for now
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
          System.out.println("No companies found, getAICompanies returning null");
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
      ArrayList<Company> companies = this.getAICompanies();
      ArrayList<Group> result = new ArrayList<>();
      if(companies == null) return null;

      // put all companies in a Map
      HashMap<String, Company> companiesMap = new HashMap<>();
      for(Company c: companies) {
        companiesMap.put(c.getCode(),c);
      }

      String query1 = "SELECT  GroupName ";
      query1+= "FROM FTSEGroupMappings ";

      Statement stmt = null;
      ResultSet rs = null;
      // groups map
      HashMap<String, Group> groupsMap = new HashMap<>();

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query1);
        // getting all the group names
        while (rs.next()) {
          String gName = rs.getString("GroupName");
          groupsMap.put(gName, new Group(gName));
        }
        // entry set iterator
        Set<Map.Entry<String,Group>> entrySet = groupsMap.entrySet();

        // retrieve all companies for each group
        for(Map.Entry<String,Group> g: entrySet) {
          String query2 = "SELECT CompanyCode FROM FTSECompanies NATURAL JOIN FTSEGroupMappings WHERE GroupName = '" + g.getKey()+"'";
          ResultSet rs0 = null;

          try {
            rs0 = stmt.executeQuery(query2);
            ArrayList<Company> companiesForThisGroup = new ArrayList<>();
            // put all the companies for this group in its list
            while(rs0.next()) {
              Company c = companiesMap.get(rs0.getString("CompanyCode"));
              companiesForThisGroup.add(c);
            }
            g.getValue().addCompanies(companiesForThisGroup);

            // add to final list
            result.add(g.getValue());
          } catch(SQLException e) {
            printSQLException(e);
          } finally {
            if(rs0 != null) {tryClose(rs0); }
          }
        }

        // now add the remaining values to each group
        for(Group g: result) {
          ArrayList<Company> com = g.getCompanies();
          int numberOfCompanies = com.size();

          Float priority = 0.0f;
          Float irrelevantSuggestionWeight = 0.0f;

          for(Company c: com) {
            priority+= c.getPriority();
            irrelevantSuggestionWeight+= c.getIrrelevantSuggestionWeight();
          }
          irrelevantSuggestionWeight/= numberOfCompanies;

          g.setPriority(priority);
          g.setIrrelevantSuggestionWeight(irrelevantSuggestionWeight);
        }

      } catch (SQLException ex) {
        printSQLException(ex);
      } finally {
        if (stmt != null) { tryClose(stmt); }
        if(rs != null) {tryClose(rs); }
      }

      return result;
    }
    //TODO
    private ArrayList<String> detectedImportantChange() {
      String query =  "SELECT PercentageChange, CompanyCode FROM FTSECompanySnapshots ORDER BY TimeOfData DESC LIMIT 1";

      ResultSet rs = null;
      Statement stmt = null;
      ArrayList<String> result = new ArrayList<>();

      HashMap<String, Float> companiesPercChangeMap = new HashMap<>();

      Float treshold = 0.0f;

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        while(rs.next()) {
          String companyName = rs.getString("CompanyCode");
          Float percChange = rs.getFloat("PercentageChange");

          if(percChange > treshold) {
            companiesPercChangeMap.put(companyName, percChange);
          }
        }

        Set<String> winningNames = companiesPercChangeMap.keySet();

        for(String s: winningNames) {
          result.add(s);
        }

      } catch (SQLException e) {
        printSQLException(e);
      } finally {
        if (stmt != null) { tryClose(stmt); }
        if(rs != null) {tryClose(rs); }
      }

      if(result.size() == 0) return null;
      return result;
    }


    //TODO
    private void onSuggestionIrrelevant() {
      
    }

    public IntentData getIntentForCompany() {
      return null;
    }

    // This is potentially not needed couple of methods as
    // the database will always be updated i.e.
    // the only changes the intelligence core makes locally
    // are on tallies and it will update the database accordingly immediately
    public void storeAICompanies(ArrayList<Company> companies) {

    }

    public void storeAIGroups(ArrayList<Group> groups) {

    }

    public String[] getCompaniesInGroup(String groupName){
        groupName.toLowerCase();
        ArrayList<String> companies = new ArrayList<String>();
        ResultSet r1 = null;
        Statement s1 = null;
        try {
            String query = "SELECT CompanyName from FTSECompanies ";
            query += "INNER JOIN FTSEGroupMappings ON (FTSECompanies.CompanyCode = FTSEGroupMappings.CompanyCode) ";
            query += "WHERE FTSEGroupMappings.GroupName = '"+groupName+"'";
            s1 = conn.createStatement();
            r1 = s1.executeQuery(query);
            while(r1.next()){
                companies.add(r1.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Couldn't resolve group name");
            return null;
        } finally {
          if (s1 != null) { tryClose(s1); }
          if(r1 != null) {tryClose(r1); }
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
              if (ignoreSQLException(((SQLException)e).getSQLState()) == false) {

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

    public static boolean ignoreSQLException(String sqlState) {

      if (sqlState == null) {
          System.out.println("The SQL state is not defined!");
          return false;
      }

      // X0Y32: Jar file already exists in schema
      if (sqlState.equalsIgnoreCase("X0Y32"))
          return true;

      // 42Y55: Table already exists in schema
      if (sqlState.equalsIgnoreCase("42Y55"))
          return true;

      return false;
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

    //Similar methods for rollback and commit
    private void trySetAutoCommit(Boolean b){
        try{
            conn.setAutoCommit(b);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void tryRollback(){
        try{
            conn.rollback();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void tryCommit(){
        try{
            conn.commit();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
