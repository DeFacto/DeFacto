#!/bin/sh
./stop.sh
export MAVEN_OPTS=-Xmx4G
nohup mvn compile exec:java -Dexec.mainClass="org.aksw.defacto.restful.App" > run.log &
 
