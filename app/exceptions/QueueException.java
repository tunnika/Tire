package exceptions;

public class QueueException extends Exception {
    public QueueException() {
        super();
    }

    public QueueException(String message) {
        super(message);
    }

    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueException(Throwable cause) {
        super(cause);
    }
}
