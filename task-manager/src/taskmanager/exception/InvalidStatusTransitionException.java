package taskmanager.exception;

public class InvalidStatusTransitionException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
