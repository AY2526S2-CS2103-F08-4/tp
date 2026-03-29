package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static PersonInformation targetInfoFromPerson(Person person) {
        return new PersonInformation(person.getName(), null, null, null, null);
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        for (Event event : personToEdit.getEvents()) {
            editedPerson.addEvent(event);
        }

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);
        expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(lastPerson), descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);
        expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noActualChangeUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(personToEdit.getName().fullName)
                .build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(editCommand, model, EditCommand.MESSAGE_NO_CHANGES_DONE, expectedModel);
    }

    @Test
    public void execute_editAddsTagWhenTagNotPresent_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_THIRD_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags(VALID_TAG_HUSBAND).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editAddsOnlyNewTagsWhenSomeAlreadyPresent_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags("friends", VALID_TAG_HUSBAND)
                .build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags("friends", VALID_TAG_HUSBAND).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editWithOnlyExistingTag_noChangesDone() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("friends").build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(editCommand, model, EditCommand.MESSAGE_NO_CHANGES_DONE, expectedModel);
    }

    @Test
    public void execute_editWithOnlyExistingTagDifferentCase_noChangesDone() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("Friends").build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        assertCommandSuccess(editCommand, model, EditCommand.MESSAGE_NO_CHANGES_DONE, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(secondPerson), descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person targetPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(targetPerson),
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_noMatchingPersonUnfilteredList_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(
                new PersonInformation(new Name("Non Existing Person"), null, null, null, null), descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    /**
         * Edit still matches against the full address book even when current list is filtered.
     */
    @Test
    public void execute_targetOutsideFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person targetPerson = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(targetPerson).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(
                new PersonInformation(new Name("Benson Meier"), null, null, null, null),
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(targetPerson, editedPerson);
        expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        final EditCommand standardCommand = new EditCommand(targetInfoFromPerson(firstPerson), DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(targetInfoFromPerson(firstPerson), copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different target information -> returns false
        assertFalse(standardCommand.equals(new EditCommand(targetInfoFromPerson(secondPerson), DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(targetInfoFromPerson(firstPerson), DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        PersonInformation targetInfo = new PersonInformation(new Name("Amy Bee"), null, null, null, null);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(targetInfo, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{targetInfo=" + targetInfo
            + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    @Test
    public void execute_editPersonWithPhoto_success(@TempDir Path tempDir) throws Exception {
        String originalDir = PhotoStorageUtil.getImageDirectory();
        String tempDirPath = tempDir.toString().replace("\\", "/") + "/";
        PhotoStorageUtil.setImageDirectory(tempDirPath);


        try {
            Path sourceFile = tempDir.resolve("test.jpg");
            Files.createFile(sourceFile);
            String pathToSourceFile = sourceFile.toString().replace("\\", "/");

            Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
            Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
            PersonBuilder personInList = new PersonBuilder(lastPerson);

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withName(VALID_NAME_BOB)
                    .withPhoto(pathToSourceFile)
                    .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();

            Person editedPerson = personInList.withName(VALID_NAME_BOB)
                    .withPhone(VALID_PHONE_BOB)
                    .withPhoto(pathToSourceFile)
                    .withTags(VALID_TAG_HUSBAND).build();

            EditCommand editCommand = new EditCommand(targetInfoFromPerson(lastPerson), descriptor);
            String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                    Messages.format(editedPerson));

            Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
            expectedModel.setPerson(lastPerson, editedPerson);
            expectedModel.updateFilteredPersonList(p -> p.equals(editedPerson));

            assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }

    @Test
    public void execute_editPersonWithInvalidPhoto_throwsCommandException(@TempDir Path tempDir) throws Exception {
        String originalDir = PhotoStorageUtil.getImageDirectory();
        Path appFolder = tempDir.resolve("app_storage");
        Path userFolder = tempDir.resolve("user_desktop");
        Files.createDirectory(appFolder);
        Files.createDirectory(userFolder);

        String tempDirPath = appFolder.toString().replace("\\", "/") + "/";
        PhotoStorageUtil.setImageDirectory(tempDirPath);

        try {
            String missingFilePath = userFolder.resolve("does_not_exist.jpg")
                    .toString().replace("\\", "/");
            Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withName(VALID_NAME_BOB)
                    .withPhoto(missingFilePath)
                    .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();

            Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
            EditCommand editCommand = new EditCommand(targetInfoFromPerson(lastPerson), descriptor);
            assertThrows(CommandException.class, () -> editCommand.execute(model));
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }

    @Test
    public void execute_editPersonWithSamePhoto_success(@TempDir Path tempDir) throws Exception {
        String originalDir = PhotoStorageUtil.getImageDirectory();
        Path appFolder = tempDir.resolve("app_storage");
        Files.createDirectory(appFolder);

        String tempDirPath = appFolder.toString().replace("\\", "/") + "/";
        PhotoStorageUtil.setImageDirectory(tempDirPath);

        try {
            Path existingPhoto = appFolder.resolve("i_exists.jpg");
            Files.createFile(existingPhoto);
            String existingPhotoPath = existingPhoto.toString().replace("\\", "/");

            Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
            Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
            Person personWithPhoto = new PersonBuilder(lastPerson).withPhoto(existingPhotoPath).build();
            model.setPerson(lastPerson, personWithPhoto);
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withPhoto(existingPhotoPath)
                    .build();

            EditCommand editCommand = new EditCommand(targetInfoFromPerson(personWithPhoto), descriptor);
            String expectedMessage = EditCommand.MESSAGE_NO_CHANGES_DONE;

            Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
            assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }

    @Test
    public void execute_editPersonPhotoDeletion_throwsCommandException(@TempDir Path tempDir) throws Exception {
        String originalDir = PhotoStorageUtil.getImageDirectory();
        Path appFolder = tempDir.resolve("app_storage");
        Files.createDirectory(appFolder);
        PhotoStorageUtil.setImageDirectory(appFolder.toString().replace("\\", "/") + "/");

        try {
            // cannot_delete.jpg/locked.txt to prevent cannot_delete.jpg from being deleted trick
            Path undeletableOldPhoto = appFolder.resolve("cannot_delete.jpg");
            Files.createDirectory(undeletableOldPhoto);
            Files.createFile(undeletableOldPhoto.resolve("locked.txt"));
            String oldPhotoPath = undeletableOldPhoto.toString().replace("\\", "/");

            // Give the person "cannot_delete.jpg"
            Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
            Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
            Person personWithOldPhoto = new PersonBuilder(lastPerson).withPhoto(oldPhotoPath).build();
            model.setPerson(lastPerson, personWithOldPhoto);

            // Create file to replace "cannot_delete.jpg" with
            Path newPhotoFile = tempDir.resolve("new_photo.jpg");
            Files.createFile(newPhotoFile);
            String newPhotoPath = newPhotoFile.toString().replace("\\", "/");

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withPhoto(newPhotoPath)
                    .build();
            EditCommand editCommand = new EditCommand(targetInfoFromPerson(personWithOldPhoto), descriptor);
            assertThrows(CommandException.class, () -> editCommand.execute(model));
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }
}
