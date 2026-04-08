package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.ImportCommand.FILENAME_SUFFIX;
import static seedu.address.logic.commands.ImportCommand.MESSAGE_ERROR_READING_FILE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;
    private final String header = "Name,Phone,Email,Address,Tags,Events,Photo";

    private void createCsvFile(String fileName, String content) throws Exception {
        Path filePath = testFolder.resolve(fileName + FILENAME_SUFFIX);
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    public ImportCommand createTestCommand(String importType, String filename) {
        return new ImportCommand(importType, filename) {
            @Override
            protected Path getImportPath(Model model) {
                return testFolder.resolve(filename + FILENAME_SUFFIX);
            }
        };
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path path = model.getAddressBookFilePath().getParent().resolve("test_import" + FILENAME_SUFFIX);
        Files.deleteIfExists(path);
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
                .withPhoto("this_is_another.jpg")
                .build();

        model.addPerson(expectedTest1);

        createCsvFile("valid", header + "\nDavid,91234567,david@u.nus.edu,Blk 456,,,");

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

        String testString = """
                \nAlice Pauline,12345678,alice@u.nus.edu,Blk 123,,,
                \nBob,88662211,bob@u.nus.edu,Blk 123,,,
                """;

        createCsvFile("merge", header + testString);

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

        createCsvFile("valid", header + "\nAlice,12345678,,,,,");

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

        createCsvFile("photo", header + "\nAlice,12345678,alice@u.nus.edu,Blk 123,,,hello_world.jpg");

        ImportCommand command = createTestCommand("overwrite", "photo");
        command.execute(model);

        assertTrue(model.hasPerson(expectedTest1));
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(command.execute(model).getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_fileNotFound_throwsCommandException() {
        ImportCommand command = createTestCommand("add", "invalid_file");

        String expectedMessage = String.format(MESSAGE_ERROR_READING_FILE, "invalid_file" + FILENAME_SUFFIX);
        assertCommandFailure(command, model, expectedMessage);
    }

    @Test
    public void execute_csvHeaderOnly_returnsEmptyMessage() throws Exception {
        createCsvFile("empty", header);

        ImportCommand command = createTestCommand("add", "empty");
        CommandResult result = command.execute(model);

        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty" + FILENAME_SUFFIX),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyCsvFile_throwsCommandException() throws Exception {
        Path emptyFilePath = testFolder.resolve("empty" + FILENAME_SUFFIX);
        Files.write(emptyFilePath, new byte[0]);

        ImportCommand command = createTestCommand("add", "empty");

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));
        assertEquals(String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty" + FILENAME_SUFFIX),
                exception.getMessage());
    }

    @Test
    public void execute_rowsWithAndWithoutPhoto_importsBothPersons() throws Exception {
        String testString = "\nPhoto User,81234567,photo@u.nus.edu,Blk 123,,,avatar.png"
                + "\nNo Photo,82345678,nophoto@u.nus.edu,Blk 456,,,";
        createCsvFile("mixedPhoto", header + testString);

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
        String testString = "\nValid,91234567,valid@u.nus.edu,Blk 123,,,\nInvalid,abcd,invalid@u.nus.edu,Blk 123,,,";

        createCsvFile("invalidRow", header + testString);

        ImportCommand command = createTestCommand("add", "invalidRow");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(result.getFeedbackToUser().contains("1 row(s) skipped"));
    }

    @Test
    public void execute_totalMalformedCsv_abortAndReports() throws Exception {
        String testString = "\nValid,91234567,valid@u.nus.edu,Blk 123,,,aaa"
                + "\nInvalid,abcd,invalid@u.nus.edu,Blk 123,,,";

        createCsvFile("malformed", header + testString);

        ImportCommand addCommand = createTestCommand("add", "malformed");
        CommandException addException = assertThrows(CommandException.class, () -> addCommand.execute(model));
        assertEquals(String.format(ImportCommand.MESSAGE_MALFORM_CSV, "malformed" + FILENAME_SUFFIX),
                addException.getMessage());

        ImportCommand overwriteCommand = createTestCommand("overwrite", "malformed");
        CommandException overwriteException = assertThrows(
                CommandException.class, () -> overwriteCommand.execute(model));
        assertEquals(String.format(ImportCommand.MESSAGE_MALFORM_CSV, "malformed" + FILENAME_SUFFIX),
                overwriteException.getMessage());
    }

    @Test
    public void execute_fileDoesNotExist_throwsCommandException() {
        ImportCommand importCommand = new ImportCommand("add", "nonExistentFile");
        assertCommandFailure(importCommand, model,
                String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, "nonExistentFile.csv"));
    }

    @Test
    public void execute_headerOnlyFile_returnsEmptyFileMessage() throws Exception {
        Path filePath = testFolder.resolve("empty.csv");
        Files.writeString(filePath, "Name,Phone,Email,Address,Tags,Events,Photo");

        ImportCommand importCommand = new ImportCommand("add", "empty") {
            @Override
            protected Path getImportPath(Model model) {
                return filePath;
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_EMPTY_FILE, "empty.csv");
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
            protected Path getImportPath(Model model) {
                return dirPath;
            }
        };

        String expectedMessage = String.format(ImportCommand.MESSAGE_ERROR_READING_FILE, dirname + ".csv");

        assertThrows(CommandException.class, () -> importCommand.execute(model), expectedMessage);
    }

    @Test
    public void processImportedLines_invalidColumnCount_skipsRow() throws Exception {
        Path filePath = testFolder.resolve("malformed.csv");
        List<String> lines = List.of(
                "Name,Phone,Email,Address,Tags,Events,Photo",
                "John Doe,91234567",
                "Yohan,67676868,,,,,"
        );
        Files.write(filePath, lines);

        ImportCommand importCommand = new ImportCommand("add", "malformed") {
            @Override
            protected Path getImportPath(Model model) {
                return filePath;
            }
        };

        CommandResult result = importCommand.execute(model);

        String expectedFeedback = String.format(ImportCommand.MESSAGE_SUCCESS_ROWS_ADDED_SKIPPED,
                "malformed.csv", 1, 1);
        assertEquals(expectedFeedback, result.getFeedbackToUser());
    }

    @Test
    public void parseEvents_validEvents_success() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|101;"
                + "Lunch||2026-05-06 1200|2026-05-06 1300|1|102";
        List<Event> result = importCommand.parseEvents(eventString, new HashMap<>());

        assertEquals(2, result.size());
        assertEquals("Meeting", result.get(0).getTitle().fullTitle);
        assertEquals("Kickoff", result.get(0).getDescription().orElseThrow().fullDescription);
        assertEquals("Lunch", result.get(1).getTitle().fullTitle);
        assertTrue(result.get(1).getDescription().isEmpty());
    }

    @Test
    public void parseEvents_nullOrEmpty_returnsEmptyList() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseEvents(null, new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEvents("", new HashMap<>()).isEmpty());
        assertTrue(importCommand.parseEvents("   ", new HashMap<>()).isEmpty());
    }

    @Test
    public void parseEvents_malformedEntries_skipsInvalidEvents() {
        String malformedString = "InvalidEvent|OnlyOnePipe; |Desc|Start|End; "
                + "ValidEvent|Note|2026-05-06 1000|2026-05-06 1100|3|888";
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        List<Event> result = importCommand.parseEvents(malformedString, new HashMap<>());

        assertEquals(1, result.size());
        assertEquals("ValidEvent", result.get(0).getTitle().fullTitle);
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
        createCsvFile("tagged", header + "\nCharlie,87654321,charlie@u.nus.edu,Blk 789,friends;CS2103,,");

        ImportCommand command = createTestCommand("add", "tagged");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void execute_rowWithEvents_importsPerson() throws Exception {
        createCsvFile("withevents", header
                + "\nEve,81234567,eve@u.nus.edu,Blk 321,,Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|1|500,");

        ImportCommand command = createTestCommand("add", "withevents");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
    }

    @Test
    public void parseEvents_sameEventId_reusesSharedEventInstance() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        List<Event> firstParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> secondParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);

        assertEquals(1, firstParsed.size());
        assertEquals(1, secondParsed.size());
        assertSame(firstParsed.get(0), secondParsed.get(0));
    }

    @Test
    public void parseEvents_sameEventIdDifferentTitleOrTime_skipsConflictingEvent() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        List<Event> firstParsed = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> conflictingTitle = importCommand.parseEvents(
                "Consultation|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900", eventMap);
        List<Event> conflictingTime = importCommand.parseEvents(
                "Meeting|Kickoff|2026-05-06 1200|2026-05-06 1300|2|900", eventMap);

        assertEquals(1, firstParsed.size());
        assertTrue(conflictingTitle.isEmpty());
        assertTrue(conflictingTime.isEmpty());
        assertEquals("Meeting", eventMap.get(900).getTitle().fullTitle);
    }

    @Test
    public void parseEvents_malformedAndIncompleteEntries_areSkipped() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String malformedString = "|Desc|2026-05-06 1000|2026-05-06 1100|1|100;"
                + "Meeting|Desc||2026-05-06 1100|1|101;"
                + "Meeting|Desc|2026-05-06 1000||1|102;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|1|";

        List<Event> result = importCommand.parseEvents(malformedString, eventMap);

        assertTrue(result.isEmpty());
        assertTrue(eventMap.isEmpty());
    }

    @Test
    public void parseEvents_invalidFieldValues_areSkipped() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String invalidString = "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|-1|104;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|abc|105";

        List<Event> result = importCommand.parseEvents(invalidString, eventMap);

        assertTrue(result.isEmpty());
        assertTrue(eventMap.isEmpty());
    }

    @Test
    public void parseEvents_requiredFieldsLinkedCountAndException_skipsInvalidKeepsValid() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "|Desc|2026-05-06 1000|2026-05-06 1100|1|100;"
                + "Meeting|Desc||2026-05-06 1100|1|101;"
                + "Meeting|Desc|2026-05-06 1000||1|102;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|1|;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|0|103;"
                + "Meeting|Desc|2026-05-06 1000|2026-05-06 1100|-1|104;"
                + "Broken|Desc|not-a-time|2026-05-06 1100|1|105;"
                + "Valid|Desc|2026-05-06 1200|2026-05-06 1300|1|106";

        List<Event> result = importCommand.parseEvents(eventString, eventMap);

        assertEquals(1, result.size());
        assertEquals("Valid", result.get(0).getTitle().fullTitle);
        assertEquals(1, eventMap.size());
    }

    @Test
    public void parseEvents_multipleEventsWithoutConflict_importsAllEvents() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Lunch||2026-05-06 1200|2026-05-06 1300|1|901;"
                + "Tutorial|CS2103|2026-05-06 1400|2026-05-06 1500|1|902";

        List<Event> parsedEvents = importCommand.parseEvents(eventString, eventMap);

        assertEquals(3, parsedEvents.size());
        assertEquals("Meeting", parsedEvents.get(0).getTitle().fullTitle);
        assertEquals("Lunch", parsedEvents.get(1).getTitle().fullTitle);
        assertEquals("Tutorial", parsedEvents.get(2).getTitle().fullTitle);
        assertEquals(3, eventMap.size());
    }

    @Test
    public void parseEvents_multipleEventsWithConflict_skipsOnlyConflictingEvent() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        HashMap<Integer, Event> eventMap = new HashMap<>();

        String eventString = "Meeting|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Consultation|Kickoff|2026-05-06 1000|2026-05-06 1100|2|900;"
                + "Tutorial|CS2103|2026-05-06 1400|2026-05-06 1500|1|901";

        List<Event> parsedEvents = importCommand.parseEvents(eventString, eventMap);

        assertEquals(2, parsedEvents.size());
        assertEquals("Meeting", parsedEvents.get(0).getTitle().fullTitle);
        assertEquals("Tutorial", parsedEvents.get(1).getTitle().fullTitle);
        assertEquals(2, eventMap.size());
        assertEquals("Meeting", eventMap.get(900).getTitle().fullTitle);
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
        assertEquals("Importing list from: myFile.csv", importCommand.toString());
    }
}
