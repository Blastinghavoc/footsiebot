package footsiebot.nlp;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;

public class NLPCore implements INaturalLanguageProcessor{
    private HashMap<String,String> operandMap;//Maps raw string to internally used string. Could map to an enum instead.
    private ArrayList<String> operandList;

    private HashMap<String,String> groupOperandMap;
    private ArrayList<String> groupOperandList;

    private HashMap<String,Intent> intentMap;//Maps raw string to intent
    private ArrayList<String> intentList;

    private HashMap<String,TimeSpecifier> timeMap;
    private ArrayList<String> timeList;

    public NLPCore(){
        initialiseOperands();
        initialiseIntents();
        initialiseTimes();
    }

    /**
    * The primary function of this class. Takes an input String
    * and returns a ParseResult. If the input cannot be parsed completely,
    * the ParseResult has null values for the fields that could not be determined.
    * @param s The input string.
    * @return A ParseResult. Never null, but may contain null fields
    */
    public ParseResult parse(String s){
        Intent in = Intent.SPOT_PRICE;//DEBUG
        String raw = s;
        String operand = null;
        Boolean operandIsGroup = false;
        TimeSpecifier ts = TimeSpecifier.TODAY;//DEBUG

        s = s.toLowerCase();
    	  //STRIP INTENT OUT OF STRING USING STRING.REPLACE AND STRING.CONTAINS
        Boolean valid = false;
        int longestMatchingTokenLength = 0;
        String finalIntent = null;
        for ( String i: intentList) {
          if(s.contains(i)){
            if(i.length() > longestMatchingTokenLength){
                in = intentMap.get(i);
                finalIntent = i;
                valid = true;
                longestMatchingTokenLength = i.length();
            }
          }
        }

        if(!valid){
          in = null;
        }
        else{
            s = s.replace(finalIntent,"");//Removing the detected intent from the string.
        }

        //System.out.println("s is now '"+s+"'");

        //FIND TIME SPECIFIER FROM REMAINING.
        valid = false;
        for ( String i: timeList) {
          if(s.contains(i)){
            ts = timeMap.get(i);
            //System.out.println("Found TimeSpecifier '"+ ts + "' as string '" + i + "' in s '" + s + "'");//DEBUG
            s = s.replace(i,"");
            valid = true;
            break;
          }
        }
        //Acceptable for there to be no time specifier. Defaults to TODAY

    	//TOKENIZE REMAINING STRING
        s = s.replace("   "," ");//removing any triple spaces that may arrise from previous deletions
        s = s.replace("  "," ");//removing any double spaces.
        //System.out.println(s);//DEBUG

        String[] tokens = s.split(" ");

        for (int i = 0; i<tokens.length ; i++ ) {
            tokens[i] = stripPunctuation(tokens[i]);
        }

    	//FIND OPERAND


        operand = autocompleteHeuristic(tokens,operandList,operandMap);

        if (operand != null){//we have an operand
            operandIsGroup = false;
            valid = true;
        }
        else
        {
            //try again with the groups

            operand = searchForFullGroup(s);//First try to find the full group name
            if(operand != null){
                operandIsGroup = true;
                valid = true;
            }
            else
            {
                operand = autocompleteHeuristic(tokens,groupOperandList,groupOperandMap);
            }
            if (operand != null){
                operandIsGroup = true;
                valid = true;
                if(in == Intent.TREND){
                    in = Intent.GROUP_FULL_SUMMARY;
                }
            }
        }


        if(!valid){
          operand = null;
        }



        return new ParseResult(in,raw,operand,operandIsGroup,ts);//Note that fields could be null.
    }

    /**
    * Sets up the mapping of Strings to TimeSpecifiers, and initialises
    * a sorted list of those strings.
    */
    private void initialiseTimes(){
    timeMap = new HashMap<String,TimeSpecifier>();
    timeList = new ArrayList<String>();
    timeMap.put("today",TimeSpecifier.TODAY);
    timeMap.put("yesterday",TimeSpecifier.YESTERDAY);
    timeMap.put("monday",TimeSpecifier.LAST_MONDAY);
    timeMap.put("tuesday",TimeSpecifier.LAST_TUESDAY);
    timeMap.put("wednesday",TimeSpecifier.LAST_WEDNESDAY);
    timeMap.put("thursday",TimeSpecifier.LAST_THURSDAY);
    timeMap.put("friday",TimeSpecifier.LAST_FRIDAY);

    for (String s :timeMap.keySet()) {
      timeList.add(s);
    }
    timeList.sort(String.CASE_INSENSITIVE_ORDER);


  }

  /*
  * Sets up the mapping of Strings to Intents, and initialises
  * a sorted list of those strings.
  */
  private void initialiseIntents(){
    intentMap = new HashMap<String,Intent>();
    intentList = new ArrayList<String>();
    //Adding intents and their synonyms manually, as they should remain fixed
    intentMap.put("spot",Intent.SPOT_PRICE);
    intentMap.put("current",Intent.SPOT_PRICE);
    intentMap.put("price",Intent.SPOT_PRICE);

    intentMap.put("trading volume",Intent.TRADING_VOLUME);
    intentMap.put("volume",Intent.TRADING_VOLUME);

    intentMap.put("opening",Intent.OPENING_PRICE);

    intentMap.put("closing",Intent.CLOSING_PRICE);

    intentMap.put("percentage change",Intent.PERCENT_CHANGE);
    intentMap.put("percent change",Intent.PERCENT_CHANGE);
    intentMap.put("% change",Intent.PERCENT_CHANGE);

    intentMap.put("absolute change",Intent.ABSOLUTE_CHANGE);
    intentMap.put("abs change",Intent.ABSOLUTE_CHANGE);

    intentMap.put("trend since",Intent.TREND_SINCE);
    intentMap.put("risen since",Intent.TREND_SINCE);
    intentMap.put("fallen since",Intent.TREND_SINCE);

    intentMap.put("rising",Intent.TREND);
    intentMap.put("falling",Intent.TREND);
    intentMap.put("risen",Intent.TREND);
    intentMap.put("rise",Intent.TREND);
    intentMap.put("fall",Intent.TREND);
    intentMap.put("fallen",Intent.TREND);
    intentMap.put("doing",Intent.TREND);
    intentMap.put("trend",Intent.TREND);

    intentMap.put("news",Intent.NEWS);


    for (String s :intentMap.keySet()) {
      intentList.add(s);
    }


    intentList.sort(String.CASE_INSENSITIVE_ORDER);

  }

  /*
  * Sets up the mapping of Strings to recognized operands, and initialises
  * a sorted list of those strings. Also deals with synonyms.
  */
  private void initialiseOperands(){
    //An operand is a company
    operandMap = new HashMap<String,String>();
    operandList = new ArrayList<String>();
    //or a group
    groupOperandMap = new HashMap<String,String>();
    groupOperandList = new ArrayList<String>();

    //normal operands
    try{
      File fl = new File("src/nlp/operands.txt");
      BufferedReader br = new BufferedReader(new FileReader(fl.getAbsolutePath().replace("\\", "/")));
      String line;
      String[] splitLine;
      while((line = br.readLine()) != null){
        splitLine = line.toLowerCase().split(",");
        String operand = splitLine[0];

        operand = stripPunctuation(operand);

        operandMap.put(operand,operand);//Adds the operand to the map, mapping to itself
        operandList.add(operand);
        //System.out.println("Added '" + operand +"' to the map");//DEBUG

        if(operand.startsWith("the ")){//additional synonyms for things starting with "the"
          String newSyn = operand.replaceFirst("the ","");
          operandMap.put(newSyn,operand);
          operandList.add(newSyn);
        }

        if(operand.contains(" & ")){//additional synonyms for things containing &
            String newSyn = operand.replace(" & "," and ");
            operandMap.put(newSyn,operand);
            operandList.add(newSyn);
        }

        if(splitLine.length < 2){//There are no synonyms
          continue;//read next line
        }
        else{//There is at least 1 synonym
          for (int i = 1; i< splitLine.length;i++ ) {//Adds all synonyms to map, mapped to the primary operand
            String currentOp = splitLine[i];

            currentOp = stripPunctuation(currentOp);

            operandMap.put(currentOp,operand);
            operandList.add(currentOp);
            //System.out.println("Added '"+ splitLine[i] + "' -> '" + operand +"' to the map");//DEBUG
            if(currentOp.startsWith("the ")){//additional synonyms for things starting with "the"
              String newSyn = currentOp.replaceFirst("the ","");
              operandMap.put(newSyn,operand);
              operandList.add(newSyn);
            }
          }
        }
      }

    }catch(Exception e){
      e.printStackTrace();
      System.err.println("Error loading operands");
    }
    operandList.sort(String.CASE_INSENSITIVE_ORDER);

    //group operands
    try{
      File fl = new File("src/nlp/groupOperands.txt");
      BufferedReader br = new BufferedReader(new FileReader(fl.getAbsolutePath().replace("\\", "/")));
      String line;
      String[] splitLine;
      while((line = br.readLine()) != null){
        splitLine = line.toLowerCase().split(",");
        String operand = splitLine[0];

        operand = stripPunctuation(operand);

        groupOperandMap.put(operand,operand);//Adds the operand to the map, mapping to itself
        groupOperandList.add(operand);

        if(operand.startsWith("the ")){//additional synonyms for things starting with "the"
          String newSyn = operand.replaceFirst("the ","");
          groupOperandMap.put(newSyn,operand);
          groupOperandList.add(newSyn);
        }

        if(operand.contains(" & ")){//additional synonyms for things containing &
            String newSyn = operand.replace(" & "," and ");
            groupOperandMap.put(newSyn,operand);
            groupOperandList.add(newSyn);
        }

        if(splitLine.length < 2){//There are no synonyms
          continue;//read next line
        }
        else{//There is at least 1 synonym
          for (int i = 1; i< splitLine.length;i++ ) {//Adds all synonyms to map, mapped to the primary operand
            String currentOp = splitLine[i];

            currentOp = stripPunctuation(currentOp);

            groupOperandMap.put(currentOp,operand);
            groupOperandList.add(currentOp);
            //System.out.println("Added '"+ splitLine[i] + "' -> '" + operand +"' to the map");//DEBUG
            if(currentOp.startsWith("the ")){//additional synonyms for things starting with "the"
              String newSyn = currentOp.replaceFirst("the ","");
              groupOperandMap.put(newSyn,operand);
              groupOperandList.add(newSyn);
            }
          }
        }
      }

    }catch(Exception e){
      e.printStackTrace();
      System.err.println("Error loading group operands");
    }
    groupOperandList.sort(String.CASE_INSENSITIVE_ORDER);

  }

  /**
  * Searches the input string to see if it contains the full name of any group.
  * @param s The input string.
  * @return The "Official" string representation of the group name.
  */
  private String searchForFullGroup(String s){
    //System.out.println(s);
    for(String candidate: groupOperandList){
        //System.out.println(candidate);
        if(s.contains(candidate)){
            //System.out.println("Candidate was "+candidate);
            return groupOperandMap.get(candidate);
        }
    }
    return null;
  }

    /**
    * The following is an autocomplete based "Heuristic"
    * Based on following reference
    * REF: https://docs.oracle.com/javase/tutorial/uiswing/components/textarea.html
    * @param tokens An array of strings representing tokens from the input.
    * @param list A list of Strings to autocomplete to.
    * @param map A mapping from each of the strings in list to an "Official" string
    * representation of that word/operand.
    * @return One of the Strings from the range of map, matching one or more consecutive
    * tokens. Null if no substring of tokens matches.
    */
    private String autocompleteHeuristic(String[] tokens,ArrayList<String> list, HashMap<String,String> map){
        String operand = null;
        int longestMatchingTokenLength = 0;
        for (int i = 0; i<tokens.length ; i++ ) {
            String token = tokens[i];
            if (token == null){
              continue;
            }

            //Check for 'the', as this word is too general to autocomplete from. Could extend to check a list of common words
            if(token.equals("the")){
                if((i+1)<tokens.length){
                    token = token + " "+ tokens[i+1];//Choose a longer token if possible.
                }
                else{
                    //If a longer token isn't possible, it means our string ends with 'the', which is ridiculous.
                    continue;
                }
            }

            Boolean keepTryingToExtendToken = false;
            int extension = 1;

            do{
                keepTryingToExtendToken = false;
                int index = Collections.binarySearch(list,token);
                if(index >= 0){//An exact match was found
                  operand = map.get(token);
                  break;//Nothing beats an exact match
                }
                else if(token.length()<=2){//Far too small to autocomplete from
                  break;
                }
                else if(-index <= list.size()){//The index where the word would be in the sorted list
                  int nextBestPosition = -index -1;//Finding the string that's lexicographically closest to the token
                  String candidate = list.get(nextBestPosition);

                  if(candidate.startsWith(token)){//If the token is a substring, then it's likely that the user just didn't type the full thing
                        keepTryingToExtendToken = true;
                        if(longestMatchingTokenLength<token.length()){//Replace previous autocompletion if we find one for a longer token, as this is more likely to be correct?
                          operand = map.get(candidate);//Autocompleting the token
                          longestMatchingTokenLength = token.length();
                        }
                  }
                }
                else{
                    //If the index magnitude is greater than the list size,the closest match would be something off the end of the list, so we can't have that
                    break;
                }

                if(keepTryingToExtendToken && !(i+extension<tokens.length)){
                    keepTryingToExtendToken = false;
                }
                if(keepTryingToExtendToken){
                    token = token + " " + tokens[i+extension];
                    extension++;
                }
            }while(keepTryingToExtendToken);
        }
        return operand;
    }

    /**
    * Removes punctuation from the end of a string.
    * @param inp A string input
    * @return The prefix of the input string that does not end in a punctuation
    * mark.
    */
    private String stripPunctuation(String inp){
        if(inp.endsWith(".")||inp.endsWith("?")||inp.endsWith("!")||inp.endsWith(",")){//Remove punctuation if present
            inp = inp.substring(0, inp.length() - 1);
        }
        return inp;
    }

}
