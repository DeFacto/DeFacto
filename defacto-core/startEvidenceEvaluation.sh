export MAVEN_OPTS="-Xmx12000m"
mvn -o compile
mvn exec:java -Dexec.mainClass="org.aksw.defacto.evaluation.DefactoEvaluation" -Dexec:args="train"