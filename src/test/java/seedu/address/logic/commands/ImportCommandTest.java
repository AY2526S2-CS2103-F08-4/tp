package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.ImportCommand.FILENAME_SUFFIX;
import static seedu.address.logic.commands.ImportCommand.MESSAGE_ERROR_READING_FILE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ImportCommandTest {
    private Model model;
    private Model expectedModel;
    private final String testFileName = "test_import";
    private final String header = "Name,Phone,Email,Address,Tags,Events";

    @TempDir
    public Path testFolder;

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

        ImportCommand command = createTestCommand("overwrite","valid");
        CommandResult result = command.execute(model);

        assertFalse(model.hasPerson(expectedTest1));
        assertTrue(model.hasPerson(expectedTest2));
        assertEquals(1, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void execute_addWithDuplicates_skipsExistingData() throws Exception {
        Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(alice);

        createCsvFile("merge", header + "\nAlice Pauline,12345678,alice@u.nus.edu,Blk 123,,\nBob,88662211,bob@u.nus.edu,Blk 123,,");

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
        createCsvFile("invalidRow", header + "\nValid,91234567,valid@u.nus.edu,Blk 123,,\nInvalid,abcd,invalid@u.nus.edu,Blk 123,,");

        ImportCommand command = createTestCommand("add", "invalidRow");
        CommandResult result = command.execute(model);

        assertTrue(result.getFeedbackToUser().contains("1 row(s) added"));
        assertTrue(result.getFeedbackToUser().contains("1 row(s) skipped"));
    }

}
