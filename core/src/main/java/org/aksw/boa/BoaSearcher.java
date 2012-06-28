package org.aksw.boa;

import org.aksw.helper.DBpediaKnowledgeBaseQueryExecutor;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 2/9/12
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class BoaSearcher {

    private static Logger logger = Logger.getLogger(BoaSearcher.class.getName());

    /**
     * Searches the BOA index for a specific predicate and returns only its NL representations whose confidence exceed
     * the passed confidence threshold value
     * @param searchPredicate   The required predicate for which the NL representations should be returned.
     * @param confidenceThreshold   The confidence threshold only exceeding which the NL representations are returned.
     * @param maximumNumberOfResults    The maximum number of BOA results to be returned
     * @return  A list of the results returned from BOA with their corresponding confidence value
     */
    public static ArrayList<BoaSearchResult> searchBOA(String searchPredicate, double confidenceThreshold,
                                                       int maximumNumberOfResults){

        ArrayList<BoaSearchResult> BoaResults = null;

        try{
//            String searchPhrase = "writer";
//            String searchPhrase = "http://dbpedia.org/ontology/notableWork";

//            double confidenceThreshold = 0.5D;

            Query query1 = new TermQuery(new Term("uri", searchPredicate));
            Query query2 = NumericRangeQuery.newDoubleRange("confidence", confidenceThreshold, 1D, true, true);

            BooleanQuery booleanQuery = new BooleanQuery();
            booleanQuery.add(query1, BooleanClause.Occur.MUST);
            booleanQuery.add(query2, BooleanClause.Occur.MUST);

            //System.out.println(booleanQuery);

            //Prepare the directory containing the Lucene index
            File luceneIndexFile = new File("/home/mohamed/LeipzigUniversity/JavaProjects/test/DBpediaPerformanceTester/pattern_library_en_dbpedia_wikipedia");
            IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory.open(luceneIndexFile), true));
//            IndexSearcher indexSearcher = new IndexSearcher(FSDirectory.open(new File("/home/mohamed/LeipzigUniversity/JavaProjects/test/DBpediaPerformanceTester/slr_index_dbpedia_properites")), true);

            ScoreDoc[] hits = indexSearcher.search(booleanQuery, 100).scoreDocs;

            BoaResults = new ArrayList<BoaSearchResult>();

            //If there is at least one NL represntation for the predicate, then we get them, and sort them afterwards
            if(hits.length > 0){
                for (int i = 0; i < hits.length; i++) {

                    /*System.out.println(indexSearcher.doc(hits[i].doc).get("uri"));
                    System.out.println(indexSearcher.doc(hits[i].doc).get("nlr"));
                    System.out.println(indexSearcher.doc(hits[i].doc).get("confidence"));*/

//                BoaResults.add(indexSearcher.doc(hits[i].doc).get("nlr"));
                    BoaResults.add(new BoaSearchResult(indexSearcher.doc(hits[i].doc).get("nlr").replace(",", ""),
                            indexSearcher.doc(hits[i].doc).get("uri"),
                            Double.parseDouble(indexSearcher.doc(hits[i].doc).get("confidence"))));
                }

                //Sort the results descendingly according to the confidence, so we can select top N out of the results.
                Collections.sort(BoaResults);

                //Select Top N from the list, in case that maximumNumberOfResults is less than the size of the list
                if(maximumNumberOfResults < BoaResults.size()){
                    List<BoaSearchResult> requiredSublist = BoaResults.subList(0, maximumNumberOfResults);
                    BoaResults = new ArrayList<BoaSearchResult>(requiredSublist);
                }

            }
            //Fallback case: when there is no match found in the lucene index, we get the predicate label and use it.
            else{
                String predicateLabel = DBpediaKnowledgeBaseQueryExecutor.getLabelForURI(searchPredicate);
                BoaResults.add(new BoaSearchResult(predicateLabel, searchPredicate, 1.0));
            }



        }
        catch (Exception exp){
            //System.out.println("Problem occurred because of " + exp.getMessage());
            logger.error("Problem occurred because of " + exp.getMessage());
        }

        return BoaResults;

    }
}
