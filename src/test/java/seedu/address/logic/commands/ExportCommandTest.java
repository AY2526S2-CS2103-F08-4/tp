package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.commons.util.CsvUtil.splitCsvLine;
import static seedu.address.commons.util.CsvUtil.unwrapValue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ExportCommandTest {
    @TempDir
    public Path testFolder;

    private Model model;
    private Model expectedModel;
    private final String testFileName = "test_export";

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @AfterEach
    public void cleanUp() throws Exception {
        Path personsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_persons.csv");
        Path eventsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_events.csv");
        Files.deleteIfExists(personsPath);
        Files.deleteIfExists(eventsPath);
    }

    @Test
    public void execute_exportAll_success() {
        ExportCommand exportCommand = new ExportCommand("all", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName);

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);

        Path personsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_persons.csv");
        Path eventsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_events.csv");
        assertTrue(Files.exists(personsPath));
        assertTrue(Files.exists(eventsPath));
    }

    @Test
    public void execute_exportAll_headerHasExpectedColumnsInOrder() throws Exception {
        ExportCommand exportCommand = new ExportCommand("all", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName);

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);

        Path personsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_persons.csv");
        List<String> lines = Files.readAllLines(personsPath);
        String[] headerColumns = splitCsvLine(lines.get(0));

        assertEquals(8, headerColumns.length);
        assertEquals("Name", headerColumns[0]);
        assertEquals("Phone", headerColumns[1]);
        assertEquals("Email", headerColumns[2]);
        assertEquals("Address", headerColumns[3]);
        assertEquals("Tags", headerColumns[4]);
        assertEquals("EventIds", headerColumns[5]);
        assertEquals("Photo", headerColumns[6]);
        assertEquals("Pinned", headerColumns[7]);

        // Every exported person row should stay at the fixed 8-column schema.
        for (int i = 1; i < lines.size(); i++) {
            assertEquals(8, splitCsvLine(lines.get(i)).length);
        }
    }

    @Test
    public void execute_exportAll_eventsHeaderHasExpectedColumnsInOrder() throws Exception {
        ExportCommand exportCommand = new ExportCommand("all", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName);

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);

        Path eventsPath = model.getAddressBookFilePath().getParent().resolve(testFileName + "_events.csv");
        List<String> lines = Files.readAllLines(eventsPath);
        String[] headerColumns = splitCsvLine(lines.get(0));

        assertEquals(5, headerColumns.length);
        assertEquals("EventId", headerColumns[0]);
        assertEquals("Title", headerColumns[1]);
        assertEquals("Description", headerColumns[2]);
        assertEquals("Start", headerColumns[3]);
        assertEquals("End", headerColumns[4]);

        for (int i = 1; i < lines.size(); i++) {
            assertEquals(5, splitCsvLine(lines.get(i)).length);
        }
    }

    @Test
    public void execute_exportCurrent_success() {
        model.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));
        expectedModel.updateFilteredPersonList(p -> p.getName().fullName.contains("Alice"));

        ExportCommand exportCommand = new ExportCommand("current", testFileName);
        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName);

        assertCommandSuccess(exportCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_exportPinnedPerson_writesPinnedColumn() throws Exception {
        Person pinnedPerson = new PersonBuilder()
                .withName("Pinned Person")
                .withPhone("91234567")
                .withEmail("pinned@example.com")
                .withAddress("Blk 123")
                .withTags("friends")
                .withoutPhoto()
                .build();

        Model modelWithPinnedPerson = new ModelManager();
        Model expectedModelWithPinnedPerson = new ModelManager();
        modelWithPinnedPerson.addPerson(pinnedPerson);
        expectedModelWithPinnedPerson.addPerson(pinnedPerson);
        modelWithPinnedPerson.pinPerson(pinnedPerson);
        expectedModelWithPinnedPerson.pinPerson(pinnedPerson);

        Path personsFilePath = testFolder.resolve(testFileName + "_persons.csv");
        Path eventsFilePath = testFolder.resolve(testFileName + "_events.csv");
        ExportCommand exportCommand = new ExportCommand("all", testFileName) {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return eventsFilePath;
            }
        };

        String expectedMessage = String.format(ExportCommand.MESSAGE_SUCCESS, testFileName);
        assertCommandSuccess(exportCommand, modelWithPinnedPerson, expectedMessage, expectedModelWithPinnedPerson);

        List<String> lines = Files.readAllLines(personsFilePath);
        String[] headerColumns = splitCsvLine(lines.get(0));
        String[] rowColumns = splitCsvLine(lines.get(1));

        assertEquals(8, headerColumns.length);
        assertEquals("Pinned", headerColumns[7]);
        assertEquals(8, rowColumns.length);
        assertEquals("Pinned Person", rowColumns[0]);
        assertEquals("true", unwrapValue(rowColumns[7]));
    }

    @Test
    public void execute_exportUnpinnedPerson_writesFalseInPinnedColumn() throws Exception {
        Person unpinnedPerson = new PersonBuilder()
                .withName("Unpinned Person")
                .withPhone("92345678")
                .withEmail("unpinned@example.com")
                .withAddress("Blk 456")
                .withTags("colleague")
                .withoutPhoto()
                .build();

        Model modelWithUnpinnedPerson = new ModelManager();
        modelWithUnpinnedPerson.addPerson(unpinnedPerson);

        Path personsFilePath = testFolder.resolve("exportUnpinned_persons.csv");
        Path eventsFilePath = testFolder.resolve("exportUnpinned_events.csv");
        ExportCommand exportCommand = new ExportCommand("all", "exportUnpinned") {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return eventsFilePath;
            }
        };

        exportCommand.execute(modelWithUnpinnedPerson);

        List<String> lines = Files.readAllLines(personsFilePath);
        String[] rowColumns = splitCsvLine(lines.get(1));
        assertEquals("Unpinned Person", rowColumns[0]);
        assertEquals("false", unwrapValue(rowColumns[7]));
    }

    @Test
    public void execute_exportCurrent_onlyFilteredPinnedPersonWritten() throws Exception {
        Person pinnedCurrent = new PersonBuilder()
                .withName("Current Pinned")
                .withPhone("93456789")
                .withEmail("current@example.com")
                .withAddress("Blk 100")
                .withoutPhoto()
                .build();
        Person unfilteredPerson = new PersonBuilder()
                .withName("Other Person")
                .withPhone("94567890")
                .withEmail("other@example.com")
                .withAddress("Blk 200")
                .withoutPhoto()
                .build();

        Model modelWithMixedPersons = new ModelManager();
        modelWithMixedPersons.addPerson(pinnedCurrent);
        modelWithMixedPersons.addPerson(unfilteredPerson);
        modelWithMixedPersons.pinPerson(pinnedCurrent);
        modelWithMixedPersons.updateFilteredPersonList(p -> p.getName().fullName.equals("Current Pinned"));

        Path personsFilePath = testFolder.resolve("exportCurrentPinned_persons.csv");
        Path eventsFilePath = testFolder.resolve("exportCurrentPinned_events.csv");
        ExportCommand exportCommand = new ExportCommand("current", "exportCurrentPinned") {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return eventsFilePath;
            }
        };

        exportCommand.execute(modelWithMixedPersons);

        List<String> lines = Files.readAllLines(personsFilePath);
        assertEquals(2, lines.size()); // header + only one filtered row

        String[] rowColumns = splitCsvLine(lines.get(1));
        assertEquals("Current Pinned", rowColumns[0]);
        assertEquals("true", unwrapValue(rowColumns[7]));
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
                .withoutPhoto()
                .build();

        Model model = new ModelManager();
        model.addPerson(originalPerson);

        String filename = "exportImport";
        Path personsFilePath = testFolder.resolve(filename + "_persons.csv");
        Path eventsFilePath = testFolder.resolve(filename + "_events.csv");

        ExportCommand exportCommand = new ExportCommand("all", filename) {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return eventsFilePath;
            }
        };
        exportCommand.execute(model);

        model.setAddressBook(new AddressBook());
        assertFalse(model.hasPerson(originalPerson));

        ImportCommand importCommand = new ImportCommand("add", filename) {
            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsFilePath;
            }
        };
        importCommand.execute(model);

        assertTrue(model.hasPerson(originalPerson), "Test person was not reconstructed correctly.");

        Optional<Person> importedPersonOpt = model.getAddressBook().getPersonList().stream()
                .filter(originalPerson::isSamePerson)
                .findFirst();
        assertTrue(importedPersonOpt.isPresent(), "Imported person not found in address book.");
        Person importedPerson = importedPersonOpt.get();
        assertEquals(originalPerson.getAddress(), importedPerson.getAddress(),
                "Address with commas failed round-trip integrity!");
    }

    @Test
    public void execute_exportImportMultipleEvents_roundTripIntegrityMaintained() throws Exception {
        Person originalPerson = new PersonBuilder()
                .withName("Eve Tan")
                .withPhone("81234567")
                .withEmail("eve@u.nus.edu")
                .withAddress("Blk 321")
                .withEvents("Meeting,Kickoff,2026-05-06 1000,2026-05-06 1100",
                        "Lunch,2026-05-06 1200,2026-05-06 1300")
                .withoutPhoto()
                .build();

        Model model = new ModelManager();
        model.addPerson(originalPerson);

        String filename = "exportImportMultipleEvents";
        Path personsFilePath = testFolder.resolve(filename + "_persons.csv");
        Path eventsFilePath = testFolder.resolve(filename + "_events.csv");

        ExportCommand exportCommand = new ExportCommand("all", filename) {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return eventsFilePath;
            }
        };
        exportCommand.execute(model);

        model.setAddressBook(new AddressBook());

        ImportCommand importCommand = new ImportCommand("add", filename) {
            @Override
            protected Path getPersonsImportPath(Model model) {
                return personsFilePath;
            }

            @Override
            protected Path getEventsImportPath(Model model) {
                return eventsFilePath;
            }
        };
        importCommand.execute(model);

        Optional<Person> importedPersonOpt = model.getAddressBook().getPersonList().stream()
                .filter(originalPerson::isSamePerson)
                .findFirst();
        assertTrue(importedPersonOpt.isPresent(), "Imported person not found in address book.");
        Person importedPerson = importedPersonOpt.get();
        assertEquals(2, importedPerson.getEvents().size());

        Event firstEvent = importedPerson.getEvents().stream()
                .filter(e -> e.getTitle().fullTitle.equals("Meeting"))
                .findFirst()
                .orElseThrow();
        Event secondEvent = importedPerson.getEvents().stream()
                .filter(e -> e.getTitle().fullTitle.equals("Lunch"))
                .findFirst()
                .orElseThrow();

        assertEquals("Meeting", firstEvent.getTitle().fullTitle);
        assertEquals("Kickoff", firstEvent.getDescription().orElseThrow().fullDescription);
        assertEquals("2026-05-06 1000", firstEvent.getStartTimeFormatted());
        assertEquals("2026-05-06 1100", firstEvent.getEndTimeFormatted());

        assertEquals("Lunch", secondEvent.getTitle().fullTitle);
        assertTrue(secondEvent.getDescription().isEmpty());
        assertEquals("2026-05-06 1200", secondEvent.getStartTimeFormatted());
        assertEquals("2026-05-06 1300", secondEvent.getEndTimeFormatted());
    }

    @Test
    public void execute_exportPathIsDirectory_throwsCommandException() throws Exception {
        Path exportDirectory = testFolder.resolve("exportDir");
        Files.createDirectory(exportDirectory);

        ExportCommand exportCommand = new ExportCommand("all", "directoryExport") {
            @Override
            protected Path getPersonsExportPath(Model model) {
                return exportDirectory;
            }

            @Override
            protected Path getEventsExportPath(Model model) {
                return exportDirectory;
            }
        };

        assertThrows(CommandException.class, () -> exportCommand.execute(model));
    }

    @Test
    public void equals() {
        ExportCommand exportFirst = new ExportCommand("all", "file1");
        ExportCommand exportSecond = new ExportCommand("all", "file1");
        ExportCommand exportDifferentType = new ExportCommand("current", "file1");
        ExportCommand exportDifferentFile = new ExportCommand("all", "file2");
        ExportCommand exportDifferentTypeAndFile = new ExportCommand("current", "file3");

        assertEquals(exportFirst, exportFirst);
        assertEquals(exportFirst, exportSecond);
        assertFalse(exportFirst.equals(exportDifferentType));
        assertFalse(exportFirst.equals(exportDifferentFile));
        assertFalse(exportFirst.equals(exportDifferentTypeAndFile));
        assertNotEquals(null, exportFirst);
        assertNotEquals(exportFirst, new ImportCommand("add", "test_file"));
    }

    @Test
    public void toString_returnsExpectedString() {
        ExportCommand exportCommand = new ExportCommand("current", "myFile");
        assertEquals("ExportCommand{type=current, filename=myFile_persons.csv, myFile_events.csv}",
                exportCommand.toString());
    }
}
