package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.ImportCommand.FILENAME_SUFFIX;
import static seedu.address.logic.commands.ImportCommand.MESSAGE_ERROR_READING_FILE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;
    private Model expectedModel;
    private final String testFileName = "test_import";
    private final String header = "Name,Phone,Email,Address,Tags,Events";

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
        expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + FILENAME_SUFFIX);
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
                .build();

        Person expectedTest2 = new PersonBuilder()
                .withName("David")
                .withPhone("91234567")
                .withEmail("david@u.nus.edu")
                .withAddress("Blk 456")
                .withTags()
                .withEvents()
                .build();

        model.addPerson(expectedTest1);

        createCsvFile("valid", header + "\nDavid,91234567,david@u.nus.edu,Blk 456,,");

        ImportCommand command = createTestCommand("overwrite", "valid");
        CommandResult result = command.execute(model);

        assertFalse(model.hasPerson(expectedTest1));
        assertTrue(model.hasPerson(expectedTest2));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addWithDuplicates_skipsExistingData() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(alice);

        String testString = "\nAlice Pauline,12345678,alice@u.nus.edu,Blk 123,,\nBob,88662211,bob@u.nus.edu,Blk 123,,";

        createCsvFile("merge", header + testString);

        ImportCommand command = createTestCommand("add", "merge");
        CommandResult result = command.execute(model);

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
    public void execute_invalidDataRow_skipsAndReports() throws Exception {
        String testString = "\nValid,91234567,valid@u.nus.edu,Blk 123,,\nInvalid,abcd,invalid@u.nus.edu,Blk 123,,";

        createCsvFile("invalidRow", header + testString);

        ImportCommand command = createTestCommand("add", "invalidRow");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(result.getFeedbackToUser().contains("1 row(s) skipped"));
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
        Files.writeString(filePath, "Name,Phone,Email,Address,Tags,Events");

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
        dirPath.toFile().mkdir();

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
                "Name,Phone,Email,Address,Tags,Events",
                "John Doe,91234567"
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
                "malformed.csv", 0, 1);
        assertEquals(expectedFeedback, result.getFeedbackToUser());
    }

    @Test
    public void parseEvents_validEvents_success() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        String eventString = "Meeting|2026-05-06 10:00|2026-05-06 11:00; Lunch|12:00|13:00";
        List<Event> result = importCommand.parseEvents(eventString);

        assertEquals(2, result.size());
        assertEquals("Meeting", result.get(0).getDescription());
        assertEquals("Lunch", result.get(1).getDescription());
    }

    @Test
    public void parseEvents_nullOrEmpty_returnsEmptyList() {
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        assertTrue(importCommand.parseEvents(null).isEmpty());
        assertTrue(importCommand.parseEvents("").isEmpty());
        assertTrue(importCommand.parseEvents("   ").isEmpty());
    }

    @Test
    public void parseEvents_malformedEntries_skipsInvalidEvents() {
        String malformedString = "InvalidEvent|OnlyOnePipe; |Start|End; ValidEvent|Start|End";
        ImportCommand importCommand = new ImportCommand("add", "testFile");
        List<Event> result = importCommand.parseEvents(malformedString);

        assertEquals(1, result.size());
        assertEquals("ValidEvent", result.get(0).getDescription());
    }

    @Test
    public void equals() {
        ImportCommand importFirst = new ImportCommand("overwrite", "file1");
        ImportCommand importSecond = new ImportCommand("overwrite", "file1");

        assertTrue(importFirst.equals(importFirst));

        assertTrue(importFirst.equals(importSecond));

        assertTrue(!importFirst.equals(null));
    }
}
