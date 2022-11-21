#!/bin/bash

# Run Polystat (EO) and save reports to "results" folder
curl -L -o polystat.jar "https://github.com/polystat/polystat-cli/releases/download/v0.1.11/polystat.jar"
echo "Polystat (EO) analysis has started"
touch polystat-eo-out.txt
java -jar polystat.jar eo --in out --to file=polystat-eo-out.txt --sarif
echo "Polystat (EO) analysis has finished"o polystat.jar "https://github.com/polystat/polystat-cli/releases/download/v0.1.11/polystat.jar"