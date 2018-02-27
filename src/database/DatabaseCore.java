package footsiebot.database;

import footsiebot.nlp.TimeSpecifier;
import footsiebot.nlp.Intent;
import footsiebot.nlp.ParseResult;
import footsiebot.datagathering.ScrapeResult;
import footsiebot.ai.*;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.*;
import java.lang.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.lang.Integer;
import java.util.Currency;
import java.util.Locale;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
            conn = DriverManager
                    .getConnection("jdbc:sqlite:src/database/footsie_db.db");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
    * Stores FTSE data in the database
    *
    * @param sr The scrape result given by the web scraper
    * @return true if the FTSE data is successfully store, false otherwise
    */
    public boolean storeScraperResults(ScrapeResult sr) {

        int numCompanies = 100; // Constant
        int tradingVolume = 0;
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

        // Will treat the following as a transaction, so that it can be rolled
        // back if it fails
        trySetAutoCommit(false);

        deleteOldFTSEData();

        // Store all scraper data in the database
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
            tradingVolume = sr.getVolume(i);

            checkNewCompanyQuery = null; // Reseting
            addNewCompanyQuery = null;
            addCompanyGroupQuery = null;
            addScrapeResultQuery = null;

            try {

                // If the company is a new FTSE company, add it to the
                // FTSECompanies and FTSEGroupMappings table
                checkNewCompanyQuery    = "SELECT * FROM FTSECompanies "
                                        + "WHERE CompanyCode = '" + code + "'";
                s1 = conn.createStatement();
                companyCheck = s1.executeQuery(checkNewCompanyQuery);
                if (!companyCheck.next()) {
                    addNewCompanyQuery  = "INSERT INTO FTSECompanies "
                                        + "VALUES(?,?)";
                    s2 = conn.prepareStatement(addNewCompanyQuery);
                    s2.setString(1,code);
                    s2.setString(2,name);
                    s2.executeUpdate();

                    addCompanyGroupQuery    = "INSERT INTO FTSEGroupMappings "
                                            + "VALUES(?,?)";
                    s3 = conn.prepareStatement(addCompanyGroupQuery);
                    s3.setString(1,group);
                    s3.setString(2,code);
                    s3.executeUpdate();
                }

                // Add the company data into the FTSECompanySnapshots table
                addScrapeResultQuery    = "INSERT INTO FTSECompanySnapshots "
                                        + "(CompanyCode, SpotPrice, "
                                        + "PercentageChange, AbsoluteChange, "
                                        + "TradingVolume) "
                                        + "VALUES('" + code + "', " + price
                                        + ", " + percChange + ", " + absChange
                                        + "," + tradingVolume + ")";
                s4 = conn.createStatement();
                s4.executeUpdate(addScrapeResultQuery);

            } catch (SQLException e) {
                e.printStackTrace();
                tryRollback();
                trySetAutoCommit(true);
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

    /* 
    * Deletes FTSE data from over 5 trading days ago
    *
    */
    private void deleteOldFTSEData() {

        LocalDateTime currentTime = LocalDateTime.now();
        String comparisonTime = getMostRecentTradingDay(currentTime);
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

    /**
    * Stores the user's queries
    *
    * @param pr The parse result from the user's input
    * @param date The date and time the query was made
    * @return true if the query was successfully stored, false otherwise
    */
    public boolean storeQuery(ParseResult pr, LocalDateTime date) {

        if (pr.isOperandGroup()) {
            return true;
        }

        String companyCode = pr.getOperand();
        String intent = pr.getIntent().toString();
        String timeSpecifier = pr.getTimeSpecifier().toString();

        String query    = "INSERT INTO Queries"
                        + "(CompanyCode, Intent, TimeSpecifier) "
                        + "VALUES('" + companyCode + "','" + intent + "','" 
                        + timeSpecifier + "')";
        Statement s1 = null;
        ResultSet r1 = null;
        String table = intentToTableName(pr.getIntent());
        if (table == null) {
            return false;
        }
        // The count row in the tables has the same name as the table, 
        // minus the prefix "Company"
        String rowName = table.replace("Company","");
        trySetAutoCommit(false);
        try {
            s1 = conn.createStatement();
            s1.executeUpdate(query);

            // Check if a row with the relevant CompanyCode exists in the 
            // relevant count table. If not, create that row. If it does, 
            // increment the value of the relevant count row (rowName)
            query   = "SELECT * FROM " + table + " WHERE CompanyCode = '" 
                    + companyCode + "'";
            r1 = s1.executeQuery(query);
            // If the row does not exist, create it.
            if (!r1.next()) {
                query   = "INSERT INTO " + table + " VALUES ('" + companyCode 
                        + "',1,0)";
                s1.executeUpdate(query);
            } else {
                // Row does exist, so just increment the count.
                query   = "UPDATE " + table + " SET " + rowName + " = " 
                        + rowName + " + 1 WHERE CompanyCode = '" + companyCode 
                        + "'";
                s1.executeUpdate(query);
            }
            tryCommit();
        } catch (SQLException e) {
            e.printStackTrace();
            tryClose(s1, r1);
            tryRollback();
            trySetAutoCommit(true);
            return false;
        }
        tryClose(s1, r1);
        trySetAutoCommit(true);
        return true;
    }

    /**
    * Returns the FTSE data asked for as well as other information about the
    * company
    *
    * @param pr The parse result of the user's input
    * @return An array list of strings containing the FTSE data requested and
    * other infomation about the company to be output
    * */
    @SuppressWarnings("fallthrough")
    public String[] getFTSE(ParseResult pr) {

        Intent intent = pr.getIntent();
        ArrayList<String> output = new ArrayList<>();

        // Call relevant method to get a query for the intent data or a
        // percentage change if the intent is to get trend data
        switch (intent) {
            case SPOT_PRICE:
                // fall through
            case TRADING_VOLUME:
                // fall through
            case PERCENT_CHANGE:
                // fall through
            case ABSOLUTE_CHANGE:
                // fall through
            case OPENING_PRICE:
                // fall through
            case CLOSING_PRICE:

                // Creates the query to get the data required, executes it and
                // adds the result to the first index of output array list
                String FTSEQuery = convertFTSEQuery(pr);
                Statement s1 = null;
                ResultSet results = null;

                if (FTSEQuery == null || FTSEQuery.isEmpty()) {
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
                        switch (intent) {
                            case TRADING_VOLUME:
                                do {
                                    output.add(Integer.toString(results
                                            .getInt(1)));
                                } while (results.next());
                                break;
                            case SPOT_PRICE:
                                // fall through
                            case ABSOLUTE_CHANGE:
                                // fall through
                            case OPENING_PRICE:
                                // fall through
                            case CLOSING_PRICE:
                                // fall though
                                do {
                                    output.add(convertToGBX(((Float)results
                                            .getFloat(1))));
                                } while (results.next());
                                break;
                            case PERCENT_CHANGE:
                                do {
                                    output.add(roundPercentage(((Float)results
                                            .getFloat(1))).toString());
                                } while (results.next());
                                break;
                        }
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
                // fall through
            case TREND_SINCE:
                output.addAll(getTrendData(pr));
                break;
            case GROUP_FULL_SUMMARY:
                output.addAll(getGroupData(pr));
                break;
            case OPENING_PRICE:
                // fall through
            case CLOSING_PRICE:
                output.add("Date, " + timeSpecifierToDate
                        (pr.getTimeSpecifier()));
                // fall through
            default:
                // add other data about company to other indexes of the array
                output.addAll(getAllCompanyInfo(pr));
                break;
        }

        return output.toArray(new String[1]);
    }

    /**
    * Gets data about a group of companies over the time period specified
    *
    * @param pr The parse result from the user's input
    * @return Array list of strings containing the average percentage change
    * for a group over time period specified, whether a group is rising or
    * falling, the company with maximum spot price, the company with the minimum
    * spot price, the company with the maxium percentage change, the company
    * with the minimum percentage change
    */
    private ArrayList<String> getGroupData(ParseResult pr) {

        ArrayList<String> output = new ArrayList<>();
        HashMap<String, Float> spotPriceMap = new HashMap<>();
        HashMap<String, Float> percChangeMap = new HashMap<>();
        ArrayList<Float> spotPrices = new ArrayList<>();
        ArrayList<Float> percChanges = new ArrayList<>();
        Float percChangeTotal = 0.0f;
        String groupName = pr.getOperand();
        String[] companies = getCompaniesInGroup(groupName);
        TimeSpecifier timeSpec = pr.getTimeSpecifier();
        Statement s1 = null;
        ResultSet results = null;
        String date = timeSpecifierToDate(timeSpec);

        // Gets percentage change and spot price/ closing price for each company
        // in group
        for (int i = 0; i < companies.length; i ++) {
            ArrayList<Float> tmp = getTrendDataOnDate(companies[i], timeSpec);
            if (tmp == null || tmp.size() == 0){
                return new ArrayList<String>(); // Returns an empty result
            }
            Float percChange = tmp.get(0);
            percChangeTotal += percChange;
            percChangeMap.put(companies[i], percChange);

            // Gets spot price if the time specifier is today, otherwise gets
            // closing price
            String spotOrClosingPriceQry = spotOrClosingPriceQuery(timeSpec,
                    companies[i], date);
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

        // If no data stored for time specifier, return null array
        if (spotPriceMap.isEmpty()) {
            return output;
        }

        // Calculates average percentage change for whole group and whether
        // the overall group is rising or falling
        Float averagePercChange = percChangeTotal / companies.length;
        output.add(roundPercentage(averagePercChange).toString());
        if (averagePercChange > 0) {
            output.add("rose");
        } else if (averagePercChange < 0) {
            output.add("fell");
        } else {
            output.add("had no overall change");
        }

        Comparator<? super Map.Entry<String, Float>> valueComparator =
                ((entry1, entry2) ->
                entry1.getValue().compareTo(entry2.getValue()));

        Map.Entry<String, Float> maxSpotPriceCompany =
                Collections.max(spotPriceMap.entrySet(), valueComparator);
        Map.Entry<String, Float> minSpotPriceCompany =
                Collections.min(spotPriceMap.entrySet(), valueComparator);
        Map.Entry<String, Float> maxPercChangeCompany =
                Collections.max(percChangeMap.entrySet(), valueComparator);
        Map.Entry<String, Float> minPercChangeCompany =
                Collections.min(percChangeMap.entrySet(), valueComparator);

        output.add(maxSpotPriceCompany.getKey() + ", "
                + convertToGBX(maxSpotPriceCompany.getValue()));
        output.add(minSpotPriceCompany.getKey() + ", "
                + convertToGBX(minSpotPriceCompany.getValue()));
        output.add(maxPercChangeCompany.getKey() + ", "
                + roundPercentage(maxPercChangeCompany.getValue()).toString());
        output.add(minPercChangeCompany.getKey() + ", "
                + roundPercentage(minPercChangeCompany.getValue()).toString());

        return output;
    }

    /**
    * Gets trend data
    *
    * @param pr The parse result from the user's input
    * @return An array list of strings containing the percentage change, whether
    * the company has risen or fallen, the start price and the end price
    */
    private ArrayList<String> getTrendData(ParseResult pr) {

        ArrayList<Float> trendData = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();
        Intent intent = pr.getIntent();
        TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String companyCode = pr.getOperand();

        switch (intent) {
            case TREND:
                trendData = getTrendDataOnDate(companyCode, timeSpec);
                if (trendData.isEmpty()) {
                    return output;
                }
                break;
            case TREND_SINCE:
                trendData = getTrendDataSinceDate(companyCode, timeSpec);
                if (trendData.isEmpty()) {
                    return output;
                }
                break;
            default:
                break;
        }

        Float percChange = trendData.get(0);
        Float startPrice = trendData.get(1);
        Float endPrice = trendData.get(2);

        output.add(roundPercentage(percChange).toString());

        if (percChange > 0)
            output.add("rose");
        else if (percChange < 0) {
            output.add("fell");
        } else {
            output.add("had no overall change");
        }
        output.add(convertToGBX(startPrice));
        output.add(convertToGBX(endPrice));

        return output;
    }

    /**
    * Returns query to get opening price on date given of company given
    *
    * @param companyCode The company's code
    * @param date The date to get the opening price on
    * @return query to get opening price of company on date given
    */
    private String getOpeningPriceQuery(String companyCode, String date) {
        String query    = "SELECT (SpotPrice - AbsoluteChange) "
                        + "FROM FTSECompanySnapshots "
                        + "WHERE CompanyCode = '" + companyCode
                        + "' AND DATE(TimeOfData) <= '" + date
                        + "' ORDER BY TimeOfData ASC LIMIT 1";
        return query;
    }

    /**
    * Calculates the percentage change between the opening price and the
    * closing price (or spot price if time specifier is today) for a company
    * on day specified
    *
    * @param companyCode The company's code
    * @param timeSpec The time specifier to get the trend data from
    * @return An array list of floats containing the percentage change, start
    * price and end price
    */
    private ArrayList<Float> getTrendDataOnDate(String companyCode,
            TimeSpecifier timeSpec) {

        LocalDateTime currentTime = LocalDateTime.now();
        ArrayList<Float> trendData = new ArrayList<>();
        Statement s1 = null;
        Statement s2 = null;
        ResultSet startPriceRS = null;
        ResultSet endPriceRS = null;
        Float startPrice = 0.0f;
        Float endPrice = 0.0f;
        Float percChange = 0.0f;
        String date = timeSpecifierToDate(timeSpec);

        String startTimeQuery = getOpeningPriceQuery(companyCode, date);
        String endTimeQuery = spotOrClosingPriceQuery(timeSpec, companyCode,
                date);

        // If able to get start and end prices, calculate the percentage
        // change between them
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

            if (!startPrice.equals(0.0f) && !endPrice.equals(0.0f)) {
                percChange = ((endPrice - startPrice) / startPrice) * 100;
            } else {
                return trendData;
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

    /**
    * Calculates the percentage change between the opening price on the day
    * specified  and the current spot price for a company
    *
    * @param companyCode The company's code
    * @param timeSpec The time specifier
    * @return An array list of floats containing the percentage change, opening
    * price and spot price
    */
    private ArrayList<Float> getTrendDataSinceDate(String companyCode,
            TimeSpecifier timeSpec) {

        ArrayList<Float> trendData = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        Statement s1 = null;
        Statement s2 = null;
        ResultSet spotPriceRS = null;
        ResultSet openingPriceRS = null;
        Float spotPrice = 0.0f;
        Float openingPrice = 0.0f;
        Float percChange = 0.0f;

        String date = timeSpecifierToDate(timeSpec);
        String spotPriceQuery   = "SELECT SpotPrice FROM FTSECompanySnapshots "
                                + "WHERE CompanyCode = '" + companyCode + "'";
        String openingPriceQuery = getOpeningPriceQuery(companyCode, date);
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

            if (!spotPrice.equals(0.0f) && !openingPrice.equals(0.0f)) {
                percChange = ((spotPrice - openingPrice) / openingPrice) * 100;
            } else {
                System.out.println("Null start or spot price");
                return trendData;
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

    /**
    * Returns query to get spot price of company if the time specifer is today
    * or the closing price if it isn't
    *
    * @param timeSpec The time specifier
    * @param companyCode The company's code
    * @param date The time specifier converted to a date
    * @return query to get spot price of company if the time specifier is today,
    * otherwise query to get closing price of compnay on the specified day
    */
    private String spotOrClosingPriceQuery(
            TimeSpecifier timeSpec, String companyCode,
            String date) {

        String query = "";
        if (timeSpec == TimeSpecifier.TODAY) {
            query   = "SELECT SpotPrice FROM FTSECompanySnapshots "
                    + "WHERE CompanyCode = '" + companyCode + "'";
        } else {
            query   = "SELECT SpotPrice FROM FTSECompanySnapshots "
                    + "WHERE CompanyCode = '" + companyCode
                    + "' AND DATE(TimeOfData) <= '" + date
                    + "' ORDER BY TimeOfData DESC LIMIT 1";
        }
        return query;
    }

    /** 
    * Returns an SQL query to get the FTSE data required in the parse result 
    *
    * @param pr The parse result from the user's input
    * @return An SQL query to get the FTSE data required
    */
    public String convertFTSEQuery(ParseResult pr) {
        Intent intent = pr.getIntent();
        TimeSpecifier timeSpec = pr.getTimeSpecifier();
        String companyCode = pr.getOperand();
        Boolean isGroup = pr.isOperandGroup();

        String query = "";
        String timeSpecifierSQL = "";
        Boolean isFetchCurrentQuery = false;
        String colName = "";

        LocalDateTime currentTime = LocalDateTime.now();
        String date = timeSpecifierToDate(timeSpec);

        switch (intent) {
            case SPOT_PRICE:
                isFetchCurrentQuery = true;
                colName = "SpotPrice";
                break;
            case TRADING_VOLUME:
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
                query = getOpeningPriceQuery(companyCode, date);
                break;
            case CLOSING_PRICE:
                query   = "SELECT SpotPrice FROM FTSECompanySnapshots "
                        + "WHERE CompanyCode = '" + companyCode
                        + "' AND DATE(TimeOfData) <= '" + date + "' "
                        + "ORDER BY TimeOfData DESC LIMIT 1";
                break;
            default:
                break;
        }

        // get current data requested from database
        if (isFetchCurrentQuery) {
            query   = "SELECT " + colName + " FROM FTSECompanySnapshots "
                    + "WHERE CompanyCode = '" + companyCode
                    + "' ORDER BY TimeOfData DESC LIMIT 1";
        }

        return query;
    }

    /**
    * Converts time specifier to date
    *
    * @param t The time specifier
    * @return The time specifier converted to a date in the format yyyy-MM-dd
    */
    private String timeSpecifierToDate(TimeSpecifier t) {

        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd");
        String formattedDate = "";

        switch (t) {
            // If the time specifier is today and yesterday, get date of most
            // recent trading day
            case TODAY:
                formattedDate = getMostRecentTradingDay(date);
                return formattedDate;
            case YESTERDAY:
                formattedDate = getMostRecentTradingDay(date.minusDays(1));
                return formattedDate;
            case LAST_MONDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != DayOfWeek.MONDAY);
                break;
            case LAST_TUESDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != DayOfWeek.TUESDAY);
                break;
            case LAST_WEDNESDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != DayOfWeek.WEDNESDAY);
                break;
            case LAST_THURSDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != DayOfWeek.THURSDAY);
                break;
            case LAST_FRIDAY:
                do {
                    date = date.minusDays(1);
                } while (date.getDayOfWeek() != DayOfWeek.FRIDAY);
                break;
        }

        formattedDate = date.format(dateFormatter);
        return formattedDate;
    }

    /**
    * Finds date of most recent trading day if it is a non trading day
    *
    * @param currentTime The current time
    * @return If it is a trading day, the current date, otherwise the date of
    * the most recent trading day
    */
    private String getMostRecentTradingDay(LocalDateTime currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date;
        switch (currentTime.getDayOfWeek()) {
            case SATURDAY:
                date = (currentTime.minusDays(1)).format(formatter);
                break;
            case SUNDAY:
                date = (currentTime.minusDays(2)).format(formatter);
                break;
            default:
                date = currentTime.format(formatter);
                break;
        }

        return date;

    }

    /**
    * Rounds percentage to 3 decimal places
    *
    * @param f The percentage to be rounded
    * @return The rounded percentage
    */
    private Float roundPercentage(Float f) {
        return Math.round(f * 1000.0f) / 1000.0f;
    }

    /**
    * Converts a float into GBX currency format
    *
    * @param num The number to be converted
    * @return The converted number
    */
    private String convertToGBX(Float num) {
        DecimalFormat formatter = new DecimalFormat(
                "GBX #,##0.00;GBX -#,##0.00");
        return formatter.format(num);
    }

    /**
    *
    * Returns othe relevant information stored about a company except the
    * information asked for by the user
    *
    * @param pr The parse result of the user's input
    * @return An array list of strings containing the other company information
    */
    private ArrayList<String> getAllCompanyInfo(ParseResult pr) {

        Statement s1 = null;
        ResultSet results = null;
        ArrayList<String> rs = new ArrayList<>();
        String companyCode = pr.getOperand();
        Intent intent = pr.getIntent();
        ArrayList<String> columns = new ArrayList<>();

        // Get columns needed in query
        switch(intent) {
            case SPOT_PRICE:
                columns.add("PercentageChange");
                columns.add("TradingVolume");
                columns.add("AbsoluteChange");
                break;
            case TRADING_VOLUME:
                columns.add("SpotPrice");
                columns.add("PercentageChange");
                columns.add("AbsoluteChange");
                break;
            case PERCENT_CHANGE:
                columns.add("SpotPrice");
                columns.add("TradingVolume");
                columns.add("AbsoluteChange");
                break;
            case ABSOLUTE_CHANGE:
                columns.add("SpotPrice");
                columns.add("TradingVolume");
                columns.add("PercentageChange");
                break;
            case OPENING_PRICE:
            case CLOSING_PRICE:
                columns.add("SpotPrice");
                columns.add("TradingVolume");
                columns.add("PercentageChange");
                columns.add("AbsoluteChange");
                break;
            default:
                break;
        }

        // Create query
        String query = "SELECT ";
        for (int i = 0; i < columns.size(); i++) {
            query += columns.get(i);
            // Don't add comma after last column
            if (i != columns.size() -1) {
                query += ", ";
            }
        }
        query   += " FROM FTSECompanySnapshots WHERE CompanyCode = '"
                + companyCode
                + "' ORDER BY TimeOfData DESC LIMIT 1";

        // Execute and store query results
        try {
            s1 = conn.createStatement();
            results = s1.executeQuery(query);
            ResultSetMetaData rsmd = results.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String colName = rsmd.getColumnName(i);
                switch (colName) {
                    case "TradingVolume":
                        rs.add("Trading volume| " + (Integer.toString(
                                results.getInt(i))));
                        break;
                    case "SpotPrice":
                        rs.add("Spot price| " + (convertToGBX((Float)results
                                .getFloat(i))));
                        break;
                    case "PercentageChange":
                        rs.add("Percentage change| " + ((Float)results
                                .getFloat(i)).toString() + "%");
                        break;
                    case "AbsoluteChange":
                        rs.add("Absolute change| " + (convertToGBX((Float)results
                                .getFloat(i))));
                        break;
                    default:
                        break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            tryClose(s1, results);
        }

        return rs;
    }

    /**
    * Gets the table name where the count of queries regarding an intent is
    * stored
    *
    * @param i The intent
    * @return The name of the table
    */
    private String intentToTableName(Intent i){
        String name = null;
        switch (i) {
            case SPOT_PRICE:
                name = "CompanySpotPriceCount";
                break;
            case TRADING_VOLUME:
                name = "CompanyTradingVolumeCount";
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
            case TREND_SINCE:
                name = "CompanyTrendSinceCount";
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

    /**
    * 
    * 
    * @return
    */
    public ArrayList<Company> getAICompanies() {

      ArrayList<Company> companies = new ArrayList<>();
      // Get Counts for each intent
      String query = ""
        + "SELECT ftc.CompanyCode,coalesce(NewsCount,0),coalesce(SpotPriceCount,0),coalesce(OpeningPriceCount,0),coalesce(AbsoluteChangeCount,0),coalesce(ClosingPriceCount,0),coalesce(percentageChangeCount,0),coalesce(TrendCount,0),coalesce(TradingVolumeCount,0),coalesce(newsAdjustment,0),coalesce(SpotPriceAdjustment,0),coalesce(OpeningPriceAdjustment,0),coalesce(AbsoluteChangeAdjustment,0),coalesce(ClosingPriceAdjustment,0),coalesce(percentageChangeAdjustment,0),coalesce(TrendAdjustment,0),coalesce(TradingVolumeAdjustment,0) "
        + "FROM FTSECompanies ftc "
        + "LEFT OUTER JOIN CompanyNewsCount cnc ON (cnc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanySpotPriceCount csc ON (csc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyOpeningPriceCount coc ON (coc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyAbsoluteChangeCount cac ON (cac.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyClosingPriceCount ccc ON (ccc.CompanyCode = ftc.CompanyCode) "
        + "LEFT OUTER JOIN CompanyPercentageChangeCount cpc ON (cpc.CompanyCode = ftc.CompanyCode)"
        + "LEFT OUTER JOIN CompanyTrendCount ctc ON (ctc.CompanyCode = ftc.CompanyCode)"
        + "LEFT OUTER JOIN CompanyTradingVolumeCount ctvc ON (ctvc.CompanyCode = ftc.CompanyCode)";

      Statement stmt = null;
      ResultSet rs = null;

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        while (rs.next()) {
          // Create list of intents for each company
          // ArrayList<IntentData> intents = new ArrayList<>();
          // News counter
          float newsCount = (float) rs.getInt("coalesce(NewsCount,0)");
          // Intents
          float spot = (float) rs.getInt("coalesce(SpotPriceCount,0)");
          float opening = (float) rs.getInt("coalesce(OpeningPriceCount,0)");
          float absoluteChange = (float) rs.getInt("coalesce(AbsoluteChangeCount,0)");
          float closing = (float) rs.getInt("coalesce(ClosingPriceCount,0)");
          float percentageChange = (float) rs.getInt("coalesce(percentageChangeCount,0)");
          float trend = (float) rs.getInt("coalesce(TrendCount,0)");
          float volume = (float) rs.getInt("coalesce(TradingVolumeCount,0)");


          // Now the  adjustments
          // for news
          float newsAdj =  rs.getFloat("coalesce(newsAdjustment,0)");
          // and for intents
          float spotAdj =  rs.getFloat("coalesce(SpotPriceAdjustment,0)");
          float openingAdj =  rs.getFloat("coalesce(OpeningPriceAdjustment,0)");
          float absoluteChangeAdj =  rs.getFloat("coalesce(AbsoluteChangeAdjustment,0)");
          float closingPriceAdj =  rs.getFloat("coalesce(ClosingPriceAdjustment,0)");
          float percentageChangeAdj =  rs.getFloat("coalesce(percentageChangeAdjustment,0)");
          float trendAdj =  rs.getFloat("coalesce(TrendAdjustment,0)");
          float volumeAdj =  rs.getFloat("coalesce(TradingVolumeAdjustment,0)");

          // intent priorities
          float spotPriority = spot - spotAdj;
          float openingPriority = opening - openingAdj;
          float closingPriority = closing - closingPriceAdj;
          float absoluteChangePriority = absoluteChange - absoluteChangeAdj;
          float percentageChangePriority = percentageChange - percentageChangeAdj;
          float trendPriority = trend - trendAdj;
          float volumePriority = volume - volumeAdj;

          // news
          float newsPriority = newsCount - newsAdj;

          HashMap<AIIntent, Float[]> mapping = new HashMap<>();

          mapping.put(AIIntent.SPOT_PRICE, new Float[]{spot, spotAdj});
          mapping.put(AIIntent.OPENING_PRICE, new Float[]{opening, openingAdj});
          mapping.put(AIIntent.CLOSING_PRICE, new Float[]{closing, closingPriceAdj});
          mapping.put(AIIntent.PERCENT_CHANGE, new Float[]{percentageChange,percentageChangeAdj });
          mapping.put(AIIntent.ABSOLUTE_CHANGE, new Float[]{absoluteChange, absoluteChangeAdj});
          mapping.put(AIIntent.TREND, new Float[]{trend, trendAdj});
          mapping.put(AIIntent.TRADING_VOLUME, new Float[]{volume, volumeAdj});

          // Calculate priority for each company
          Float intentScale = 1.0f;
          Float newsScale = 1.0f;
          float priority = intentScale * (spotPriority + openingPriority + closingPriority + absoluteChangePriority + percentageChangePriority + trendPriority + volumePriority) + newsScale * (newsPriority);
          // average of all intent's irrelevantSuggestionWeight

          companies.add(new Company(rs.getString("CompanyCode"), mapping, intentScale, newsScale, newsCount, newsAdj));
        }

        if(companies.size() != 0) {
          return companies;
        } else {
          System.out.println("No companies found, getAICompanies returning null");
          return null;
        }

      } catch (SQLException e) {
        e.printStackTrace();
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


        for (Map.Entry<String,Group> g: entrySet) {
            ArrayList<Company> list = new ArrayList<>();
            String[] companylist = getCompaniesInGroup(g.getValue().getGroupCode());
            for (int i = 0; i < companylist.length; i++) {
                for (int j = 0; j < companies.size(); j++) {
                    Company current = companies.get(j);
                    if (current.getCode().equals(companylist[i])) {
                        list.add(current);
                        break;
                    }
                }
            }
            g.getValue().addCompanies(list);
            // TODO add group adjustment
        }

        // now add the remaining values to each group
        for(Group g: result) {
          ArrayList<Company> com = g.getCompanies();
          int numberOfCompanies = com.size();

          Float groupCount = 0.0f;
          // Calculate group count
          for(Company c: com) {
            groupCount+= c.getIntentsCount() + c.getNewsCount();
          }

          g.setGroupCount(groupCount);
        }

      } catch (SQLException ex) {
        ex.printStackTrace();
      } finally {
        if (stmt != null) { tryClose(stmt); }
        if (rs != null) { tryClose(rs); }
      }

      return result;
    }

    /**
    *
    *
    * @param threshold
    * @return
    */
    //TODO
    public ArrayList<String> detectedImportantChange(Float treshhold) {
      String query =  "SELECT PercentageChange, CompanyCode FROM FTSECompanySnapshots ORDER BY TimeOfData DESC LIMIT 101";

      ResultSet rs = null;
      Statement stmt = null;
      ArrayList<String> result = new ArrayList<>();

      HashMap<String, Float> companiesPercChangeMap = new HashMap<>();
      // TODO

      try {
        stmt = conn.createStatement();
        rs = stmt.executeQuery(query);

        while(rs.next()) {
          String companyName = rs.getString("CompanyCode");

          Float percChange = rs.getFloat("PercentageChange");

          if(Math.abs(percChange) > Math.abs(treshhold)) {
            companiesPercChangeMap.put(companyName, percChange);
          }
        }

        Set<String> winningNames = companiesPercChangeMap.keySet();

        for(String s: winningNames) {
          result.add(s);
        }

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (stmt != null) { tryClose(stmt); }
        if(rs != null) {tryClose(rs); }
      }

      if(result.size() == 0){
            System.out.println("No companies have percentage change exceeding the threshold");
           return null;
       }
      return result;
    }

    /**
    *
    *
    * @param company
    * @param intent
    * @param isNews
    * @return 
    */
    //TODO
    public void onSuggestionIrrelevant(Company company, AIIntent intent, boolean isNews) {
      String table = "";
      if(intent == null) {
        System.out.println("Intent was null");
        return;
      }
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
          case TREND: table+= "CompanyTrendCount";
          break;
          case TRADING_VOLUME: table+= "CompanyTradingVolumeCount";
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
        e.printStackTrace();
      } finally {
        if (stmt != null) { tryClose(stmt); }
      }
    }

    // DO WE NEED THIS???
    public void storeAIGroups(ArrayList<Group> groups) {
        
    }

    public void storeAICompanies(ArrayList<Company> companies) {
        
    }

    /**
    *
    *
    * @param groupName
    * @return
    */
    public String[] getCompaniesInGroup(String groupName){
        groupName.toLowerCase();
        ArrayList<String> companies = new ArrayList<>();
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
          if (r1 != null) { tryClose(r1); }
        }
        return companies.toArray(new String[1]);
    }

    /**
    * Closes statement
    *
    * @param s The statement
    */
    private void tryClose(Statement s) {
        try {
            s.close();
        } catch(Exception e) {
            // Do nothing
        }
    }

    /**
    * Closes result set
    *
    * @param s The result set
    */
    private void tryClose(ResultSet s) {
        try {
            s.close();
        } catch(Exception e) {
            // Do nothing
        }
    }

    /**
    * Closes a statement and result set
    *
    * @param s The statement
    * @param rs The results set
    */
    private void tryClose(Statement s, ResultSet rs) {
        tryClose(s);
        tryClose(rs);
    }

    /**
    * Sets the auto commit field
    *
    * @param b The boolean to see the autocommit field to
    */
    private void trySetAutoCommit(Boolean b) {
        try{
            conn.setAutoCommit(b);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Rolls back the database
    */
    private void tryRollback() {
        try{
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
    * Commits the database
    */
    private void tryCommit() {
        try {
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
