#!/bin/sh
./stop.sh
export MAVEN_OPTS=-Xmx4G
nohup mvn exec:java -Dexec.mainClass="org.aksw.defacto.restful.webservice.App" > run.log &
 
