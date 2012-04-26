package exceptions;


import javax.persistence.PersistenceException;

public class FormValidationException extends Exception {

    public FormValidationException() {
    }

    public FormValidationException(PersistenceException pe){
    }

    public FormValidationException(String message) {
        super(message);
    }

    public FormValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormValidationException(Throwable cause) {
        super(cause);
    }
}
