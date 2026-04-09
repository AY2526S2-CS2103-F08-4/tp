package seedu.address.model.event.exceptions;

/**
 * Signals that the event clashes with (overlaps) an existing event.
 */
public class ClashingEventException extends RuntimeException {
    public ClashingEventException() {
        super("The event clashes with 1 or more existing events.");
    }
}
