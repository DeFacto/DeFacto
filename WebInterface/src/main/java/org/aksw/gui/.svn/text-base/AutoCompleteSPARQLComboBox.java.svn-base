package org.aksw.gui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import com.vaadin.ui.ComboBox;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/28/12
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompleteSPARQLComboBox extends ComboBox{

    private static final long serialVersionUID = 8862886677356906794L;
    String sparqlEndpoint;
    String prefix;
    final EventList<String> itemList = GlazedLists.eventList(new LinkedList<String>());

    @SuppressWarnings("unused")
    private AutoCompleteSPARQLComboBox() {};

    public AutoCompleteSPARQLComboBox(String sparqlEndpoint, String prefix)
    {
//        this.setEditable(true);
//        this.getEditor().getEditorComponent().addKeyListener(new AutoCompleteSPARQLJComboBoxListener());
        this.sparqlEndpoint = sparqlEndpoint;
        this.prefix = prefix;

        try
        {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
//                    AutoCompleteSupport<String> support = AutoCompleteSupport.install(AutoCompleteSPARQLComboBox.this, itemList);
//                    support.setFilterMode(TextMatcherEditor.CONTAINS); // <- deswegen hats wohl noch keine
                }
            });
        } catch (Exception e) {throw new RuntimeException(e);}

    }

    private class AutoCompleteSPARQLJComboBoxListener implements KeyListener
    {

        @Override
        public void keyPressed(KeyEvent arg0){}

        @Override
        public void keyReleased(KeyEvent arg0){}

        @Override
        public void keyTyped(KeyEvent arg0)
        {
            /*String uriPart = ((JTextField)AutoCompleteSPARQLComboBox.this.getEditor().getEditorComponent()).getText();
            System.out.println(uriPart);
            if(uriPart.length()>2)
            {
                String queryString  = "select ?s where {?s rdfs:label ?o. FILTER REGEX(?o, \"^"+uriPart+"\",\"i\") } limit 10";

                ResultSet rs = SPARQLHelper.query(sparqlEndpoint, null, queryString);
                while(rs.hasNext())
                {

                    AutoCompleteSPARQLComboBox.this.itemList.add(rs.next().get("s").toString());
                }

            }*/
            //+arg0.getKeyChar()
            //			if(AutoCompleteSPARQLJComboBox.this.getSelectedItem()!=null)
            //				AutoCompleteSPARQLJComboBox.this.addItem(AutoCompleteSPARQLJComboBox.this.getSelectedItem().toString());
            //			AutoCompleteSPARQLJComboBox.this.addItem("bla"+System.currentTimeMillis());
        }

    }

}
