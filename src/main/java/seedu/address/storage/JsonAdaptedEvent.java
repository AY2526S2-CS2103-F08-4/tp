package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Event;

/**
 * Jackson-friendly version of {@link Event}.
 */
class JsonAdaptedEvent {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Event's %s field is missing!";

    private final String description;
    private final String startTime;
    private final String endTime;

    /**
     * Constructs a {@code JsonAdaptedEvent} with the given event details.
     */
    @JsonCreator
    public JsonAdaptedEvent(@JsonProperty("description") String description,
                            @JsonProperty("startTime") String startTime,
                            @JsonProperty("endTime") String endTime) {
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Converts a given {@code Event} into this class for Jackson use.
     */
    public JsonAdaptedEvent(Event source) {
        description = source.getDescription();
        startTime = source.getStartTime();
        endTime = source.getEndTime();
    }

    /**
     * Converts this Jackson-friendly adapted event object into the model's {@code Event} object.
     */
    public Event toModelType() throws IllegalValueException {
        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "description"));
        }
        if (startTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "startTime"));
        }
        if (endTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "endTime"));
        }
        return new Event(description, startTime, endTime);
    }
}
