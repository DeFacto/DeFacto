package org.aksw.validators;

import com.vaadin.data.validator.DoubleValidator;

/**
 * Validates the value entered as a threshold for BOA
 */
@SuppressWarnings("serial")
public class BoaConfidenceThresholdValidator extends DoubleValidator {
    public BoaConfidenceThresholdValidator(String errorMessage) {
        super(errorMessage);
    }

    // The isValid() method returns simply a boolean value, so
    // it can not return an error message.
    public boolean isValid(Object value) {
        try{


            if (value == null)
                return false;

            double inputThreshold = Double.parseDouble(value.toString());

            return !(inputThreshold < 0 || inputThreshold > 1.0);
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
                        "Invalid BOA threshold value.");
            } else {
                throw new InvalidValueException("BOA threshold value must be a real number between 0.0 - 1.0.");
            }
        }
    }

}
