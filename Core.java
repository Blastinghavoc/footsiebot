package footsiebot;

import footsiebot.nlp.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.*;//May not be needed


public class Core{

  public static void main(String[] args){
    INaturalLanguageProcessor p = new NLPCore();
    while(true){
      ParseResult result = p.parse(readEntry("Enter a query:\n"));
      if(result == null){
	System.out.println("Sorry, I did not understand the query.");
      }
      System.out.println(result);
    }
    
    
    //connect();

  }
  
  private static String readEntry(String prompt) //Nicked from Databases worksheets, can't be included in final submission DEBUG
	{
		try 
		{
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
		catch (IOException e) 
		{
			return "";
		}
 	}
  
  public static void connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:./database/sqlite/test.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }


}
