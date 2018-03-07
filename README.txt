==== CS261-Project-Team-29 ====

This is team 29's submission for the CS261 software engineering project 2018.

The majority of the code is original, but this project does make use of some free packages:
Voce : http://voce.sourceforge.net/ For simple voice input
Jsoup : https://jsoup.org/ For web scraping
Sqlite JDBC : https://bitbucket.org/xerial/sqlite-jdbc For accessing an SQLITE database

==== System Requirements ====
JDK 8 or above

==== Installation and Running the Footsiebot ====
Extract the zip into a folder, navigate to that folder and:
 - if you're running Windows, execute run.bat
 - if you're running MacOS/Linux, execute run.sh

==== Basics ====
Commands are entered via the textbox at the bottom, and sent either by pressing the enter key,
or pressing the send button.

AI suggestions have 2 buttons next to them:
the cross button is pressed to indicate to the AI that the suggestion is irrelevant to you.
the question mark inside the circle is pressed when you want to know why the suggestion was made.

==== Commands ====
For individual companies:
 - Spot Price - e.g. "Spot price for X"
 - Opening Price - e.g. "Opening price for X"
 - Closing Price - e.g. "Closing price for X"
 - Trading Volume - e.g. "Volume for X"
 - Percentage Change (since market opened) - e.g. "Percentage change for X"
 - Absolute Change (since market opened) - e.g. "Absolute change for X"
 - Company Trend (for that day) - e.g. "Is X rising or falling?"
 - Company Trend (since a given day) - e.g. "has X risen since Thursday"
 - Recent News - e.g. "News for X"
All of these commands (except news) can be modified to include a specific day in the last 5 
trading days. For example, "Closing price for Barclays on Wednesday" is a valid command.
    
For Groups:
 - Company Trend (for that day) - e.g. "Is X rising or falling?"
 - Company Trend (since a given day) - e.g. "has X risen since Thursday"
 - Recent News - e.g. "News for X"

If you ever need reminding of these commands, just send the chatbot the message "help" and
it'll print a summary.

On startup the Footsiebot will ask what to call you, but you can change this at any time by
saying "call me X".

There is prototype support for voice chat, in order to use it all you have to do is say is 
"assistant" followed by your command.

==== Settings ====
The settings menu (accessing bu clicking on the gear in the top right hand side of the screen)
allows you to change some properties.
1) Daily summary time - the Footsiebot is configured to print a daily summary of your most
   important companies, the time for which you can adjust here in half hour intervals.
   You can also ask for your daily summary to be printed immediately.
2) Percentage change threshold - the Footsiebot will show warnings for companies where the
   absolute value of their percentage change exceeds this value.
3) Theme - allows you to change the colour of the Footsiebot buttons. Updates live.
4) Open in fullscreen - as described
Make sure you save your changes before you close, otherwise they won't be applied.
