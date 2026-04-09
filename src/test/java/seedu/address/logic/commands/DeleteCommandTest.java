package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validName_success() {
        Person personToDelete = model.findPersons(createNameOnlyInfo(new Name("Carl Kurz"))).get(0);
        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(personToDelete.getName()));

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedPersonToDelete = expectedModel.findPersons(createNameOnlyInfo(personToDelete.getName())).get(0);
        expectedModel.deletePerson(expectedPersonToDelete);
        expectedModel.showNoEvents();

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidName_throwsCommandException() {
        Name invalidName = new Name("John Doe");
        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(invalidName));

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_multipleNameMatches_throwsCommandException() {
        Person duplicatePerson = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(duplicatePerson);
        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(new Name("Alice Pauline")));

        CommandException thrown = assertThrows(CommandException.class, () -> {
            deleteCommand.execute(model);
        });

        assertEquals(Messages.MESSAGE_MULTIPLE_MATCH, thrown.getMessage());
    }

    @Test
    public void execute_multipleNameMatches_updatesFilteredListToMatchingPersons() {
        Person duplicatePerson = new PersonBuilder().withName("Alice Pauline").withPhone("12345678").build();
        model.addPerson(duplicatePerson);
        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(new Name("Alice Pauline")));

        assertThrows(CommandException.class, () -> deleteCommand.execute(model));
        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream()
                .allMatch(person -> person.getName().equalsIgnoreCase(new Name("Alice Pauline"))));
    }

    @Test
    public void execute_sameNameDifferentPhone_success() {
        Person firstMatch = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("90001111")
                .build();
        Person secondMatch = new PersonBuilder()
                .withName("Alex Tan")
                .withPhone("90002222")
                .build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        PersonInformation info = new PersonInformation(new Name("Alex Tan"), new Phone("90001111"), null, null, null);
        DeleteCommand deleteCommand = new DeleteCommand(info);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(firstMatch));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedPersonToDelete = expectedModel.findPersons(info).get(0);
        expectedModel.deletePerson(expectedPersonToDelete);
        expectedModel.showNoEvents();

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameDifferentPhone_throwsCommandException() {
        Person firstMatch = new PersonBuilder()
                .withName("David Ng")
                .withPhone("90001111")
                .build();
        Person secondMatch = new PersonBuilder()
                .withName("David Ng")
                .withPhone("90002222")
                .build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        // 0 result test:
        PersonInformation info1 = new PersonInformation(new Name("David Ng"),
                new Phone("98889999"), null, null, null);
        DeleteCommand deleteCommand1 = new DeleteCommand(info1);
        assertCommandFailure(deleteCommand1, model, Messages.MESSAGE_NO_MATCH);

        // 2 result test:
        PersonInformation info2 = new PersonInformation(new Name("David Ng"),
                null, null, null, null);
        DeleteCommand deleteCommand2 = new DeleteCommand(info2);
        CommandException thrown = assertThrows(CommandException.class, () -> deleteCommand2.execute(model));
        assertEquals(Messages.MESSAGE_MULTIPLE_MATCH, thrown.getMessage());
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);

        // 1 result test:
        PersonInformation info3 = new PersonInformation(new Name("David Ng"), new Phone("90002222"),
                null, null, null);
        DeleteCommand deleteCommand3 = new DeleteCommand(info3);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(secondMatch));
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedPersonToDelete = expectedModel.findPersons(info3).get(0);
        expectedModel.deletePerson(expectedPersonToDelete);
        expectedModel.showNoEvents();
        assertCommandSuccess(deleteCommand3, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validName_clearsFilteredEventList() throws Exception {
        Person personToDelete = model.findPersons(createNameOnlyInfo(new Name("Alice Pauline"))).get(0);
        model.showEventsForPerson(personToDelete);
        assertFalse(model.getFilteredEventList().isEmpty());

        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(personToDelete.getName()));
        deleteCommand.execute(model);

        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void equals() {
        Person personOne = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personTwo = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        DeleteCommand deleteFirstCommand = new DeleteCommand(createNameOnlyInfo(personOne.getName()));
        DeleteCommand deleteSecondCommand = new DeleteCommand(createNameOnlyInfo(personTwo.getName()));

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(createNameOnlyInfo(personOne.getName()));
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Name targetName = new Name("Alice Pauline");
        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(targetName));
        String expected = DeleteCommand.class.getCanonicalName() + "{targetName=" + targetName + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteCommand(null));
    }

    private static PersonInformation createNameOnlyInfo(Name name) {
        return new PersonInformation(name, null, null, null, null);
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_deletesPersonAndPhoto_success(@TempDir Path tempDir) throws Exception {
        // Set up the temp directories for simulation
        String tempDirPath = PhotoStorageUtil.formatPath(tempDir);

        Path photoFile = tempDir.resolve("delete_me.jpg");
        Files.createFile(photoFile);
        String photoPath = PhotoStorageUtil.formatPath(photoFile);

        Person personToDelete = new PersonBuilder().withName("John Doe").withPhoto(photoPath).build();
        model.addPerson(personToDelete);

        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(personToDelete.getName()), tempDirPath);
        deleteCommand.execute(model);

        assertFalse(model.hasPerson(personToDelete));
        assertFalse(Files.exists(photoFile));

    }

    @Test
    public void execute_deletesPersonAndPhoto_throwsCommandException(@TempDir Path tempDir) throws Exception {
        // Set up the temp directories for simulation
        String tempDirPath = PhotoStorageUtil.formatPath(tempDir);

        // Create a structure where /to_be_deleted.jpg/dummy.txt
        // Prevent to_be_deleted.jpg from getting deleted
        Path dummyDir = tempDir.resolve("to_be_deleted.jpg");
        Files.createDirectory(dummyDir);

        Files.createFile(dummyDir.resolve("dummy.txt"));
        String photoPath = PhotoStorageUtil.formatPath(dummyDir);

        Person personToDelete = new PersonBuilder().withName("John Doe").withPhoto(photoPath).build();
        model.addPerson(personToDelete);

        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(personToDelete.getName()), tempDirPath);
        assertThrows(CommandException.class, () -> deleteCommand.execute(model));
    }

    @Test
    public void execute_sharedPhoto_deletesPersonButKeepsPhotoFile(@TempDir Path tempDir) throws Exception {
        String tempDirPath = PhotoStorageUtil.formatPath(tempDir);


        Path sharedPhotoFile = tempDir.resolve("shared.jpg");
        Files.createFile(sharedPhotoFile);
        String sharedPhotoPath = PhotoStorageUtil.formatPath(sharedPhotoFile);

        Person firstPerson = new PersonBuilder().withName("Shared One").withPhone("81112222")
                .withPhoto(sharedPhotoPath).build();
        Person secondPerson = new PersonBuilder().withName("Shared Two").withPhone("83334444")
                .withPhoto(sharedPhotoPath).build();
        model.addPerson(firstPerson);
        model.addPerson(secondPerson);

        DeleteCommand deleteCommand = new DeleteCommand(createNameOnlyInfo(firstPerson.getName()), tempDirPath);
        deleteCommand.execute(model);

        assertFalse(model.hasPerson(firstPerson));
        assertTrue(model.hasPerson(secondPerson));
        assertTrue(Files.exists(sharedPhotoFile));
    }
}
