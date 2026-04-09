package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an Event in the address book.
 */
public class Event {

    private static final int INITIAL_PERSON_LINK_COUNT = 1;

    private final Title title;
    private final TimeRange timeRange;

    private final int eventId;
    private int numberOfPersonLinked;

    private final Optional<Description> description;

    /**
     * Creates an Event. Title and time range are required. Description is optional.
     * {@code numberOfPersonLinked} starts at 1 for a newly created event.
     */
    public Event(Title title, Optional<Description> description, TimeRange timeRange) {
        requireAllNonNull(title, description, timeRange);
        this.title = title;
        this.description = description;
        this.timeRange = timeRange;
        this.numberOfPersonLinked = INITIAL_PERSON_LINK_COUNT;
        this.eventId = Objects.hash(title.fullTitle, timeRange.getStartTimeFormatted(),
            timeRange.getEndTimeFormatted());
    }

    /**
     * Reconstruction constructor for storage deserialization.
     * Restores an event with a specific participant count instead of defaulting to 1.
     */
    public Event(Title title, Optional<Description> description, TimeRange timeRange, int numberOfPersonLinked) {
        requireAllNonNull(title, description, timeRange);
        this.title = title;
        this.description = description;
        this.timeRange = timeRange;
        this.numberOfPersonLinked = numberOfPersonLinked;
        this.eventId = Objects.hash(title.fullTitle, timeRange.getStartTimeFormatted(),
            timeRange.getEndTimeFormatted());
    }

    /**
     * Reconstruction constructor for storage deserialization with an explicit event ID.
     * Uses the saved {@code eventId} directly for person-event mapping, so that
     * modified event details do not break existing links.
     */
    public Event(Title title, Optional<Description> description, TimeRange timeRange,
                 int numberOfPersonLinked, int eventId) {
        requireAllNonNull(title, description, timeRange);
        this.title = title;
        this.description = description;
        this.timeRange = timeRange;
        this.numberOfPersonLinked = numberOfPersonLinked;
        this.eventId = eventId;
    }

    /**
     * Returns the title of this event.
     */
    public Title getTitle() {
        return title;
    }

    /**
     * Returns the time range of this event.
     */
    public TimeRange getTimeRange() {
        return timeRange;
    }

    /**
     * Returns the stable identifier for this event.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Returns the number of persons currently linked to this event.
     */
    public int getNumberOfPersonLinked() {
        return numberOfPersonLinked;
    }

    /**
     * Returns the optional description of this event.
     */
    public Optional<Description> getDescription() {
        return description;
    }

    /**
     * Increments the count of linked persons.
     */
    public void incrementNumberOfPersonLinked() {
        numberOfPersonLinked += 1;
    }

    /**
     * Decrements the count of linked persons.
     * Throws if the count would go below zero.
     */
    public void decrementNumberOfPersonLinked() {
        if (numberOfPersonLinked <= 0) {
            throw new IllegalStateException("numberOfPersonLinked cannot be negative.");
        }
        numberOfPersonLinked -= 1;
    }

    /**
     * Returns the formatted start time of this event.
     */
    public String getStartTimeFormatted() {
        return timeRange.getStartTimeFormatted();
    }

    /**
     * Returns the formatted end time of this event.
     */
    public String getEndTimeFormatted() {
        return timeRange.getEndTimeFormatted();
    }

    /**
     * Returns true if both event is the same
     * We define a event to be uniquely identified by event identifier
     */
    public boolean isSameEvent(Event otherEvent) {
        if (otherEvent == this) {
            return true;
        }

        return otherEvent != null
                && otherEvent.title.equals(title)
                && otherEvent.timeRange.equals(timeRange);
    }

    /**
     * Returns true if both event's time range are overlapping
     */
    public boolean isClashingWith(Event otherEvent) {
        return timeRange.isOverlapping(otherEvent.timeRange);
    }

    /**
     * Returns true if this event starts at {@code otherStartTime}.
     */
    public boolean hasSameStartTime(LocalDateTime otherStartTime) {
        requireNonNull(otherStartTime);
        return timeRange.hasSameStartTime(otherStartTime);
    }

    /**
     * Returns a human-readable representation of this event.
     */
    @Override
    public String toString() {
        String duration = "From " + timeRange;
        if (description.isPresent()) {
            return String.format("Title: %s, Description: %s, Duration: %s",
                    title, description.get(), duration);
        }
        return String.format("Title: %s, Duration: %s", title, duration);
    }

    /**
     * Returns a formatted string representation of the event for clash warnings.
     */
    public String getClashDisplayString() {
        return String.format("%s -> (%s)", title.fullTitle, timeRange.toString());
    }

    /**
     * Return true if both Event have the same title and timeRange
     * @param other   the reference object with which to compare.
     * @return True if the events have the same title and timeRange, else return false
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Event otherEvent) {
            return title.equals(otherEvent.title)
                    && timeRange.equals(otherEvent.timeRange);
        }
        return false;
    }

    /**
     * Returns the hash code of this event.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.title, this.description, this.timeRange);
    }
}
