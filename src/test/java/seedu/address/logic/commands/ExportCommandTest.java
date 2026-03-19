package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.commons.util.CsvUtil.splitCsvLine;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.ImportCommand.FILENAME_SUFFIX;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExportCommandTest {
    private Model model;
    private Model expectedModel;
    private final String testFileName = "test_export";

    @TempDir
    public Path testFolder;

    private void createCsvFile(String filename, String content) throws Exception {
        Path filePath = testFolder.resolve(filename + FILENAME_SUFFIX);
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + ".csv");
        Files.deleteIfExists(path);
    }

    @Test
    public void execute_exportAll_success() {
        ExportCommand exportCommand = new ExportCommand("all", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName + ".csv");

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);

        Path path = model.getAddressBookFilePath().getParent().resolve(testFileName + ".csv");
        assertTrue(Files.exists(path));
    }

    @Test
    public void execute_exportCurrent_success() {
        model.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));
        expectedModel.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));

        ExportCommand exportCommand = new ExportCommand("current", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName + ".csv");

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void splitCsvLine_complexAddress_integrityMaintained() {
        String line = "David,91234567,,\"Blk 123, Jurong West, #01-01\",friends,";
        String[] columns = splitCsvLine(line);

        // Check the integrity of the total number of columns
        assertEquals(6, columns.length);

        // Check the integrity of the address column
        assertEquals("\"Blk 123, Jurong West, #01-01\"", columns[3]);

        // Check the integrity of the empty tag column
        assertEquals("", columns[2]);
    }

    @Test
    public void execute_exportImport_dataIntegrityMaintained() throws Exception {
        Person originalPerson = new PersonBuilder()
                .withName("David Ng")
                .withPhone("91234567")
                .withEmail("david@u.nus.edu")
                .withAddress("Blk 123, Clementi Ave 2, #10-10")
                .withTags("friends", "nus")
                .build();

        Model model = new ModelManager();
        model.addPerson(originalPerson);

        String filename = "exportImport";
        Path sharedFilePath = testFolder.resolve(filename + FILENAME_SUFFIX);

        ExportCommand exportCommand = new ExportCommand("all", filename) {
            @Override
            protected Path getExportPath(Model model) {
                return sharedFilePath;
            }
        };
        exportCommand.execute(model);

        model.setAddressBook(new AddressBook());
        assertFalse(model.hasPerson(originalPerson));

        ImportCommand importCommand = new ImportCommand("add", filename) {
            @Override
            protected Path getImportPath(Model model) {
                return sharedFilePath;
            }
        };
        importCommand.execute(model);

        assertTrue(model.hasPerson(originalPerson), "Test person was not reconstructed correctly.");

        Person importedPerson = model.getFilteredPersonList().get(0);
        assertEquals(originalPerson.getAddress(), importedPerson.getAddress(),
                "Address with commas failed round-trip integrity!");
    }

    @Test
    public void equals() {
        ExportCommand exportFirst = new ExportCommand("all", "file1");
        ExportCommand exportSecond = new ExportCommand("all", "file1");

        assertTrue(exportFirst.equals(exportFirst));

        assertTrue(exportFirst.equals(exportSecond));

        assertTrue(!exportFirst.equals(null));
    }
}