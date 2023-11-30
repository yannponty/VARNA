#! /bin/bash

ant -f nbbuild.xml jar

mkdir -p ${PREFIX}/lib/varna
cp dist/VARNA*.jar ${PREFIX}/lib/varna/
cp misc/varna.sh ${PREFIX}/bin/varna
