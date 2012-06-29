package org.aksw.gui;

import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/29/12
 * Time: 6:50 PM
 * This is an advanced ComboBox, that supports writing text directly to the ComboBox itself as in TextField
 */
public class MyComboBox extends ComboBox {

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey("filter")) {
            final String text = variables.get("filter").toString();
            fireEvent(new FieldEvents.TextChangeEvent(this) {

                @Override
                public String getText() {
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
}