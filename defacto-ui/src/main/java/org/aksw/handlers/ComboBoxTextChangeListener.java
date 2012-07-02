package org.aksw.handlers;

import com.vaadin.event.FieldEvents;
import org.aksw.gui.MyComboBox;
import org.aksw.helper.SolrSearcher;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/30/12
 * Time: 12:01 PM
 * Whenever the text of the ComboBox changes, we should contact the lucene index of DBpedia, adn get suggestions
 */
public class ComboBoxTextChangeListener implements FieldEvents.TextChangeListener {

    private Logger logger = Logger.getLogger(ComboBoxTextChangeListener.class);

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {
        String labelPart = event.getText();

        if((labelPart == null) || (labelPart.compareTo("") == 0))//Do nothing if the text is empty
            return;

        MyComboBox cmb = (MyComboBox)event.getSource();

        /*if(labelPart.contains("http://")){ //If the user is pasting a URL directly without searching, then we should not
            cmb.removeAllItems();          //search our lucene index, and just add it to the combobox
            cmb.addItem(labelPart);
            cmb.setValue(labelPart);
        }*/

        SolrSearcher slrSearcher = new SolrSearcher();
        HashMap<String, String> suggestionsList = slrSearcher.getResources(labelPart);


        if(suggestionsList.size() > 0){
            cmb.itemsList = suggestionsList;
            cmb.removeAllItems();
            for(Map.Entry<String, String> suggestion: suggestionsList.entrySet()){
                cmb.addItem(suggestion.getKey());
                cmb.setItemCaption(suggestion.getKey(), suggestion.getValue());
            }
        }


    }

}
