package org.aksw.gui;

import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.server.JsonPaintTarget;
import com.vaadin.ui.TextField;

import java.util.List;
import java.util.Map;
/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/28/12
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */


public class AutoCompleteTextField extends TextField {
    private static final long serialVersionUID = -6051244662590740225L;
    private List<Suggestion> suggestions = null;
    private String suggestWord = "";
    private Suggester suggester = null;
    private int key = ' ';
    private int modifier = ModifierKey.CTRL;
    private int cursorPosition;
    private String text;
    private int textLength;
    public AutoCompleteTextField() {
    }
    public AutoCompleteTextField(String caption, int rows, int cols) {
        setColumns(cols);
        setRows(rows);
        setCaption(caption);
    }
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        target.addVariable(this, "suggestWord", suggestWord == null ? ""
                : suggestWord);
        target.addVariable(this, "cursor", -1);
        target.addVariable(this, "key", getKey());
        target.addVariable(this, "modifier", getModifier());
        if (suggestions != null) {
            String[] titles = new String[suggestions.size()];
            String[] suggs = new String[suggestions.size()];
            String[] suffices = new String[suggestions.size()];
            String[] starts = new String[suggestions.size()];
            String[] ends = new String[suggestions.size()];
            int i = 0;
            for (Suggestion s : suggestions) {
                titles[i] = JsonPaintTarget.escapeJSON(s.getDisplayText());
                suggs[i] = JsonPaintTarget.escapeJSON(s.getValueText());
                suffices[i] = JsonPaintTarget.escapeJSON(s.getValueSuffix());
                starts[i] = "" + s.getStartPosition();
                ends[i++] = "" + s.getEndPosition();
            }
            suggestions = null;
            target.addVariable(this, "titles", titles);
            target.addVariable(this, "suggestions", suggs);
            target.addVariable(this, "suffices", suffices);
            target.addVariable(this, "starts", starts);
            target.addVariable(this, "ends", ends);
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public void changeVariables(Object source, Map variables) {
        super.changeVariables(source, variables);
        suggestions = null;
        if (suggester != null) {
            text = getText();
            textLength = text != null ? text.length() : 0;
            suggestions = suggester.getSuggestions(this, text, 0);
        }
        if (suggestions != null) {
            requestRepaint();
        }
    }
    public interface Suggester {
        public List<Suggestion> getSuggestions(
                final AutoCompleteTextField source, final String text,
                final int cursorPosition);
    }
    /**
     * Class for single suggestion for {@link AutoCompleteTextField}.
     *
     * Single suggestion has three attributes: <li>Suggestion value itself. If
     * user selects this suggestion this value will be inserted to text field.</li>
     * <li>Display value that is presented inside the suggestion box. <li>Cursor
     * positions (start and end) for the replacement.
     */
    public class Suggestion {
        String displayText;
        String valueText;
        String valueSuffix;
        int startPosition;
        int endPosition;
        /**
         * Create new suggestion.
         *
         * @param displayText
         *            Text to display in the suggestion box
         * @param valueText
         *            Value to be inserted into text field if this suggestion is
         *            selected.
         * @param valueSuffix
         *            The part of the value that is located after the current
         *            cursor position. This is used by the client-side filtering
         *            method.
         * @param startPosition
         *            Start position of the replacement. Must be positive and
         *            below length of text in textfield. If negative number is
         *            given the current cursor position is used.
         * @param endPosition
         *            End position of the replacement. Must be positive and
         *            below length of text in textfield. If negative number is
         *            given the current cursor position is used.
         */
        public Suggestion(String displayText, String valueText,
                          String valueSuffix, int startPosition, int endPosition) {
            super();
            this.displayText = displayText;
            this.valueText = valueText;
            this.valueSuffix = valueSuffix;
            this.startPosition = startPosition < 0 ? cursorPosition
                    : startPosition;
            this.endPosition = endPosition < 0 ? cursorPosition : endPosition;
            // Text length check
            int l = getValue().toString().length();
            if (startPosition > l) {
                startPosition = l;
            }
            if (endPosition > l) {
                endPosition = l;
            }
        }
        public String getValueSuffix() {
            return valueSuffix;
        }
        public String getDisplayText() {
            return displayText;
        }
        public String getValueText() {
            return valueText;
        }
        public int getStartPosition() {
            return startPosition;
        }
        public int getEndPosition() {
            return endPosition;
        }
    }
    public Suggester getSuggester() {
        return suggester;
    }
    public void setSuggester(Suggester suggester) {
        this.suggester = suggester;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public int getKey() {
        return key;
    }
    public void setModifier(int modifier) {
        this.modifier = modifier;
    }
    public int getModifier() {
        return modifier;
    }
    public String getText() {
        return (String) getValue();
    }
    /**
     * Factory function for creating new Suggestion instances.
     *
     * @param valueText
     * @param displayText
     * @param startPosition
     * @param endPosition
     * @return new {@link Suggestion} instance
     */
    public Suggestion createSuggestion(String displayText, String valueText,
                                       String valueSuffix, int startPosition, int endPosition) {
        return new Suggestion(displayText, valueText, valueSuffix,
                startPosition, endPosition);
    }
    public int getCursorPosition() {
        return cursorPosition;
    }
    public int getTextLength() {
        return textLength;
    }
}
