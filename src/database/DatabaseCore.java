package footsiebot.database;

import footsiebot.nlp.*;
import footsiebot.datagathering.ScrapeResult;
import footsiebot.ai.*;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

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

    /* Stores FTSE data in database */
    public boolean storeScraperResults(ScrapeResult sr) {

        // need to delete old FTSE data

        int numCompanies = 100;//Constant
        Float price, absChange, percChange = 0.0f;
        String code, group, name = " ";
        Statement s1 = null;
        PreparedStatement s2 = null;
        PreparedStatement s3 = null;
        Statement s4 = null;

        String checkNewCompanyQuery = null;
        String addNewCompanyQuery = null;
        String addCompanyGroupQuery = null;
        String addScrapeResultQuery = null;
        ResultSet companyCheck = null;

        trySetAutoCommit(false); // Will treat the following as a transaction, so that it can be rolled back if it fails

        // may not need if storing older data as well
        deleteOldFTSEData();

        // store all scraper data in database
        for (int i = 0; i < numCompanies; i++) {
            code = sr.getCode(i).toLowerCase();

            // Remove punctuation if present
            if (code.endsWith(".")) {
                code = code.substring(0, code.length() - 1);
            }

            group = sr.getGroup(i).toLowerCase();
            name = sr.getName(i).toLowerCase();
            price = sr.getPrice(i);
            absChange = sr.getAbsChange(i);
            percChange = sr.getPercChange(i);

            checkNewCompanyQuery = null; //Reseting
            addNewCompanyQuery = null;
            addCompanyGroupQuery = null;
            addScrapeResultQuery = null;

            try {

                // if the company is a new FTSE company, add it to the FTSECompanies and FTSEGroupMappings table
                checkNewCompanyQuery 	= "SELECT * FROM FTSECompanies "
                						+ "WHERE CompanyCode = '" + code + "'";
                s1 = conn.createStatement();
                companyCheck = s1.executeQuery(checkNewCompanyQuery);
                if (!companyCheck.next()) {
                    addNewCompanyQuery 	= "INSERT INTO FTSECompanies "
                                        + "VALUES(?,?)";
                    s2 = conn.prepareStatement(addNewCompanyQuery);//Must be prepared statement to deal with names with quotes in
                    s2.setString(1,code);
                    s2.setString(2,name);
                    s2.executeUpdate();

                    addCompanyGroupQuery 	= "INSERT INTO FTSEGroupMappings "
                                        	+ "VALUES(?,?)";
                    s3 = conn.prepareStatement(addCompanyGroupQuery);
                    s3.setString(1,group);
                    s3.setString(2,code);
                    s3.executeUpdate();
                }

                // add the company data into the FTSECompanySnapshots table
                addScrapeResultQuery 	= "INSERT INTO FTSECompanySnapshots "
                						+ "(CompanyCode, SpotPrice, PercentageChange, AbsoluteChange) "
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

    /* Deletes FTSE data from over 5 trading days ago
    MAY NOT NEED */
    private void deleteOldFTSEData() {

        LocalDateTime currentTime = LocalDateTime.now();
        String comparisonTime = getComparisonTime(currentTime);
        Statement s1 = null;

        try {
            s1 = conn.createStatement();
            String query    = "DELETE FROM FTSECompanySnapshots\n"
                            + "WHERE DATETIME(TimeOfData, '+7 days') <= '"
                            + comparisonTime + "'";
            s1.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
            tryClose(s1);
        }

        tryClose(s1);
    }

    /*Probably doesn't actually need the time. The database can do that
    automatically
    */
    public boolean storeQuery(ParseResult pr, LocalDateTime date) {

        // if("DEBUG".equals("DEBUG")){
        //     return false;//DEBUG
        // }

        //TODO: need branch to process group queries sepparately.
        if(pr.isOperandGroup()){
            return true;
        }

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

    /* Returns the FTSE data asked for as well as other information about the
    company */
    public String[] getFTSE(ParseResult pr) {

    	footsiebot.nlp.Intent intent = pr.getIntent();
    	ArrayList<String> output = new ArrayList<String>();

    	/* call relevant method to get a query for the intent data or a
    	percentage change if the intent is to get trend data */

    	switch (intent) {
    		case SPOT_PRICE:
    		case TRADING_VOLUME:
    		case PERCENT_CHANGE:
    		case ABSOLUTE_CHANGE:
    		case OPENING_PRICE:
    		case CLOSING_PRICE:

    			/* get query for data required, execute it, add result to first
    			 index of array list */

    			String FTSEQuery = convertFTSEQuery(pr);
		        Statement s1 = null;
		        ResultSet results = null;

		        if (FTSEQuery == null || FTSEQuery.isEmpty()) {
		            System.out.println("Null query"); //DEBUG
		            return null;
		        }

		        // add asked for data to first index of array
		        try {
		            s1 = conn.createStatement();
		            results = s1.executeQuery(FTSEQuery);
		            if (!results.next()) {
		                String nullArr[] = null;
		                return nullArr; // return null array if no results
		            } else {
		                do {
		                    output.add(((Float)results.getFloat(1)).toString());
		                } while (results.next());
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		            tryClose(s1,results);
		        }
		        tryClose(s1, results);
    			break;
    		default:
    			break;
    	}

        // add other company data to array list
        switch (intent) {
        	case TREND:
        		output.addAll(getTrendData(pr));
        		break;
        	case GROUP_FULL_SUMMARY:
        		output.addAll(getGroupData(pr));
        		break;
            case OPENING_PRICE:
            case CLOSING_PRICE:
                output.add("Date, " + timeSpecifierToDate(pr.getTimeSpecifier()));
            default:
            	// add other data about company to other indexes of the array
        		output.addAll(getAllCompanyInfo(pr));
                break;
        }

        return output.toArray(new String[1]);
    }

    /* Returns average percentage change for a group over time period specified
    and whether a group is rising or falling */
    private ArrayList<String> getGroupData(ParseResult pr) {
    	ArrayList<String> output = new ArrayList<String>();
    	Float percChange = 0.0f;
    	Float percChangeTotal = 0.0f;
    	Float averagePercChange = 0.0f;
    	String groupName = pr.getOperand();
    	String[] companies = getCompaniesInGroup(groupName);
    	footsiebot.nlp.TimeSpecifier timeSpec = pr.getTimeSpecifier();
    	String comparisonTime = "";
    	HashMap<String, Float> spotPriceMap = new HashMap<String, Float>();
    	HashMap<String, Float> percChangeMap = new HashMap<String, Float>();
    	ArrayList<Float> spotPrices = new ArrayList<Float>();
    	ArrayList<Float> percChanges = new ArrayList<Float>();
    	Float maxSpotPrice = 0.0f;
    	Float minSpotPrice = 0.0f;
    	Float maxPercChange = 0.0f;
    	Float minPercChange = 0.0f;
    	String companyWithMaxSpotPrice, companyWithMinSpotPrice, companyWithMaxPercChange, companyWithMinPercChange;

    	String spotOrClosingPriceQry = "";
    	Statement s1 = null;
    	ResultSet results = null;

    	comparisonTime = timeSpecifierToDate(timeSpec);

    	/* gets percentage change and spot price/ closing price for each company
    	in group */
    	for (int i = 0; i < companies.length; i ++) {
    		percChange = getTrendDataOnDate(companies[i], timeSpec).get(0);
			percChangeTotal += percChange;
			percChangeMap.put(companies[i], percChange);

			/* gets spot price if the time specifier is today, otherwise gets
			closing price */
			spotOrClosingPriceQry = spotOrClosingPriceQuery(timeSpec, companies[i], comparisonTime);
			try {
				s1 = conn.createStatement();
				results = s1.executeQuery(spotOrClosingPriceQry);
				while (results.next()) {
					spotPriceMap.put(companies[i], results.getFloat(1));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				tryClose(s1, results);
			}
    	}
    	tryClose(s1, results);

    	// if no data stored for time specifier
    	if (spotPriceMap.isEmpty()) {
    		return output;
    	}

    	// calculates average percentage change for whole group and whether
    	// the overall group is rising or falling
    	averagePercChange = percChangeTotal/ companies.length;
    	output.add(averagePercChange.toString());
    	if (averagePercChange > 0) {
			output.add("risen");
    	} else if (averagePercChange < 0) {
			output.add("fallen");
		} else {
			output.add("stayed the same");
		}

		Comparator<? super Map.Entry<String, Float>> valueComparator = ((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()));

		maxSpotPrice = Collections.max(spotPriceMap.entrySet(), valueComparator).getValue();
		companyWithMaxSpotPrice = Collections.max(spotPriceMap.entrySet(), valueComparator).getKey();
		minSpotPrice = Collections.min(spotPriceMap.entrySet(), valueComparator).getValue();
		companyWithMinSpotPrice = Collections.min(spotPriceMap.entrySet(), valueComparator).getKey();
		maxPercChange = Collections.max(percChangeMap.entrySet(), valueComparator).getValue();
		companyWithMaxPercChange = Collections.max(percChangeMap.entrySet(), valueComparator).getKey();
		minPercChange = Collections.min(percChangeMap.entrySet(), valueComparator).getValue();
		companyWithMinPercChange = Collections.min(percChangeMap.entrySet(), valueComparator).getKey();

		output.add(companyWithMaxSpotPrice + ", " + maxSpotPrice.toString());
		output.add(companyWithMinSpotPrice + ", " + minSpotPrice.toString());
		output.add(companyWithMaxPercChange + ", " + maxPercChange.toString());
		output.add(companyWithMinPercChange + ", " + minPercChange.toString());

		System.out.println("PERC CHANGE " + output.get(0) + " " + output.get(1));
		System.out.println(output.get(2));
		System.out.println(output.get(3));
		System.out.println(output.get(4));
		System.out.println(output.get(5));
    	return output;
    }

    /* Returns an array list  containing the data to be output by the Core
    for trend data */
    private ArrayList<String> getTrendData(ParseResult pr) {

    	ArrayList<Float> trendData = new ArrayList<Float>();
    	ArrayList<String> output = new ArrayList<String>();
    	footsiebot.nlp.Intent intent = pr.getIntent();
        footsiebot.nlp.TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String companyCode = pr.getOperand();
        Boolean isGroup = pr.isOperandGroup();
        Float percChange = 0.0f;
        Float startPrice = 0.0f;
        Float endPrice = 0.0f;

    	switch (intent) {
    		case TREND:
    			trendData = getTrendDataOnDate(companyCode, timeSpec);
    			break;
            // case SINCE_TREND:
    		// 		percChange = getTrendDataSinceDate(companyCode, timeSpec);
    		//		break;
    		default:
    			break;
    	}
    	percChange = trendData.get(0);
    	startPrice = trendData.get(1);
    	endPrice = trendData.get(2);

		output.add(percChange.toString());

		if (percChange > 0)
			output.add("risen");
		else if (percChange < 0) {
			output.add("fallen");
		} else {
			output.add("stayed the same");
		}
		output.add(startPrice.toString());
		output.add(endPrice.toString());

		System.out.println("START PRICE: " + startPrice + " END PRICE " + endPrice + " PERC CHANGE " + percChange);

    	return output;
    }

    /* Returns array list containing percentage change in spot price,
    opening price and closing price or spot price(if time specifier is today)
    for a company on day specified */
    private ArrayList<Float> getTrendDataOnDate(String companyCode, footsiebot.nlp.TimeSpecifier timeSpec) {
    	LocalDateTime currentTime = LocalDateTime.now();
        String comparisonTime = "";
        ArrayList<Float> trendData = new ArrayList<Float>();
        String startTimeQuery, endTimeQuery = null;
    	Statement s1 = null;
        Statement s2 = null;
        ResultSet startPriceRS = null;
        ResultSet endPriceRS = null;
        Float startPrice = 0.0f;
        Float endPrice = 0.0f;
        Float percChange = 0.0f;

    	// query to get opening price of day specified
   		comparisonTime = timeSpecifierToDate(timeSpec);
   		startTimeQuery 	= "SELECT SpotPrice FROM FTSECompanySnapshots\n"
             			+ "WHERE CompanyCode = '" + companyCode
             			+ "' AND DATE(TimeOfData) <= '" + comparisonTime + "'\n"
             			+ "ORDER BY TimeOfData ASC LIMIT 1";

   		endTimeQuery = spotOrClosingPriceQuery(timeSpec, companyCode, comparisonTime);

   		/* If able to get start and end prices, calculate the percentage
   		change between them */
   		try {
   			s1 = conn.createStatement();
   			s2 = conn.createStatement();
   			startPriceRS = s1.executeQuery(startTimeQuery);
   			endPriceRS = s2.executeQuery(endTimeQuery);

   			while (startPriceRS.next()) {
   				startPrice = startPriceRS.getFloat(1);
   			}

   			while (endPriceRS.next()) {
   				endPrice = endPriceRS.getFloat(1);
   			}

   			if (startPrice != 0.0f && endPrice != 0.0f) {
   				percChange = ((endPrice - startPrice) / startPrice) * 100;
   			} else {
   				System.out.println("Null start or end price");
   			}

   		} catch (SQLException e) {
   			e.printStackTrace();
   			tryClose(s1, startPriceRS);
   			tryClose(s2, endPriceRS);
   		}

   		tryClose(s1, startPriceRS);
   		tryClose(s2, endPriceRS);

   		trendData.add(percChange);
   		trendData.add(startPrice);
   		trendData.add(endPrice);

   		return trendData;
    }

    /* if the time specifier is today, returns query to get spot price of
    company, otherwise returns query to get closing price of company on
    specified day*/
    private String spotOrClosingPriceQuery(footsiebot.nlp.TimeSpecifier timeSpec, String companyCode, String comparisonTime) {
   		String query = "";
   		if (timeSpec == footsiebot.nlp.TimeSpecifier.TODAY) {
   			query 	= "SELECT SpotPrice FROM FTSECompanySnapshots\n"
                			+ "WHERE CompanyCode = '" + companyCode + "'";
   		} else {
   			query   	= "SELECT SpotPrice FROM FTSECompanySnapshots\n"
	                        + "WHERE CompanyCode = '" + companyCode
	                        + "' AND DATE(TimeOfData) <= '" + comparisonTime + "'\n"
	                        + "ORDER BY TimeOfData DESC LIMIT 1";
   		}
   		return query;
    }

    /* Returns array list containing percentage change in spot price,
    opening price and closing price or spot price(if time specifier is today)
    for a company since the day specified */
    private ArrayList<Float> getPercChangeSinceDate(String companyCode, footsiebot.nlp.TimeSpecifier timeSpec) {
    	ArrayList<Float> trendData = new ArrayList<Float>();
    	LocalDateTime currentTime = LocalDateTime.now();
        String comparisonTime = "";
        String spotPriceQuery, openingPriceQuery = null;
    	Statement s1 = null;
        Statement s2 = null;
        ResultSet spotPriceRS = null;
        ResultSet openingPriceRS = null;
        Float spotPrice = 0.0f;
        Float openingPrice = 0.0f;
        Float percChange = 0.0f;

    	comparisonTime = timeSpecifierToDate(timeSpec);
    	spotPriceQuery 	= "SELECT SpotPrice FROM FTSECompanySnapshots\n"
                		+ "WHERE CompanyCode = '" + companyCode;
        openingPriceQuery 	= "SELECT SpotPrice FROM FTSECompanySnapshots\n"
                			+ "WHERE CompanyCode = '" + companyCode
                			+ "' AND DATE(TimeOfData) <= '" + comparisonTime + "'\n"
                			+ "ORDER BY TimeOfData ASC LIMIT 1";
        try {
        	s1 = conn.createStatement();
        	s2 = conn.createStatement();
        	spotPriceRS = s1.executeQuery(spotPriceQuery);
        	openingPriceRS = s2.executeQuery(openingPriceQuery);

        	while (spotPriceRS.next()) {
        		spotPrice = spotPriceRS.getFloat(1);
        	}

        	while (openingPriceRS.next()) {
        		openingPrice = openingPriceRS.getFloat(1);
        	}

        	if (spotPrice != 0.0f && openingPrice != 0.0f) {
   				percChange = ((openingPrice - spotPrice) / openingPrice) * 100;
   			} else {
   				System.out.println("Null start or spot price");
   			}
        } catch (SQLException e) {
        	e.printStackTrace();
        	tryClose(s1, spotPriceRS);
        	tryClose(s2, openingPriceRS);
        }

    	tryClose(s1, spotPriceRS);
     	tryClose(s2, openingPriceRS);

     	trendData.add(percChange);
   		trendData.add(openingPrice);
   		trendData.add(spotPrice);

    	return trendData;
    }

    public String convertScrapeResult(ScrapeResult sr) {
        return null;
    }

    public String convertQuery(ParseResult pr, LocalDateTime date) {
        return null;
    }

    /* Returns an SQL query to get the FTSE data required in the parse result */
    public String convertFTSEQuery(ParseResult pr) {
        footsiebot.nlp.Intent intent = pr.getIntent();
        footsiebot.nlp.TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String companyCode = pr.getOperand();
        Boolean isGroup = pr.isOperandGroup();

        String query = "";
        String timeSpecifierSQL = "";
        Boolean isFetchCurrentQuery = false;
        String colName = "";

        LocalDateTime currentTime = LocalDateTime.now();
        String comparisonTime = "";

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
                comparisonTime = timeSpecifierToDate(timeSpec);
                query   = "SELECT SpotPrice FROM FTSECompanySnapshots\n"
                        + "WHERE CompanyCode = '" + companyCode
                        + "' AND DATE(TimeOfData) <= '" + comparisonTime + "'\n"
                        + "ORDER BY TimeOfData ASC LIMIT 1";
                break;
            case CLOSING_PRICE:
                comparisonTime = timeSpecifierToDate(timeSpec);
                query   = "SELECT SpotPrice FROM FTSECompanySnapshots\n"
                        + "WHERE CompanyCode = '" + companyCode
                        + "' AND DATE(TimeOfData) <= '" + comparisonTime + "'\n"
                        + "ORDER BY TimeOfData DESC LIMIT 1";
                break;
            case GROUP_FULL_SUMMARY:
                break;
            default:
                System.out.println("No cases ran");
            	break;
        }

        // get current data requested from database
        if (isFetchCurrentQuery) {
            query   = "SELECT " + colName + " FROM FTSECompanySnapshots\n"
                    + "WHERE CompanyCode = '" + companyCode +
                    "' ORDER BY TimeOfData DESC LIMIT 1";
        }

        return query;
    }

    /* Converts time specifier to date */
    private String timeSpecifierToDate(TimeSpecifier t) {

        LocalDateTime date = LocalDateTime.now();
        //DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = "";

        switch (t) {
            // for today and yesterday first gets most recent trading day in
            // in case it is a non trading day
            case TODAY:
                formattedDate = getComparisonTime(date);
                return formattedDate;
            case YESTERDAY:
                formattedDate = getComparisonTime(date.minusDays(1));
                return formattedDate;
            case LAST_MONDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != java.time.DayOfWeek.MONDAY);
                break;
            case LAST_TUESDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != java.time.DayOfWeek.TUESDAY);
                break;
            case LAST_WEDNESDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != java.time.DayOfWeek.WEDNESDAY);
                break;
            case LAST_THURSDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != java.time.DayOfWeek.THURSDAY);
                break;
            case LAST_FRIDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != java.time.DayOfWeek.FRIDAY);
                break;
        }

        formattedDate = date.format(dateFormatter);
        return formattedDate;
    }

    /* Returns the most current date on a trading day or the date of the most
    recent trading day on a non trading day */
    private String getComparisonTime(LocalDateTime currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String comparisonTime;
        switch (currentTime.getDayOfWeek()) {
            case SATURDAY:
                comparisonTime = (currentTime.minusDays(1)).format(formatter);
                break;
            case SUNDAY:
                comparisonTime = (currentTime.minusDays(2)).format(formatter);
                break;
            default:
                comparisonTime = currentTime.format(formatter);
                break;
        }

        return comparisonTime;

    }

    /* Returns all other information stored about a company except the
    information asked for by the user */
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
            case TREND:
                name = "CompanyTrendCount";
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

          // intent priorities
          float spotPriority = spot - spotAdj;
          float openingPriority = opening - openingAdj;
          float closingPriority = closing - closingPriceAdj;
          float absoluteChangePriority = absoluteChange - absoluteChangeAdj;
          float percentageChangePriority = percentageChange - percentageChangeAdj;
          // news
          float newsPriority = newsCount - newsAdj;

          // Instantiate IntentData List for this company
          // TODO not having values for each intent for now
          intents.add(new IntentData(AIIntent.SPOT_PRICE, spot, spotAdj));
          intents.add(new IntentData(AIIntent.OPENING_PRICE, opening, openingAdj));
          intents.add(new IntentData(AIIntent.ABSOLUTE_CHANGE, absoluteChange, absoluteChangeAdj));
          intents.add(new IntentData(AIIntent.CLOSING_PRICE, closing, closingPriceAdj));
          intents.add(new IntentData(AIIntent.PERCENT_CHANGE, percentageChange, percentageChangeAdj));

          HashMap<AIIntent, Float[]> mapping = new HashMap<>();

          mapping.put(AIIntent.SPOT_PRICE, new Float[]{spot, spotAdj});
          mapping.put(AIIntent.OPENING_PRICE, new Float[]{opening, openingAdj});
          mapping.put(AIIntent.CLOSING_PRICE, new Float[]{closing, closingPriceAdj});
          mapping.put(AIIntent.PERCENT_CHANGE, new Float[]{percentageChange,percentageChangeAdj });
          mapping.put(AIIntent.ABSOLUTE_CHANGE, new Float[]{absoluteChange, absoluteChangeAdj});


          // Calculate priority for each company
          Float intentScale = 1.0f;
          Float newsScale = 1.0f;
          float priority = intentScale * (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority) + newsScale * (newsPriority);
          // average of all intent's irrelevantSuggestionWeight

          companies.add(new Company(rs.getString("CompanyCode"), intents, mapping, intentScale, newsScale, newsCount, newsAdj));
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
        // for(Map.Entry<String,Group> g: entrySet) {
        //   String query2 = "SELECT CompanyCode FROM FTSECompanies NATURAL JOIN FTSEGroupMappings WHERE GroupName = '" + g.getKey()+"'";
        //   ResultSet rs0 = null;
        //
        //   try {
        //     rs0 = stmt.executeQuery(query2);
        //     ArrayList<Company> companiesForThisGroup = new ArrayList<>();
        //     // put all the companies for this group in its list
        //     while(rs0.next()) {
        //       Company c = companiesMap.get(rs0.getString("CompanyCode"));
        //       companiesForThisGroup.add(c);
        //     }
        //     g.getValue().addCompanies(companiesForThisGroup);
        //
        //     // add to final list
        //     result.add(g.getValue());
        //   } catch(SQLException e) {
        //     printSQLException(e);
        //   } finally {
        //     if(rs0 != null) {tryClose(rs0); }
        //   }
        // }

        for(Map.Entry<String,Group> g: entrySet) {
        }

        // now add the remaining values to each group
        for(Group g: result) {
          ArrayList<Company> com = g.getCompanies();
          int numberOfCompanies = com.size();

          Float priority = 0.0f;

          for(Company c: com) {
            priority+= c.getPriority();
          }

          g.setPriority(priority);
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
    public ArrayList<String> detectedImportantChange() {
      String query =  "SELECT PercentageChange, CompanyCode FROM FTSECompanySnapshots ORDER BY TimeOfData DESC LIMIT 1";

      ResultSet rs = null;
      Statement stmt = null;
      ArrayList<String> result = new ArrayList<>();

      HashMap<String, Float> companiesPercChangeMap = new HashMap<>();
      // TODO
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
    public void onSuggestionIrrelevant(Company company, AIIntent intent, boolean isNews) {
      String table = "";
      if(!isNews) {
        switch(intent) {
          case SPOT_PRICE: table+= "CompanySpotPriceCount";
          break;
          case OPENING_PRICE: table+= "CompanyOpeningPriceCount";
          break;
          case CLOSING_PRICE: table+= "CompanySpotPriceCount";
          break;
          case PERCENT_CHANGE: table+= "CompanySpotPriceCount";
          break;
          case ABSOLUTE_CHANGE: table+= "CompanySpotPriceCount";
          break;
        }
      } else {
        // is news
        table+= "CompanyNewsCount";
      }

      String column = table.replace("Company", "").replace("Count", "Adjustment");
      // TODO exponentially
      String query = "UPDATE " + table + " SET " + column + " = "+column+" + 1 + (0.5 * " + column  + ")";
      Statement stmt = null;

      try {
        stmt = conn.createStatement();
        stmt.executeUpdate(query);

      } catch (SQLException e) {
        printSQLException(e);
      } finally {
        if (stmt != null) { tryClose(stmt); }
      }

    }

    //TODO
    // private void onSuggestionIrrelevant(Group group) {
    //
    //
    // }


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
            String query = "SELECT FC.CompanyCode from FTSECompanies FC ";
            query += "INNER JOIN FTSEGroupMappings ON (FC.CompanyCode = FTSEGroupMappings.CompanyCode) ";
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
