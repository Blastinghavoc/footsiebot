javac -encoding UTF8 -cp "./src/datagathering/jsoup.jar";"./src/voce-0.9.1/lib/voce.jar" -d ./classes ./src/*.java ./src/nlp/*.java ./src/database/*.java ./src/datagathering/*.java ./src/gui/*.java ./src/ai/*.java

java -cp "./classes";"./src/database/sqlite-jdbc-3.21.0.jar";"./src/datagathering/jsoup.jar";"./src/voce-0.9.1/lib/voce.jar" -mx256m footsiebot.Core