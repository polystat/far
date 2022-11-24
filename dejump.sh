#!/bin/bash

# Download Dejump jar file
curl -L -o dejump-0.0.2-jar-with-dependencies.jar "https://repo1.maven.org/maven2/org/eolang/dejump/0.0.2/dejump-0.0.2-jar-with-dependencies.jar"
echo "Dejump jar was downloaded"
java -jar dejump-0.0.2-jar-with-dependencies.jar --eo out/global.eo
echo "GOTO was removed, check the result: "
cat out/generated/global_transformed.eo
echo "Rename file"
mv out/generated/global_transformed.eo out/generated/global.eo