package org.aksw.handlers;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.vaadin.event.FieldEvents;
import org.aksw.gui.MyComboBox;
import org.aksw.helper.SPARQL;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/30/12
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComboBoxTextChangeListener implements FieldEvents.TextChangeListener {

    private Logger logger = Logger.getLogger(ComboBoxTextChangeListener.class);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();


    private static <T> T timedCall(FutureTask<T> task, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        THREAD_POOL.execute(task);
        return task.get(timeout, timeUnit);
    }

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {



        String subjectLabelPart = event.getText();
        MyComboBox cmb = (MyComboBox)event.getSource();
        //Only if the user entered more than 2 letters
        if (subjectLabelPart.length() > 2) {
            cmb.removeAllItems();
//            subjectProgressIndicator.setVisible(true);
//            subjectProgressIndicator.requestRepaint();
            /*String queryString = "select ?s ?o where {?s rdfs:label ?o. FILTER REGEX(?o, \"^" + subjectLabelPart + "\",\"i\") } limit 10";

            SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
            while (potentialLabels.hasNext()) {

                QuerySolution slnSubjectWithLabel = potentialLabels.next();

                //Add the subject with its label to the arraylist of subjects
//                        arrSubjects.add(new UriWithLabel(slnSubjectWithLabel.get("s").toString(), slnSubjectWithLabel.get("o").toString()));

                cmb.addItem(slnSubjectWithLabel.get("s").toString());
            }
              */

            LabelPartSearcherWithLimit labelSearcher = new LabelPartSearcherWithLimit(subjectLabelPart);

            FutureTask<ArrayList<QuerySolution>> queryExecutionTask = new FutureTask<ArrayList<QuerySolution>>(labelSearcher);
            try{
                ArrayList<QuerySolution> solutions = timedCall(queryExecutionTask, 3000, TimeUnit.MILLISECONDS);

                for (QuerySolution sln: solutions){
                    cmb.addItem(sln.get("s").toString());
                }
            }
            catch (Exception exp){
                queryExecutionTask.cancel(true);
                logger.error("Suggestions will take too long to get fetched, Operation Terminated");

            }//            cmb.requestRepaint();
//            subjectProgressIndicator.setVisible(false);
//            subjectProgressIndicator.requestRepaint();
        }
        else 
            cmb.removeAllItems();


    }

    /**
     * This class implements Callable interface, which provides the ability to place certain time limit on the execution
     * time of a function, so it must end after a specific time limit
     */
    private class LabelPartSearcherWithLimit implements Callable<ArrayList<QuerySolution>> {

        private String labelPart;
        public LabelPartSearcherWithLimit(String labelPart){
                this.labelPart = labelPart;
        }

        public ArrayList<QuerySolution> call(){

            ArrayList<QuerySolution> arrResults = new ArrayList<QuerySolution>();
            try{


                String queryString = "select ?s ?o where {?s rdfs:label ?o. FILTER REGEX(?o, \"^" + labelPart + "\",\"i\") } limit 20";

                SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

                ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
                while (potentialLabels.hasNext()) {

                    QuerySolution slnSubjectWithLabel = potentialLabels.next();

                    //Add the subject with its label to the arraylist of subjects
//                        arrSubjects.add(new UriWithLabel(slnSubjectWithLabel.get("s").toString(), slnSubjectWithLabel.get("o").toString()));

                    arrResults.add(slnSubjectWithLabel);
                }

            }
            catch (Exception exp){
                logger.error("Suggestions cannot be fetched, due to ", exp);
            }
            finally {
                return arrResults;
            }
        }
    }

}
