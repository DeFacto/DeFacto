package org.aksw.helper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.apache.log4j.Logger;

import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/6/12
 * Time: 9:48 PM
 * Class KeywordExtractor extracts a list of keywords required for search
 */
public class KeywordExtractor {

    private static Logger logger = null;
    static {
        logger = Logger.getLogger(KeywordExtractor.class.getName());
    }

    public static String[] getStatementKeywordsForSearch(String searchTriple) {

        /*String[] predicateParts = searchPredicate.trim().split("\\s+");

        for(int i=0; i< predicateParts.length; i++){
            System.out.println(predicateParts[i]);
        }*/

        StringReader rdr = new StringReader(searchTriple);

        Model testModel = ModelFactory.createDefaultModel();
        testModel.read(rdr, "", "N-TRIPLE");

//        rdr.close();
//
//        rdr = new StringReader("<http://dbpedia.org/resource/Germany>\t<http://dbpedia.org/property/capital>\t<http://dbpedia.org/resource/Berlin> .");
//        testModel.read(rdr, "", "N-TRIPLE");

        String subject="", predicate="", object="";

        StmtIterator iter = testModel.listStatements();

        while(iter.hasNext()){
            Statement currentStatement = iter.next();
            logger.info("Subject = " + currentStatement.getSubject());
            logger.info("Predicate = " + currentStatement.getPredicate());
            logger.info("Object = " + currentStatement.getObject());

            subject = currentStatement.getSubject().toString();
            predicate = currentStatement.getPredicate().toString();

            //For literals, we have either typed or untyped literals
            //in case of typed literals, we should ignore them, as their values may affect the quality of the results
            //in case of untyped literals, we should take their values and add them to the search query, in order to
            //make the query more specific
            if(currentStatement.getObject().isLiteral()){
                if(currentStatement.getObject().asLiteral().getDatatype() == null)
                    object = currentStatement.getObject().asLiteral().getValue().toString();
                else
                    object = "";
            }
            else if(currentStatement.getObject().isResource()){
                object = currentStatement.getObject().toString();
                String[] objectParts = object.split("/");
                object = objectParts[objectParts.length-1];
            }
        }

        //Split subject, predicate and object, and get only last part, which represents the required information
        String[] subjectParts = subject.split("/");
        subject = subjectParts[subjectParts.length-1];
        subject = splitCamelCase(subject);


        String[] predicateParts = predicate.split("/");
        predicate = predicateParts[predicateParts.length-1];
        predicate = splitCamelCase(predicate);

        return new String[]{subject, predicate, object};
    }

    /**
     * Splits a string written in camel/pascal case into its parts, which simplifies the process of looking for information
     * using the search engine
     * @param  requiredString   The string the should be splitted
     * @return  splitted string
     */
    static String splitCamelCase(String requiredString) {
        String outputString = requiredString.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );

        outputString = outputString.replaceAll("_"," ");
        return outputString;
    }

    /**
     * Splits the specified triple into its parts
     * @param searchTriple  The required triples
     * @return  An array containing {Subject, Predicate, Object} respectively
     */
    public static String[] getTripleParts(String searchTriple){
        try{
            StringReader rdr = new StringReader(searchTriple);

            Model tripleModel = ModelFactory.createDefaultModel();
            tripleModel.read(rdr, "", "N-TRIPLE");

            String subject = "", predicate = "", object = "";

            StmtIterator iterStatements = tripleModel.listStatements();
            if(iterStatements.hasNext()){

                Statement currentStatement = iterStatements.next();

                subject = currentStatement.getSubject().toString();
                predicate = currentStatement.getPredicate().toString();

                //For literals, we have either typed or untyped literals
                //in case of typed literals, we should ignore them, as their values may affect the quality of the results
                //in case of untyped literals, we should take their values and add them to the search query, in order to
                //make the query more specific
                if(currentStatement.getObject().isLiteral()){
                    if(currentStatement.getObject().asLiteral().getDatatype() == null)
                        object = currentStatement.getObject().asLiteral().getValue().toString();
                    else
                        object = "";
                }
                else if(currentStatement.getObject().isResource())
                    object = currentStatement.getObject().toString();
            }

            return new String[]{subject, predicate, object};

        }
        catch (Exception exp){
            logger.error("Unable to split a triple into its parts due to " + exp.getMessage());
            return new String[]{};
        }
    }

    /**
     * Returns the subject out of the passed triple
     * @param searchTriple  The required triples
     * @return  The subject of the triple
     */
    public static String getTripleSubject(String searchTriple){
        try{
            StringReader rdr = new StringReader(searchTriple);

            Model tripleModel = ModelFactory.createDefaultModel();
            tripleModel.read(rdr, "", "N-TRIPLE");

            String subject = "";

            StmtIterator iterStatements = tripleModel.listStatements();
            if(iterStatements.hasNext()){

                Statement currentStatement = iterStatements.next();

                subject = currentStatement.getSubject().toString();
            }

            return subject;

        }
        catch (Exception exp){
            logger.error("Unable to get the subject from the triple into its parts due to " + exp.getMessage());
            return "";
        }
    }

    /**
     * Returns the predicate out of the passed triple
     * @param searchTriple  The required triples
     * @return  The predicate of the triple
     */
    public static String getTriplePredicate(String searchTriple){
        try{
            StringReader rdr = new StringReader(searchTriple);

            Model tripleModel = ModelFactory.createDefaultModel();
            tripleModel.read(rdr, "", "N-TRIPLE");

            String predicate = "";

            StmtIterator iterStatements = tripleModel.listStatements();
            if(iterStatements.hasNext()){

                Statement currentStatement = iterStatements.next();

                predicate = currentStatement.getPredicate().toString();

            }

            return predicate;

        }
        catch (Exception exp){
            logger.error("Unable to get the predicate from the triple into its parts due to " + exp.getMessage());
            return "";
        }
    }

    /**
     * Returns the object out of the passed triple
     * @param searchTriple  The required triples
     * @return  The object of the triple
     */
    public static TripleObject getTripleObject(String searchTriple){
        try{
            StringReader rdr = new StringReader(searchTriple);

            Model tripleModel = ModelFactory.createDefaultModel();
            tripleModel.read(rdr, "", "N-TRIPLE");

            TripleObject object = null;

            String objectString = "";

            StmtIterator iterStatements = tripleModel.listStatements();
            if(iterStatements.hasNext()){

                Statement currentStatement = iterStatements.next();


                //For literals, we have either typed or untyped literals
                //in case of typed literals, we should ignore them, as their values may affect the quality of the results
                //in case of untyped literals, we should take their values and add them to the search query, in order to
                //make the query more specific
                if(currentStatement.getObject().isLiteral()){
                    if(currentStatement.getObject().asLiteral().getDatatype() == null)
                        objectString = currentStatement.getObject().asLiteral().getValue().toString();
                    else
                        objectString = "";

                    object = new TripleObject(objectString, false);
                }
                else if(currentStatement.getObject().isResource()){
                    objectString = currentStatement.getObject().toString();
                    object = new TripleObject(objectString, true);
                }
            }

            return object;

        }
        catch (Exception exp){
            logger.error("Unable to get the object from the triple into its parts due to " + exp.getMessage());
            return null;
        }
    }
}
