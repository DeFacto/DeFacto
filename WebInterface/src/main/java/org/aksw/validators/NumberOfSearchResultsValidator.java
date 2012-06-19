package org.aksw.validators;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.IntegerValidator;

/**
 * Validates the value entered as a the maximum number of search results per query
 */
@SuppressWarnings("serial")
public class NumberOfSearchResultsValidator extends IntegerValidator {

    public NumberOfSearchResultsValidator(String errorMessage) {
        super(errorMessage);
    }

    // The isValid() method returns simply a boolean value, so
    // it can not return an error message.
    public boolean isValid(Object value) {
        try{
            if (value == null)
                return false;

            double inputNumberOfSearchResults = Integer.parseInt(value.toString());

            return !(inputNumberOfSearchResults < 1 || inputNumberOfSearchResults > 50);
        }
        catch (NumberFormatException exp){//If an exception is thrown, then an invalid value is entered
            return false;
        }
    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    public void validate(Object value)
            throws Validator.InvalidValueException {
        if (!isValid(value)) {
            if (value != null) {
                throw new Validator.InvalidValueException(
                        "Invalid number of search results value.");
            } else {
                throw new Validator.InvalidValueException("PageRank must an integer number between 1 - 50.");
            }
        }
    }


}
