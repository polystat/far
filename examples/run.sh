#!/bin/bash

set -euox pipefail
file=$1

echo "Pull c2eo Docker image"
mkdir out
docker pull yegor256/c2eo:0.1.24
echo "Finished pulling"

echo "Run c2eo Docker image"
docker run -v $(pwd):/eo yegor256/c2eo:0.1.24 $file out/global.eo
echo "Finished running"

echo "Check out/global.eo"
ls -la out
cat out/global.eo
echo "Finished cheking"

echo "Copy c2o.cooperators"
mkdir out/c2eo
git clone "https://github.com/polystat/c2eo.git" --branch master
cp -r c2eo/project/eo-lib out/c2eo
echo "Finished copy"

echo "Removing goto"
# Download Dejump jar file
curl -L -o dejump-0.0.2-jar-with-dependencies.jar "https://repo1.maven.org/maven2/org/eolang/dejump/0.0.2/dejump-0.0.2-jar-with-dependencies.jar"
echo "Dejump jar was downloaded"
java -jar dejump-0.0.2-jar-with-dependencies.jar --eo out/global.eo
echo "GOTO was removed, check the result: "
cat out/generated/global_transformed.eo
echo "Rename file"
mv out/generated/global_transformed.eo out/generated/global.eo
echo "Finished Removing"

echo "Run Polystat Jar"
# Run Polystat (EO) and save reports to "results" folder
curl -L -o polystat.jar "https://github.com/polystat/polystat-cli/releases/download/v0.1.11/polystat.jar"
echo "Polystat (EO) analysis has started"
touch polystat-eo-out.txt
java -jar polystat.jar eo --in examples/org/eolang --to file=polystat-eo-out.txt --sarif
#java -jar polystat.jar eo --in out/generated --to file=polystat-eo-out.txt --sarif
echo "Polystat (EO) analysis has finished"o polystat.jar "https://github.com/polystat/polystat-cli/releases/download/v0.1.11/polystat.jar"
echo "Finished running"

echo "Check polystat-eo-out.txt"
cat polystat-eo-out.txt
