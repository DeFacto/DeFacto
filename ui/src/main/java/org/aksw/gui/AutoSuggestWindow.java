package org.aksw.gui;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/28/12
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoSuggestWindow extends Window {

    VerticalLayout verticalLayout = null;
    public AutoSuggestWindow()
    {
        this.setWidth("500px");
        this.setHeight("400px");
        verticalLayout = (VerticalLayout) this.getContent();
        verticalLayout.setSizeFull();
        /***
         * get data here… from database, file..etc
         * List<ValueObject> data = getData();
         */

//creating auto suggest component here… and passing data
        //AutoSuggest autoSuggest = new AutoSuggest(data);
        //this.addComponent(autoSuggest);
    }

}
