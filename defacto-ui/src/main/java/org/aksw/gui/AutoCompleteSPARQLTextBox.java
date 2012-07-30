package org.aksw.gui;

import com.hp.hpl.jena.query.ResultSet;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import org.aksw.defacto.util.SparqlUtil;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/28/12
 * Time: 5:27 PM
 * Enables the AutoCompletion functionality for subject and object entered by the user
 */
public class AutoCompleteSPARQLTextBox implements FieldEvents.TextChangeListener {

    private Logger logger = Logger.getLogger(AutoCompleteSPARQLTextBox.class);

    @Override
    public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {

        TextField txtInputField = (TextField)textChangeEvent.getSource();
        String strInputLabel = textChangeEvent.getText();

        //Only if the user entered more than 2 letters
        if(strInputLabel.length()>2){
            String queryString  = "select ?s where {?s rdfs:label ?o. FILTER REGEX(?o, \"^"+strInputLabel+"\",\"i\") } limit 10";

            SparqlUtil sparqlEndpointDBpediaLive = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
            while (potentialLabels.hasNext()){
                logger.info(potentialLabels.next().get("s").toString());
            }
        }

        Panel popupPanel = new Panel();
        Label label = new Label("move your mouse cursor over >HERE<");
        label.setDescription("now move it >HERE<, and then click one the shadow just above description but within the popup");
        popupPanel.addComponent(label);

        PopupView popup = new PopupView("bug", popupPanel);

    }


    /*public void valueChange(Property.ValueChangeEvent event) {

        String strInputLabel = event.getProperty().toString();

        //Only if the user entered more than 2 letters
        if(strInputLabel.length()>2){
            String queryString  = "select ?s where {?s rdfs:label ?o. FILTER REGEX(?o, \"^"+strInputLabel+"\",\"i\") } limit 10";

            SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
            while (potentialLabels.hasNext()){
                logger.info(potentialLabels.next().get("s").toString());
            }
        }



    }

    @Override
    public void blur(FieldEvents.BlurEvent event) {

        ComboBox txtInputField = (ComboBox)event.getSource();
        String strInputLabel = txtInputField.getValue().toString();

        //Only if the user entered more than 2 letters
        if(strInputLabel.length()>2){
            String queryString  = "select ?s where {?s rdfs:label ?o. FILTER REGEX(?o, \"^"+strInputLabel+"\",\"i\") } limit 10";

            SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
            while (potentialLabels.hasNext()){
                logger.info(potentialLabels.next().get("s").toString());
            }

        }
    }*/
}
