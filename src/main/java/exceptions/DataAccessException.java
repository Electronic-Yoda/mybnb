package exceptions;

/*
Note: this is an unchecked exception. It extends RuntimeException, not Exception.
it will continue to propagate up the call stack until it either hits
a method that catches it or it reaches the very top of the call stack
(which is typically your application's main method).
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    public DataAccessException(String message) {
        super(message);
    }
}
