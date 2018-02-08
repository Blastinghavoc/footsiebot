#!/bin/sh

javac -d ./classes -cp ./sqlite-jdbc-3.21.0.jar ./src/*.java ./src/nlpcore/*.java ./src/databasecore/*.java ./src/datagatheringcore/*.java ./src/guicore/*.java ./src/intelligencecore/*.java
