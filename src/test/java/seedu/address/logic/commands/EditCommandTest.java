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
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
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
import seedu.address.model.person.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.Photo;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().build();
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        for (Event event : personToEdit.getEvents()) {
            editedPerson.addEvent(event);
        }

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

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
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    /*
    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }
    */

    /*
    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        // To review when implementing edit
        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }
     */

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
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

            Photo expectedPhoto = PhotoStorageUtil.copyPhotoToDirectory(new Photo(pathToSourceFile));

            Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
            Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
            PersonBuilder personInList = new PersonBuilder(lastPerson);

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withName(VALID_NAME_BOB)
                    .withPhoto(pathToSourceFile)
                    .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();

            Person editedPerson = personInList.withName(VALID_NAME_BOB)
                    .withPhone(VALID_PHONE_BOB)
                    .withPhoto(expectedPhoto.getPath())
                    .withTags(VALID_TAG_HUSBAND).build();

            EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);
            String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                    Messages.format(editedPerson));

            Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
            expectedModel.setPerson(lastPerson, editedPerson);

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

            EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);
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

            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                    .withPhoto(existingPhotoPath)
                    .build();

            EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);
            String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                    Messages.format(personWithPhoto));

            Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
            expectedModel.setPerson(personWithPhoto, personWithPhoto);
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
            EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);
            assertThrows(CommandException.class, () -> editCommand.execute(model));
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }
}
