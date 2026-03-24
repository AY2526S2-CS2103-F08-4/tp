package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Event;
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
    private static final List<JsonAdaptedEvent> VALID_EVENTS = BENSON.getEvents().stream()
            .map(JsonAdaptedEvent::new)
            .collect(Collectors.toList());
    private static final String VALID_PHOTO = BENSON.getPhoto().map(Photo::toString).orElse("");

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_success() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        assertEquals(Optional.empty(), person.toModelType().getEmail());
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS,
                        VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_success() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_PHOTO, VALID_TAGS, VALID_EVENTS);
        assertEquals(Optional.empty(), person.toModelType().getAddress());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_PHOTO, invalidTags, VALID_EVENTS);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_duplicateEvents_throwsIllegalValueException() {
        Event duplicatedEvent = new Event("Team Sync", "21-02-26 1000", "21-02-26 1100");
        JsonAdaptedEvent adaptedEvent = new JsonAdaptedEvent(duplicatedEvent);
        List<JsonAdaptedEvent> duplicatedEvents = List.of(adaptedEvent, adaptedEvent);

        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    VALID_ADDRESS, VALID_PHOTO, VALID_TAGS, duplicatedEvents);

        String expectedMessage = String.format(JsonAdaptedPerson.DUPLICATE_EVENT_MESSAGE_FORMAT,
                VALID_NAME, duplicatedEvent);
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhoto_returnsPersonWithDefaultPhoto() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME,
                        VALID_PHONE,
                        VALID_EMAIL,
                        VALID_ADDRESS,
                        INVALID_PHOTO,
                        VALID_TAGS,
                        VALID_EVENTS);

        assertEquals("data/images/corrupted_data.jpg",
                person.toModelType().getPhoto().get().value);
    }

    @Test
    public void toModelType_nullPhoto_returnsPersonWithDefaultPhotos() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME,
                VALID_PHONE,
                VALID_EMAIL,
                VALID_ADDRESS,
                null,
                VALID_TAGS,
                VALID_EVENTS);

        assertEquals(Optional.empty(), person.toModelType().getPhoto());
    }
}
