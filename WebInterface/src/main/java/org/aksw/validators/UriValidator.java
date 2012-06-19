package org.aksw.validators;

import com.vaadin.data.validator.AbstractValidator;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 5/1/12
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class UriValidator extends AbstractValidator{
    public boolean isValid(Object value) {

        try{
            URL url = new URL((String) value);
            url.toURI();
            /*if(urlValidator.isValid())
                return true;
            else
                return false;*/
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public UriValidator(String errorMessage) {
        super(errorMessage);
    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    public void validate(Object value)
            throws InvalidValueException {
        if (!isValid(value)) {
            if (value != null) {
                throw new InvalidValueException(
                        "Invalid URI.");
            } else {
                throw new InvalidValueException("URI is required. ");
            }
        }
    }

}
