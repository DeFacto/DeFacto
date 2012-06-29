package org.aksw.validators;


import com.vaadin.data.validator.IntegerValidator;

/**
 * Validates the value entered as a PageRank
 */
@SuppressWarnings("serial")
public class PageRankValidator extends IntegerValidator {
    public PageRankValidator(String errorMessage) {
        super(errorMessage);
    }

    // The isValid() method returns simply a boolean value, so
    // it can not return an error message.
    public boolean isValid(Object value) {
        try{
            if (value == null)
                return false;

            double inputPageRank = Integer.parseInt(value.toString());

            return !(inputPageRank < 0 || inputPageRank > 10);
        }
        catch (NumberFormatException exp){//If an exception is thrown, then an invalid value is entered
            return false;
        }
    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    public void validate(Object value)
            throws InvalidValueException {
        if (!isValid(value)) {
            if (value != null) {
                throw new InvalidValueException(
                        "Invalid PageRank value.");
            } else {
                throw new InvalidValueException("PageRank must an integer number between 1 - 10.");
            }
        }
    }

}
