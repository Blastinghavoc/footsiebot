#!/bin/sh

javac -d ./classes ./src/*.java ./src/nlpcore/*.java ./src/databasecore/*.java ./src/datagatheringcore/*.java ./src/guicore/*.java ./src/intelligencecore/*.java
java -cp "./classes":"./src/databasecore/sqlite-jdbc-3.21.0.jar" footsiebot.Core
