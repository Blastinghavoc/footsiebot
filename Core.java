package footsiebot;

import footsiebot.nlpcore.*;
import footsiebot.intelligencecore.*;
import footsiebot.datagatheringcore.*;
import footsiebot.guicore.*;
import footsiebot.databasecore.*;
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


}
