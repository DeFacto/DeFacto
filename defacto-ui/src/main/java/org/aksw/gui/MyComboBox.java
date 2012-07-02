package org.aksw.gui;

import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/29/12
 * Time: 6:50 PM
 * This is an advanced ComboBox, that supports writing text directly to the ComboBox itself as in TextField
 */
public class MyComboBox extends ComboBox {

    //This member is needed as the ComboBox in VAADIN has a problem, which is when an item is selected and it was added on
    //the fly, even if the caption of the item is set different to the item itself, it will remain unchanged
    public HashMap<String, String> itemsList;

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey("filter")) {
            final String text = variables.get("filter").toString();
            fireEvent(new FieldEvents.TextChangeEvent(this) {

                @Override
                public String getText() {
                    /*if(text.contains(" ")){
                        int x = 0;
                    }*/
                    return text;
                }

                @Override
                public int getCursorPosition() {
                    return text.length();
                }
            });
        }
        super.changeVariables(source, variables);
    }

    public void addListener(FieldEvents.TextChangeListener listener) {
        addListener(FieldEvents.TextChangeListener.EVENT_ID, FieldEvents.TextChangeEvent.class,
                listener, FieldEvents.TextChangeListener.EVENT_METHOD);
    }

    public void removeListener(FieldEvents.TextChangeListener listener) {
        removeListener(FieldEvents.TextChangeListener.EVENT_ID, FieldEvents.TextChangeEvent.class,
                listener);
    }

    public String getSelectedItemCaption() {

        String selectedItem = this.getValue().toString().trim();

        UrlValidator validator = new UrlValidator();

        for(Map.Entry <String, String> item: itemsList.entrySet()){
            if(selectedItem.compareTo(item.getValue()) == 0 ) {
                if(validator.isValid(item.getKey()))
                    return  item.getKey();
                else if(validator.isValid(item.getValue()))
                    return  item.getValue();
            }

        }

        return selectedItem;
    }

}