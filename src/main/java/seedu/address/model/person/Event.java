package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
/**
 * Represents an Event in the address book.
 */
public class Event {
    private final String description;
    private final String startTime;
    private final String endTime;

    /**
     * Creating an Event
     * @param description Description of the event
     * @param startTime Start Date & time of the event
     * @param endTime End Date & time of the event
     */
    public Event(String description, String startTime, String endTime) {
        requireNonNull(description);
        requireNonNull(startTime);
        requireNonNull(endTime);
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    @Override
    public String toString() {
        return String.format("%s from %s to %s.", this.description, this.startTime, this.endTime);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (other instanceof Event e) {
            return this.description.equals(e.description)
                    && this.startTime.equals(e.startTime)
                    && this.endTime.equals(e.endTime);
        }

        return false;
    }
}
