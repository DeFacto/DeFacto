#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export MAVEN_OPTS=-Xmx10G
nohup mvn clean compile > build.log &
 
