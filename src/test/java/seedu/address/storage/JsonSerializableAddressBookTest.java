package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Person;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersons_roundtripSuccess() throws Exception {
        AddressBook typical = TypicalPersons.getTypicalAddressBook();
        JsonSerializableAddressBook serialized = new JsonSerializableAddressBook(typical);
        assertEquals(typical, serialized.toModelType());
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_missingPersonsAndEventsFields_success() throws Exception {
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString("{}", JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();
        assertEquals(0, addressBook.getPersonList().size());
        assertEquals(0, addressBook.getEventList().size());
        assertEquals(0, addressBook.getPinnedPersonList().size());
    }

    @Test
    public void toModelType_pinnedPersons_success() throws Exception {
        String json = """
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": ["friends"],
                      "events": []
                    },
                    {
                      "name": "Benson Meier",
                      "phone": "98765432",
                      "email": "johnd@example.com",
                      "address": "311, Clementi Ave 2, #02-25",
                      "tags": ["owesMoney", "friends"],
                      "events": []
                    }
                  ],
                  "events": [],
                  "pinned": [
                    {
                      "name": "Benson Meier",
                      "phone": "98765432",
                      "email": "johnd@example.com",
                      "address": "311, Clementi Ave 2, #02-25",
                      "tags": ["owesMoney", "friends"],
                      "events": []
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        AddressBook addressBook = dataFromJson.toModelType();
        assertEquals(1, addressBook.getPinnedPersonList().size());
        Person pinned = addressBook.getPinnedPersonList().get(0);
        assertEquals("Benson Meier", pinned.getName().fullName);
    }

    @Test
    public void toModelType_duplicatePinnedPersons_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "persons": [
                    {
                      "name": "Benson Meier",
                      "phone": "98765432",
                      "email": "johnd@example.com",
                      "address": "311, Clementi Ave 2, #02-25",
                      "tags": ["owesMoney", "friends"],
                      "events": []
                    }
                  ],
                  "events": [],
                  "pinned": [
                    {
                      "name": "Benson Meier",
                      "phone": "98765432",
                      "email": "johnd@example.com",
                      "address": "311, Clementi Ave 2, #02-25",
                      "tags": ["owesMoney", "friends"],
                      "events": []
                    },
                    {
                      "name": "Benson Meier",
                      "phone": "98765432",
                      "email": "johnd@example.com",
                      "address": "311, Clementi Ave 2, #02-25",
                      "tags": ["owesMoney", "friends"],
                      "events": []
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PINNED_PERSON,
                dataFromJson::toModelType);
    }

    @Test
    public void toModelType_pinnedPersonNotInPersons_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "persons": [],
                  "events": [],
                  "pinned": [
                    {
                      "name": "Ghost",
                      "phone": "91234567",
                      "email": "ghost@example.com",
                      "address": "nowhere",
                      "tags": [],
                      "events": []
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_PINNED_PERSON_NOT_IN_PERSONS,
                dataFromJson::toModelType);
    }

    @Test
    public void toModelType_duplicateEventIds_skipsSecondEntry() throws Exception {
        // Two events with the same eventId — second is skipped with a warning, not thrown
        Event sampleEvent = new Event(new Title("Project Review"),
                Optional.of(new Description("Review scope")),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        int eventId = sampleEvent.getEventId();

        String json = String.format("""
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": [],
                      "eventIds": [%d]
                    }
                  ],
                  "events": [
                    {
                      "title": "Project Review",
                      "description": "Review scope",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 1,
                      "eventId": %d
                    },
                    {
                      "title": "Different Title",
                      "description": "Different description",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 1,
                      "eventId": %d
                    }
                  ]
                }
                """, eventId, eventId, eventId);

        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();

        // Only the first entry should be kept
        assertEquals(1, addressBook.getEventList().size());
        assertEquals("Project Review", addressBook.getEventList().get(0).getTitle().fullTitle);
    }

    @Test
    public void toModelType_personEventsReuseTopLevelEventInstances() throws Exception {
        Event sampleEvent = new Event(new Title("Project Review"),
                Optional.of(new Description("Review scope")),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"), 4);
        int eventId = sampleEvent.getEventId();

        String json = String.format("""
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": ["friends"],
                      "eventIds": [%d]
                    }
                  ],
                  "events": [
                    {
                      "title": "Project Review",
                      "description": "Review scope",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 4,
                      "eventId": %d
                    }
                  ]
                }
                """, eventId, eventId);

        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();

        Event topLevelEvent = addressBook.getEventList().get(0);
        Event linkedEvent = addressBook.getPersonList().get(0).getEvents().get(0);

        assertSame(topLevelEvent, linkedEvent);
        assertEquals(1, linkedEvent.getNumberOfPersonLinked());
    }

    @Test
    public void toModelType_orphanedEvent_isDropped() throws Exception {
        String json = """
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": ["friends"],
                      "eventIds": []
                    }
                  ],
                  "events": [
                    {
                      "title": "Ghost Event",
                      "description": "Nobody references this",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 1
                    }
                  ]
                }
                """;

        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();

        assertEquals(0, addressBook.getEventList().size());
        assertEquals(1, addressBook.getPersonList().size());
    }

    @Test
    public void toModelType_modifiedEventDetails_stillLinkedViaEventId() throws Exception {
        // Simulate a user modifying event details in the JSON file.
        // The eventId is preserved, so the person-event link should survive.
        Event original = new Event(new Title("Project Review"),
                Optional.of(new Description("Review scope")),
                new TimeRange("2026-03-25 0900", "2026-03-25 1000"));
        int eventId = original.getEventId();

        String json = String.format("""
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": "alice@example.com",
                      "address": "123, Jurong West Ave 6, #08-111",
                      "tags": [],
                      "eventIds": [%d]
                    }
                  ],
                  "events": [
                    {
                      "title": "Renamed Event",
                      "description": "Updated description",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1100",
                      "numberOfPersonLinked": 1,
                      "eventId": %d
                    }
                  ]
                }
                """, eventId, eventId);

        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();

        assertEquals(1, addressBook.getEventList().size());
        assertEquals("Renamed Event", addressBook.getEventList().get(0).getTitle().fullTitle);
        assertEquals(1, addressBook.getPersonList().get(0).getEvents().size());
    }
}
