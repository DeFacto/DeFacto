export MAVEN_OPTS="-mx2G"
mvn -o compile
mvn -o exec:java -Dexec.mainClass="org.aksw.defacto.webservices.server.DefactoServer"
