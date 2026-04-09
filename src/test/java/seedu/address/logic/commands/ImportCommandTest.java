package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.ImportCommand.MESSAGE_ERROR_READING_FILE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;
    private final String personsHeader = "Name,Phone,Email,Address,Tags,EventIds,Photo,Pinned";
    private final String eventsHeader = "EventId,Title,Description,Start,End";

    private void createPersonsCsvFile(String fileName, String content) throws Exception {
        Path filePath = testFolder.resolve(fileName + "_persons.csv");
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    private void createEventsCsvFile(String fileName, String content) throws Exception {
        Path filePath = testFolder.resolve(fileName + "_events.csv");
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    public ImportCommand createTestCommand(String importType, String filename) {
        return new ImportCommand(importType, filename) {
            @Override
            protected Path getEventsImportPath(Model model) {
                return testFolder.resolve(filename + "_events.csv");
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return testFolder.resolve(filename + "_persons.csv");
            }
        };
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path personsPath = model.getAddressBookFilePath().getParent().resolve("test_import_persons.csv");
        Path eventsPath = model.getAddressBookFilePath().getParent().resolve("test_import_events.csv");
        Files.deleteIfExists(personsPath);
        Files.deleteIfExists(eventsPath);
    }

    @Test
    public void execute_overwriteImportType_wipesExistingData() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withPhoto("hello_world.jpg")
                .build();

        Person expectedTest2 = new PersonBuilder()
                .withName("David")
                .withPhone("91234567")
                .withEmail("david@u.nus.edu")
                .withAddress("Blk 456")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        model.addPerson(expectedTest1);

        createEventsCsvFile("valid", eventsHeader);
        createPersonsCsvFile("valid", personsHeader + "\nDavid,91234567,david@u.nus.edu,Blk 456,,,,false");

        ImportCommand command = createTestCommand("overwrite", "valid");
        command.execute(model);

        assertFalse(model.hasPerson(expectedTest1));
        assertTrue(model.hasPerson(expectedTest2));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addWithDuplicates_skipsExistingData() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(alice);

        String personData = "Alice Pauline,12345678,alice@u.nus.edu,Blk 123,,,,false\n"
                + "Bob,88662211,bob@u.nus.edu,Blk 123,,,,false";

        createEventsCsvFile("merge", eventsHeader);
        createPersonsCsvFile("merge", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("add", "merge");
        command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Bob")
                .withPhone("88662211")
                .withEmail("bob@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .build();

        assertEquals(9, model.getAddressBook().getPersonList().size());
        assertTrue(model.hasPerson(expectedPerson));
    }

    @Test
    public void execute_overwriteImportType_handlesEmptyOptionalField() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withoutEmail()
                .withoutAddress()
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("valid", eventsHeader);
        createPersonsCsvFile("valid", personsHeader + "\nAlice,12345678,,,,,,,false");

        ImportCommand command = createTestCommand("overwrite", "valid");
        command.execute(model);

        assertTrue(model.hasPerson(expectedTest1));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_overwriteImportType_handlesPhotoField() throws Exception {
        Person expectedTest1 = new PersonBuilder()
                .withName("Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withPhoto("hello_world.jpg")
                .build();

        String testPersonStr = "\nAlice,12345678,alice@u.nus.edu,Blk 123,,,hello_world.jpg,false";
        createEventsCsvFile("photo", eventsHeader);
        createPersonsCsvFile("photo", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("overwrite", "photo");
        command.execute(model);

        assertTrue(model.hasPerson(expectedTest1));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(command.execute(model).getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_importPinnedAndLegacyRows_restoresPinnedStateAndAcceptsOldFormat() throws Exception {
        Model modelWithPins = new ModelManager();

        Person pinnedPerson = new PersonBuilder()
                .withName("Pinned Alice")
                .withPhone("12345678")
                .withEmail("alice@u.nus.edu")
                .withAddress("Blk 123")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();
        Person legacyPerson = new PersonBuilder()
                .withName("Legacy Bob")
                .withPhone("87654321")
                .withEmail("bob@u.nus.edu")
                .withAddress("Blk 456")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("pinnedAndLegacy", eventsHeader);
        createPersonsCsvFile("pinnedAndLegacy",
                personsHeader + "\n"
                        + "Pinned Alice,12345678,alice@u.nus.edu,Blk 123,,,,true\n"
                        + "Legacy Bob,87654321,bob@u.nus.edu,Blk 456,,,,false");

        ImportCommand command = createTestCommand("overwrite", "pinnedAndLegacy");
        CommandResult result = command.execute(modelWithPins);

        assertTrue(result.getFeedbackToUser().contains("2 row(s) added"));
        assertTrue(modelWithPins.hasPerson(pinnedPerson));
        assertTrue(modelWithPins.hasPerson(legacyPerson));
        assertTrue(modelWithPins.isPersonPinned(pinnedPerson));
        assertFalse(modelWithPins.isPersonPinned(legacyPerson));
    }

    @Test
    public void execute_importPinnedFalseRow_doesNotPinPerson() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Unpinned User")
                .withPhone("92345678")
                .withEmail("unpinned@u.nus.edu")
                .withAddress("Blk 789")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("pinnedFalse", eventsHeader);
        createPersonsCsvFile("pinnedFalse",
                personsHeader + "\n"
                        + "Unpinned User,92345678,unpinned@u.nus.edu,Blk 789,,,,false");

        ImportCommand command = createTestCommand("overwrite", "pinnedFalse");
        CommandResult result = command.execute(modelWithPins);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(modelWithPins.hasPerson(person));
        assertFalse(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_importInvalidPinnedValue_treatsAsUnpinned() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Invalid Pin")
                .withPhone("93456789")
                .withEmail("invalidpin@u.nus.edu")
                .withAddress("Blk 111")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("invalidPinnedValue", eventsHeader);
        createPersonsCsvFile("invalidPinnedValue",
                personsHeader + "\n"
                        + "Invalid Pin,93456789,invalidpin@u.nus.edu,Blk 111,,,,yes");

        ImportCommand command = createTestCommand("overwrite", "invalidPinnedValue");
        command.execute(modelWithPins);

        assertTrue(modelWithPins.hasPerson(person));
        assertFalse(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_importUpperCaseTruePinnedValue_pinsPerson() throws Exception {
        Model modelWithPins = new ModelManager();

        Person person = new PersonBuilder()
                .withName("Case True")
                .withPhone("94567890")
                .withEmail("casetrue@u.nus.edu")
                .withAddress("Blk 222")
                .withTags()
                .withEvents()
                .withoutPhoto()
                .build();

        createEventsCsvFile("uppercaseTrue", eventsHeader);
        createPersonsCsvFile("uppercaseTrue",
                personsHeader + "\n"
                        + "Case True,94567890,casetrue@u.nus.edu,Blk 222,,,,TRUE");

        ImportCommand command = createTestCommand("overwrite", "uppercaseTrue");
        command.execute(modelWithPins);

        assertTrue(modelWithPins.hasPerson(person));
        assertTrue(modelWithPins.isPersonPinned(person));
    }

    @Test
    public void execute_fileNotFound_throwsCommandException() {
        ImportCommand command = createTestCommand("add", "invalid_file");

        String expectedMessage = String.format(MESSAGE_ERROR_READING_FILE, "invalid_file_events.csv");
        assertCommandFailure(command, model, expectedMessage);
    }

    @Test
    public void execute_csvHeaderOnly_returnsEmptyMessage() throws Exception {
        createEventsCsvFile("empty", eventsHeader);
        createPersonsCsvFile("empty", personsHeader);

        ImportCommand command = createTestCommand("add", "empty");
        CommandResult result = command.execute(model);

        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_persons.csv"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyCsvFile_throwsCommandException() throws Exception {
        Path emptyEventsPath = testFolder.resolve("empty_events.csv");
        Path emptyPersonsPath = testFolder.resolve("empty_persons.csv");
        Files.write(emptyEventsPath, new byte[0]);
        Files.write(emptyPersonsPath, new byte[0]);

        ImportCommand command = createTestCommand("add", "empty");

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_events.csv"),
                exception.getMessage());
    }

    @Test
    public void execute_rowsWithAndWithoutPhoto_importsBothPersons() throws Exception {
        String personData = "Photo User,81234567,photo@u.nus.edu,Blk 123,,,avatar.png,false\n"
                + "No Photo,82345678,nophoto@u.nus.edu,Blk 456,,,,false";

        createEventsCsvFile("mixedPhoto", eventsHeader);
        createPersonsCsvFile("mixedPhoto", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("overwrite", "mixedPhoto");
        command.execute(model);

        Person withPhoto = new PersonBuilder()
                .withName("Photo User")
                .withPhone("81234567")
                .withEmail("photo@u.nus.edu")
                .withAddress("Blk 123")
                .withPhoto("avatar.png")
                .build();
        Person withoutPhoto = new PersonBuilder()
                .withName("No Photo")
                .withPhone("82345678")
                .withEmail("nophoto@u.nus.edu")
                .withAddress("Blk 456")
                .withoutPhoto()
                .build();

        assertTrue(model.hasPerson(withPhoto));
        assertTrue(model.hasPerson(withoutPhoto));
    }

    @Test
    public void execute_invalidDataRow_skipsAndReports() throws Exception {
        String personData = "Valid,91234567,valid@u.nus.edu,Blk 123,,,,false\n"
                + "Invalid,abcd,invalid@u.nus.edu,Blk 123,,,,false";

        createEventsCsvFile("invalidRow", eventsHeader);
        createPersonsCsvFile("invalidRow", personsHeader + "\n" + personData);

        ImportCommand command = createTestCommand("add", "invalidRow");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(result.getFeedbackToUser().contains("1 row(s) skipped"));
    }

    @Test
    public void execute_rowWithExistingGlobalEvent_skipsDuplicateGlobalEventAdd() throws Exception {
        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        model.addEvent(existingEvent);
        int eventCountBeforeImport = model.getAddressBook().getEventList().size();

        int eventId = existingEvent.getEventId();
        String testEventStr = "\n" + eventId + ",Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100";
        String testPersonStr = "\nEve,81234567,eve@u.nus.edu,Blk 321,," + eventId + ",,false";
        createEventsCsvFile("eventExists", eventsHeader + testEventStr);
        createPersonsCsvFile("eventExists", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "eventExists");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertEquals(eventCountBeforeImport, model.getAddressBook().getEventList().size());
    }

    @Test
    public void execute_rowWithOverlappingGlobalEvent_skipsOverlappingGlobalEventAdd() throws Exception {
        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        model.addEvent(existingEvent);
        int eventCountBeforeImport = model.getAddressBook().getEventList().size();

        createEventsCsvFile("eventOverlap", eventsHeader + "\n888,Consult,Discussion,2026-05-06 1000,2026-05-06 1100");
        createPersonsCsvFile("eventOverlap", personsHeader + "\nEve,81234567,eve@u.nus.edu,Blk 321,,888,,false");

        ImportCommand command = createTestCommand("add", "eventOverlap");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertEquals(eventCountBeforeImport, model.getAddressBook().getEventList().size());
    }

    @Test
    public void execute_duplicateComputedEventId_throwsCommandException() throws Exception {
        int personCountBeforeImport = model.getAddressBook().getPersonList().size();
        int eventCountBeforeImport = model.getAddressBook().getEventList().size();

        createEventsCsvFile("duplicateComputedEventId", eventsHeader + "\n"
                + "101,Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100\n"
                + "999,Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100");
        createPersonsCsvFile("duplicateComputedEventId", personsHeader + "\n"
                + "Eve,81234567,eve@u.nus.edu,Blk 321,,,,false");

        ImportCommand command = createTestCommand("add", "duplicateComputedEventId");
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertTrue(exception.getMessage().contains("Duplicate event detected in import: EventId"));
        assertEquals(personCountBeforeImport, model.getAddressBook().getPersonList().size());
        assertEquals(eventCountBeforeImport, model.getAddressBook().getEventList().size());
    }


    @Test
    public void execute_fileDoesNotExist_throwsCommandException() {
        ImportCommand importCommand = new ImportCommand("add", "nonExistentFile");
        assertCommandFailure(importCommand, model,
                String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, "nonExistentFile_events.csv"));
    }

    @Test
    public void execute_headerOnlyFile_returnsEmptyFileMessage() throws Exception {
        Path eventsPath = testFolder.resolve("empty_events.csv");
        Path personsPath = testFolder.resolve("empty_persons.csv");
        Files.writeString(eventsPath, eventsHeader);
        Files.writeString(personsPath, personsHeader);

        ImportCommand importCommand = new ImportCommand("add", "empty") {
            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsPath;
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty_persons.csv");
        CommandResult result = importCommand.execute(model);
        assertEquals(expectedMessage, result.getFeedbackToUser());
    }

    @Test
    public void readLinesFromCsv_fileAccessError_throwsCommandException() {
        String dirname = "notAFile";
        Path dirPath = testFolder.resolve(dirname);
        assertTrue(dirPath.toFile().mkdir());

        ImportCommand importCommand = new ImportCommand("add", dirname) {
            @Override
            protected Path getEventsImportPath(Model model) {
                return dirPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return dirPath.resolve(dirname + "_persons.csv");
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, dirname + "_events.csv");

        assertThrows(CommandException.class, () -> importCommand.execute(model), expectedMessage);
    }

    @Test
    public void processImportedLines_invalidColumnCount_skipsRow() throws Exception {
        Path eventsPath = testFolder.resolve("malformed_events.csv");
        Path personsPath = testFolder.resolve("malformed_persons.csv");
        List<String> eventsLines = List.of(eventsHeader);
        List<String> personLines = List.of(
                personsHeader,
                "John Doe,91234567",
                "Yohan,67676868,,,"
        );
        Files.write(eventsPath, eventsLines);
        Files.write(personsPath, personLines);

        ImportCommand importCommand = new ImportCommand("add", "malformed") {
            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsPath;
            }

            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsPath;
            }
        };

        CommandResult result = importCommand.execute(model);

        String expectedFeedback = String.format(ImportCommand.MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                "malformed", 0, 2);
        assertEquals(expectedFeedback, result.getFeedbackToUser());
    }


    @Test
    public void parseTags_validTagString_returnsTagSet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        var tags = importCommand.parseTags("friends;colleagues");
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("friends")));
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("colleagues")));
    }

    @Test
    public void parseTags_nullOrEmpty_returnsEmptySet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseTags(null).isEmpty());
        assertTrue(importCommand.parseTags("").isEmpty());
        assertTrue(importCommand.parseTags("   ").isEmpty());
    }

    @Test
    public void parseTags_withEmptyTokens_ignoresBlankTags() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        var tags = importCommand.parseTags("friends;; colleagues; ;");

        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("friends")));
        assertTrue(tags.stream().anyMatch(t -> t.tagName.equals("colleagues")));
    }

    @Test
    public void parseTags_onlyBlankTokens_returnsEmptySet() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseTags(" ; ; ").isEmpty());
    }

    @Test
    public void execute_rowWithTags_importsPerson() throws Exception {
        String testPersonStr = "\nCharlie,87654321,charlie@u.nus.edu,Blk 789,friends;CS2103,,,false";
        createEventsCsvFile("tagged", eventsHeader);
        createPersonsCsvFile("tagged", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "tagged");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_rowWithEvents_importsPerson() throws Exception {
        int eventId = 500;
        String testEventStr = eventId + ",Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100";
        String testPersonStr = "\nEve,81234567,eve@u.nus.edu,Blk 321,," + eventId + ",,false";
        createEventsCsvFile("withevents", eventsHeader + "\n" + testEventStr);
        createPersonsCsvFile("withevents", personsHeader + testPersonStr);

        ImportCommand command = createTestCommand("add", "withevents");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_distinctEventPayloads_generateDistinctComputedIdsAndLinkSuccessfully() throws Exception {
        int computedMeetingEventId = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100"),
                0).getEventId();
        int computedLunchEventId = new Event(
                new Title("Lunch"),
                Optional.empty(),
                new TimeRange("2026-05-06 1200", "2026-05-06 1300"),
                0).getEventId();

        String eventsData = eventsHeader + "\n"
                + "111,Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100\n"
                + "222,Lunch,,2026-05-06 1200,2026-05-06 1300";
        String personsData = personsHeader + "\n"
                + "Dual Event User,81230000,dual@u.nus.edu,Blk 1,,"
                + computedMeetingEventId + ";" + computedLunchEventId + ",,false";

        createEventsCsvFile("distinctComputedIds", eventsData);
        createPersonsCsvFile("distinctComputedIds", personsData);

        ImportCommand command = createTestCommand("overwrite", "distinctComputedIds");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertEquals(2, model.getAddressBook().getEventList().size());
        assertNotEquals(model.getAddressBook().getEventList().get(0).getEventId(),
                model.getAddressBook().getEventList().get(1).getEventId());

        Optional<Person> importedPerson = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals("Dual Event User"))
                .findFirst();
        assertTrue(importedPerson.isPresent());
        assertEquals(2, importedPerson.get().getEvents().size());
        assertTrue(importedPerson.get().getEvents().stream().anyMatch(event -> event.getTitle().fullTitle
                .equals("Meeting")));
        assertTrue(importedPerson.get().getEvents().stream().anyMatch(event -> event.getTitle().fullTitle
                .equals("Lunch")));
    }

    @Test
    public void execute_eventsCsvEventIdChanged_identityAndLinkingUnaffected() throws Exception {
        int computedEventId = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100"),
                0).getEventId();

        String personsData = personsHeader + "\n"
                + "Tamper Check,81230001,tamper@u.nus.edu,Blk 2,," + computedEventId + ",,false";

        createEventsCsvFile("eventIdIgnoredA", eventsHeader + "\n"
                + "111,Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100");
        createPersonsCsvFile("eventIdIgnoredA", personsData);

        Model firstModel = new ModelManager();
        ImportCommand firstImport = createTestCommand("overwrite", "eventIdIgnoredA");
        firstImport.execute(firstModel);

        createEventsCsvFile("eventIdIgnoredB", eventsHeader + "\n"
                + "999999,Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100");
        createPersonsCsvFile("eventIdIgnoredB", personsData);

        Model secondModel = new ModelManager();
        ImportCommand secondImport = createTestCommand("overwrite", "eventIdIgnoredB");
        secondImport.execute(secondModel);

        Event firstGlobalEvent = firstModel.getAddressBook().getEventList().get(0);
        Event secondGlobalEvent = secondModel.getAddressBook().getEventList().get(0);

        Optional<Person> firstPerson = firstModel.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals("Tamper Check"))
                .findFirst();
        Optional<Person> secondPerson = secondModel.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equals("Tamper Check"))
                .findFirst();

        assertTrue(firstPerson.isPresent());
        assertTrue(secondPerson.isPresent());
        assertEquals(1, firstPerson.get().getEvents().size());
        assertEquals(1, secondPerson.get().getEvents().size());
        assertSame(firstGlobalEvent, firstPerson.get().getEvents().get(0));
        assertSame(secondGlobalEvent, secondPerson.get().getEvents().get(0));
        assertEquals(computedEventId, firstGlobalEvent.getEventId());
        assertEquals(computedEventId, secondGlobalEvent.getEventId());
    }

    @Test
    public void execute_importResetsFilteredPersonAndEventViews() throws Exception {
        Person oldPerson = new PersonBuilder()
                .withName("Old Filtered Person")
                .withPhone("81111111")
                .withEmail("old@u.nus.edu")
                .withAddress("Blk Old")
                .withoutPhoto()
                .build();
        Event oldEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100"),
                0);
        oldPerson.addEvent(oldEvent);
        model.addPerson(oldPerson);
        model.addEvent(oldEvent);
        model.showEventsForPerson(oldPerson);

        int importedEventId = new Event(
                new Title("Imported Meeting"),
                Optional.of(new Description("Reset Test")),
                new TimeRange("2026-06-06 1000", "2026-06-06 1100"),
                0).getEventId();

        createEventsCsvFile("resetFilters", eventsHeader + "\n"
                + "123,Imported Meeting,Reset Test,2026-06-06 1000,2026-06-06 1100");
        createPersonsCsvFile("resetFilters", personsHeader + "\n"
                + "New Import,82222222,new@u.nus.edu,Blk New,," + importedEventId + ",,false");

        ImportCommand command = createTestCommand("overwrite", "resetFilters");
        command.execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("New Import", model.getFilteredPersonList().get(0).getName().fullName);
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_eventsCsvContainsBlankLine_skipsBlankEventEntryAndImportsPerson() throws Exception {
        createEventsCsvFile("blankEventLine", eventsHeader + "\n   ");
        createPersonsCsvFile("blankEventLine",
                personsHeader + "\nBlank Event User,81231234,blank@u.nus.edu,Blk 10,,,,false");

        ImportCommand command = createTestCommand("overwrite", "blankEventLine");
        CommandResult result = command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Blank Event User")
                .withPhone("81231234")
                .withEmail("blank@u.nus.edu")
                .withAddress("Blk 10")
                .withoutPhoto()
                .build();

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(model.hasPerson(expectedPerson));
        assertEquals(0, model.getAddressBook().getEventList().size());
    }

    @Test
    public void execute_eventsCsvMalformedRow_triggersCatchAndContinuesImportingPersons() throws Exception {
        // Missing required event columns causes IllegalArgumentException in event parsing.
        createEventsCsvFile("malformedEventRow", eventsHeader + "\n999,OnlyTitle");
        createPersonsCsvFile("malformedEventRow",
                personsHeader + "\nMalformed Event User,81239999,malformed@u.nus.edu,Blk 11,,,,false");

        ImportCommand command = createTestCommand("overwrite", "malformedEventRow");
        CommandResult result = command.execute(model);

        Person expectedPerson = new PersonBuilder()
                .withName("Malformed Event User")
                .withPhone("81239999")
                .withEmail("malformed@u.nus.edu")
                .withAddress("Blk 11")
                .withoutPhoto()
                .build();

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(model.hasPerson(expectedPerson));
        assertEquals(0, model.getAddressBook().getEventList().size());
    }


    @Test
    public void parseLineToEvent_nullLine_returnsEmptyOptional() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseLineToEvent(null).isEmpty());
    }

    @Test
    public void parseLineToEvent_blankLine_returnsEmptyOptional() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseLineToEvent("   ").isEmpty());
    }

    @Test
    public void createEventFromCsvRow_missingRequiredFields_throwsIllegalArgumentException() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,,desc,2026-05-06 1000,2026-05-06 1100"));
        assertEquals("Event has missing required fields", exception.getMessage());
    }

    @Test
    public void createEventFromCsvRow_missingStartOrEnd_throwsIllegalArgumentException() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        IllegalArgumentException missingStart = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,Meeting,desc,,2026-05-06 1100"));
        assertEquals("Event has missing required fields", missingStart.getMessage());

        IllegalArgumentException missingEnd = assertThrows(IllegalArgumentException.class, () ->
                importCommand.createEventFromCsvRow("101,Meeting,desc,2026-05-06 1000,"));
        assertEquals("Event has missing required fields", missingEnd.getMessage());
    }

    @Test
    public void createEventFromCsvRow_nonNumericEventId_isIgnored() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        Event event = importCommand.createEventFromCsvRow("abc,Meeting,desc,2026-05-06 1000,2026-05-06 1100");
        assertEquals("Meeting", event.getTitle().fullTitle);
        assertEquals("2026-05-06 1000", event.getStartTimeFormatted());
        assertEquals("2026-05-06 1100", event.getEndTimeFormatted());
    }

    @Test
    public void parseEventIds_nullOrBlank_returnsEmptyList() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");

        assertTrue(importCommand.parseEventIds(null, new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEventIds("   ", new HashMap<>()).isEmpty());
    }

    @Test
    public void parseEventIds_existingAndInvalidIds_handlesAllBranches() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        Event existingEvent = new Event(
                new Title("Meeting"),
                Optional.of(new Description("Kickoff")),
                new TimeRange("2026-05-06 1000", "2026-05-06 1100")
        );
        eventMap.put(100, existingEvent);

        List<Event> result = importCommand.parseEventIds("100;999;abc", eventMap);

        assertEquals(1, result.size());
        assertSame(existingEvent, result.get(0));
    }

    @Test
    public void equals() {
        ImportCommand importFirst = new ImportCommand("overwrite", "file1");
        ImportCommand importSecond = new ImportCommand("overwrite", "file1");
        ImportCommand importDifferentFile = new ImportCommand("overwrite", "file2");
        ImportCommand importDifferentType = new ImportCommand("add", "file1");
        ImportCommand importDifferentTypeAndFile = new ImportCommand("add", "file3");

        assertEquals(importFirst, importFirst);
        assertEquals(importFirst, importSecond);
        assertFalse(importFirst.equals(importDifferentType));
        assertFalse(importFirst.equals(importDifferentFile));
        assertFalse(importFirst.equals(importDifferentTypeAndFile));
        assertNotEquals(null, importFirst);
        assertNotEquals(importFirst, new ExportCommand("all", "test_file"));

    }

    @Test
    public void toString_returnsExpectedString() {
        ImportCommand importCommand = new ImportCommand("add", "myFile");
        assertEquals("ImportCommand{type=add, filename=myFile_persons.csv, myFile_events.csv}",
                importCommand.toString());
    }
}
