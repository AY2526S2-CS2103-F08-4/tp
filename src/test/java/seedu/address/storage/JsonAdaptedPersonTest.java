package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_EVENT_MESSAGE_FORMAT;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "123456";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_PHOTO = "bloodhound.exe";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().map(Email::toString).orElse("");
    private static final String VALID_ADDRESS = BENSON.getAddress().map(Address::toString).orElse("");
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());
    private static final String VALID_PHOTO = "valid.jpg";

    /** Builds an eventMap containing all of BENSON's events, keyed by eventId. */
    private static Map<Integer, Event> bensonEventMap() {
        Map<Integer, Event> map = new HashMap<>();
        for (Event e : BENSON.getEvents()) {
            map.put(e.getEventId(), e);
        }
        return map;
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType(bensonEventMap()));
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_nullEmail_success() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of());
        assertEquals(Optional.empty(), person.toModelType(new HashMap<>()).getEmail());
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, List.of());
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_nullAddress_success() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_PHOTO, VALID_TAGS, List.of());
        assertEquals(Optional.empty(), person.toModelType(new HashMap<>()).getAddress());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, invalidTags, List.of());
        assertThrows(IllegalValueException.class, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void toModelType_invalidPhoto_returnsPersonWithDefaultPhoto() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                INVALID_PHOTO, VALID_TAGS, List.of());
        assertEquals("data/images/corrupted_data.jpg",
                person.toModelType(new HashMap<>()).getPhoto().get().value);
    }

    @Test
    public void toModelType_nullPhoto_returnsPersonWithNoPhoto() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, VALID_TAGS, List.of());
        assertEquals(Optional.empty(), person.toModelType(new HashMap<>()).getPhoto());
    }

    @Test
    public void toModelType_validPhoto_returnsPersonWithValidPhoto() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of());
        assertEquals(Optional.of(new Photo(VALID_PHOTO)), person.toModelType(new HashMap<>()).getPhoto());
    }

    @Test
    public void toModelType_withEventMap_reusesMappedEventInstance() throws IllegalValueException {
        Event sharedEvent = new Event(new Title("Project Review"), Optional.of(new Description("Review scope")),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"), 5);
        Map<Integer, Event> eventMap = new HashMap<>();
        eventMap.put(sharedEvent.getEventId(), sharedEvent);

        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of(sharedEvent.getEventId()));

        Event personEvent = person.toModelType(eventMap).getEvents().get(0);
        assertSame(sharedEvent, personEvent);
        assertEquals(5, personEvent.getNumberOfPersonLinked());
    }

    @Test
    public void toModelType_unknownEventId_throwsIllegalValueException() {
        int unknownId = 99999;
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, List.of(unknownId));
        String expectedMessage = String.format(MISSING_EVENT_MESSAGE_FORMAT, unknownId, VALID_NAME);
        assertThrows(IllegalValueException.class, expectedMessage, () -> person.toModelType(new HashMap<>()));
    }

    @Test
    public void fromSource_serializesEventIdsNotFullEvents() throws Exception {
        // Verify that a person serialized then deserialized with the correct event map
        // returns the same events — confirming eventIds are saved and resolved correctly.
        Map<Integer, Event> eventMap = bensonEventMap();
        JsonAdaptedPerson adapted = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON.getEvents(), adapted.toModelType(eventMap).getEvents());
    }
}
