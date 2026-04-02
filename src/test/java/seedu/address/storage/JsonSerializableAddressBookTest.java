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
    }

    @Test
    public void toModelType_duplicateEvents_throwsIllegalValueException() throws Exception {
        // Two events with same title+time produce the same eventId → duplicate
        String json = """
                {
                  "persons": [],
                  "events": [
                    {
                      "title": "Project Review",
                      "description": "Review scope",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 2
                    },
                    {
                      "title": "Project Review",
                      "description": "Another description",
                      "startTime": "2026-03-25 0900",
                      "endTime": "2026-03-25 1000",
                      "numberOfPersonLinked": 7
                    }
                  ]
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_EVENT,
                dataFromJson::toModelType);
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
                      "numberOfPersonLinked": 4
                    }
                  ]
                }
                """, eventId);

        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        AddressBook addressBook = dataFromJson.toModelType();

        Event topLevelEvent = addressBook.getEventList().get(0);
        Event linkedEvent = addressBook.getPersonList().get(0).getEvents().get(0);

        assertSame(topLevelEvent, linkedEvent);
        assertEquals(4, linkedEvent.getNumberOfPersonLinked());
    }

    @Test
    public void toModelType_personWithUnknownEventId_throwsIllegalValueException() throws Exception {
        String json = """
                {
                  "persons": [
                    {
                      "name": "Alice Pauline",
                      "phone": "94351253",
                      "email": null,
                      "address": null,
                      "tags": [],
                      "eventIds": [99999]
                    }
                  ],
                  "events": []
                }
                """;
        JsonSerializableAddressBook dataFromJson = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, dataFromJson::toModelType);
    }
}
