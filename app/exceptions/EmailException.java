package exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: kimile
 * Date: 5/15/12
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailException extends Exception {
    public EmailException() {
        super();
    }

    public EmailException(String message) {
        super(message);
    }

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailException(Throwable cause) {
        super(cause);
    }
}
