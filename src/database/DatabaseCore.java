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

  public DatabaseCore(){
      // load the sqlite-JDBC driver using the current class loader
      try{
          Class.forName("org.sqlite.JDBC");
        }
        catch(Exception e){
            e.printStackTrace();
        }

      Connection connection = null;
      try
      {
        // create a database connection
        connection = DriverManager.getConnection("jdbc:sqlite:src/database/footsie_db.db");
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

        statement.executeUpdate("INSERT INTO FTSECompanies VALUES('BAR', 'Barclays')");
        ResultSet rs = statement.executeQuery("select * from FTSECompanies");
        while(rs.next())
        {
          // read the result set
          System.out.println("code = " + rs.getString("CompanyCode"));
          System.out.println("name = " + rs.getString("CompanyName"));
        }
      }
      catch(SQLException e)
      {
        // if the error message is "out of memory",
        // it probably means no database file is found
        System.err.println(e.getMessage());
      }
      finally
      {
        try
        {
          if(connection != null)
            connection.close();
        }
        catch(SQLException e)
        {
          // connection close failed.
          System.err.println(e);
        }
      }

  }

  public boolean storeScraperResults(ScrapeResult sr) {
	return false;
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

  public ArrayList<Company> getAICompanies() {
	return null;
  }

  public ArrayList<Group> getAIGroups() {
	return null;
  }

  private IntentData getIntentForCompany() {
	return null;
  }

  public void storeAICompanies(ArrayList<Company> companies) {

  }

  public void storeAIGroups(ArrayList<Group> groups) {

  }

}
