#! /bin/bash

ant compile
ant jar

mkdir -p ${PREFIX}/lib/varna
cp build/jar/VARNA*.jar ${PREFIX}/lib/varna/
cp misc/varna.sh ${PREFIX}/bin/varna
