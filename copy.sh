#!/bin/bash

mkdir out/c2eo
git clone "https://github.com/polystat/c2eo.git" --branch master
ls -la
ls -la c2eo
cp -r c2eo/project/eo-lib out/c2eo
