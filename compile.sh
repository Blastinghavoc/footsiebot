#!/bin/sh

javac -cp "./src/datagathering/jsoup.jar":"./src/voce-0.9.1/lib/voce.jar" -d ./classes ./src/*.java ./src/nlp/*.java ./src/database/*.java ./src/datagathering/*.java ./src/gui/*.java ./src/ai/*.java
