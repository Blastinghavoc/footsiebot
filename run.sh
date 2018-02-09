#!/bin/sh

<<<<<<< HEAD
javac -d ./classes -cp ./sqlite-jdbc-3.21.0.jar ./src/*.java ./src/nlpcore/*.java ./src/databasecore/*.java ./src/datagatheringcore/*.java ./src/guicore/*.java ./src/intelligencecore/*.java
java -cp ./classes footsiebot.Core
=======
java -cp "../":"./databasecore/sqlite-jdbc-3.21.0.jar" footsiebot.Core
>>>>>>> c1c7b9b4e008d27ebc2e2c5f8fd3aa4aa78f6211
