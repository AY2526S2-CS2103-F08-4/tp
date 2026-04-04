package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;

/**
 * Jackson-friendly version of {@link Event}.
 */
class JsonAdaptedEvent {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Event's %s field is missing!";

    private final String title;
    private final String description;
    private final String startTime;
    private final String endTime;
    private final int numberOfPersonLinked;
    private final int eventId; // Unused during loading (purely for cosmetic when saving)

    /**
     * Constructs a {@code JsonAdaptedEvent} with the given event details.
     */
    @JsonCreator
    public JsonAdaptedEvent(@JsonProperty("title") String title,
                            @JsonProperty("description") String description,
                            @JsonProperty("startTime") String startTime,
                            @JsonProperty("endTime") String endTime,
                            @JsonProperty("numberOfPersonLinked") int numberOfPersonLinked,
                            @JsonProperty("eventId") int eventId) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfPersonLinked = numberOfPersonLinked;
        this.eventId = eventId; // Used only for mapping
    }

    /**
     * Converts a given {@code Event} into this class for Jackson use.
     */
    public JsonAdaptedEvent(Event source) {
        title = source.getTitle().fullTitle;
        description = source.getDescription().map(desc -> desc.fullDescription).orElse(null);
        startTime = source.getStartTimeFormatted();
        endTime = source.getEndTimeFormatted();
        numberOfPersonLinked = source.getNumberOfPersonLinked();
        eventId = source.getEventId(); // Used only for mapping
    }

    /**
     * Converts this Jackson-friendly adapted event object into the model's {@code Event} object.
     */
    public Event toModelType() throws IllegalValueException {
        if (title == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "title"));
        }
        if (!Title.isValidTitle(title)) {
            throw new IllegalValueException(Title.MESSAGE_CONSTRAINTS);
        }
        final Title modelTitle = new Title(title);

        final Optional<Description> modelDescription;
        if (description == null || description.isEmpty()) {
            modelDescription = Optional.empty();
        } else if (!Description.isValidDescription(description)) {
            throw new IllegalValueException(Description.MESSAGE_CONSTRAINTS);
        } else {
            modelDescription = Optional.of(new Description(description));
        }

        if (startTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "startTime"));
        }
        if (endTime == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "endTime"));
        }

        final TimeRange modelTimeRange;
        try {
            modelTimeRange = new TimeRange(startTime, endTime);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(TimeRange.MESSAGE_CONSTRAINTS);
        }

        return new Event(modelTitle, modelDescription, modelTimeRange, numberOfPersonLinked);
    }
}
