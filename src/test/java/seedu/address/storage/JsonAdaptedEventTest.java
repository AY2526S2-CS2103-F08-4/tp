package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedEvent.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;

public class JsonAdaptedEventTest {

    private static final String VALID_TITLE = "Project Review";
    private static final String VALID_DESC = "Review scope";
    private static final String VALID_START = "2026-03-25 0900";
    private static final String VALID_END = "2026-03-25 1000";
    private static final int VALID_NUMBER_OF_PERSON_LINKED = 3;

    @Test
    public void toModelType_validEventDetails_returnsEvent() throws Exception {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                VALID_TITLE, VALID_DESC, VALID_START, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);

        Event event = adaptedEvent.toModelType();
        assertEquals(new Title(VALID_TITLE), event.getTitle());
        assertEquals(Optional.of(new Description(VALID_DESC)), event.getDescription());
        assertEquals(VALID_START, event.getStartTimeFormatted());
        assertEquals(VALID_END, event.getEndTimeFormatted());
        assertEquals(0, event.getNumberOfPersonLinked());
    }

    @Test
    public void toModelType_fromEventConstructor_roundTripSuccess() throws Exception {
        Event source = new Event(new Title(VALID_TITLE), Optional.of(new Description(VALID_DESC)),
                new TimeRange(VALID_START, VALID_END), VALID_NUMBER_OF_PERSON_LINKED);
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(source);
        Event modelEvent = adaptedEvent.toModelType();

        assertEquals(source.getTitle(), modelEvent.getTitle());
        assertEquals(source.getDescription(), modelEvent.getDescription());
        assertEquals(source.getStartTimeFormatted(), modelEvent.getStartTimeFormatted());
        assertEquals(source.getEndTimeFormatted(), modelEvent.getEndTimeFormatted());
        assertEquals(0, modelEvent.getNumberOfPersonLinked());
    }

    @Test
    public void toModelType_missingTitle_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                null, VALID_DESC, VALID_START, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "title"), adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_invalidTitle_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                "Bad@Title", VALID_DESC, VALID_START, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class, Title.MESSAGE_CONSTRAINTS, adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_invalidDescription_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                VALID_TITLE, "Bad#Desc", VALID_START, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class, Description.MESSAGE_CONSTRAINTS, adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_missingStartTime_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                VALID_TITLE, VALID_DESC, null, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "startTime"), adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_missingEndTime_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                VALID_TITLE, VALID_DESC, VALID_START, null, VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "endTime"), adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_invalidTimeRange_throwsIllegalValueException() {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(VALID_TITLE, VALID_DESC,
                "2026-03-25 1000", "2026-03-25 0900", VALID_NUMBER_OF_PERSON_LINKED, 0);
        assertThrows(IllegalValueException.class, TimeRange.MESSAGE_CONSTRAINTS, adaptedEvent::toModelType);
    }

    @Test
    public void toModelType_emptyDescription_setsOptionalEmpty() throws Exception {
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(
                VALID_TITLE, "", VALID_START, VALID_END, VALID_NUMBER_OF_PERSON_LINKED, 0);
        Event event = adaptedEvent.toModelType();
        assertTrue(event.getDescription().isEmpty());
    }
}
