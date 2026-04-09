package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.Set;

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
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * EditCommand.
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

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);
        expectedModel.showPerson(editedPerson);

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

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noActualChangeUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(personToEdit.getName().fullName).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        assertCommandSuccess(editCommand, model, EditCommand.MESSAGE_NO_CHANGES_DONE,
                expectedModel);
    }

    @Test
    public void execute_editAddsTagWhenTagNotPresent_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_THIRD_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags(VALID_TAG_HUSBAND).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editTogglesExistingTagAndAddsNewTag_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withTags("friends", VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags(VALID_TAG_HUSBAND).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editWithOnlyExistingTag_removesTag() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("friends")
                .build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags().build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editWithOnlyExistingTagDifferentCase_removesTag() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags("Friends")
                .build();
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personToEdit), descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withTags().build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
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
        Person personInList = model.getAddressBook().getPersonList()
                .get(INDEX_SECOND_PERSON.getZeroBased());
        Person targetPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(targetPerson),
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_noMatchingPersonUnfilteredList_failure() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .build();
        EditCommand editCommand = new EditCommand(
                new PersonInformation(new Name("Non Existing Person"), null, null, null, null),
                descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_multipleMatchingPersonsUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person duplicateNamePerson = new PersonBuilder(firstPerson).withPhone("81234567").build();
        model.addPerson(duplicateNamePerson);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("92345678")
                .build();
        EditCommand editCommand = new EditCommand(
                new PersonInformation(firstPerson.getName(), null, null, null, null), descriptor);

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () -> editCommand.execute(model));
        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().contains(firstPerson));
        assertTrue(model.getFilteredPersonList().contains(duplicateNamePerson));
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallback_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("85556666")
                .build();

        PersonInformation targetWithDifferentTagCase = new PersonInformation(personToEdit.getName(),
                null, null, null, Set.of(new Tag("FRIENDS")));

        EditCommand editCommand = new EditCommand(targetWithDifferentTagCase, descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withPhone("85556666").build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallbackWithAllTargetFields_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone("86667777")
                .build();

        PersonInformation targetWithDifferentTagCase = new PersonInformation(personToEdit.getName(),
                personToEdit.getPhone(), personToEdit.getEmail().orElse(null),
                personToEdit.getAddress().orElse(null), Set.of(new Tag("FRIENDS")));

        EditCommand editCommand = new EditCommand(targetWithDifferentTagCase, descriptor);

        Person editedPerson = new PersonBuilder(personToEdit).withPhone("86667777").build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallbackphoneMismatch_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        PersonInformation targetInfo = new PersonInformation(firstPerson.getName(),
                secondPerson.getPhone(), null, null, Set.of(new Tag("FRIENDS")));

        EditCommand editCommand = new EditCommand(targetInfo,
                new EditPersonDescriptorBuilder().withName("X").build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallbackemailMismatch_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        PersonInformation targetInfo = new PersonInformation(firstPerson.getName(),
                firstPerson.getPhone(), secondPerson.getEmail().orElse(null), null,
                Set.of(new Tag("FRIENDS")));

        EditCommand editCommand = new EditCommand(targetInfo,
                new EditPersonDescriptorBuilder().withName("X").build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallbackaddressMismatch_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        PersonInformation targetInfo = new PersonInformation(firstPerson.getName(),
                firstPerson.getPhone(), firstPerson.getEmail().orElse(null),
                secondPerson.getAddress().orElse(null), Set.of(new Tag("FRIENDS")));

        EditCommand editCommand = new EditCommand(targetInfo,
                new EditPersonDescriptorBuilder().withName("X").build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_filterCaseInsensitiveTagFallbackmissingTag_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        PersonInformation targetInfo = new PersonInformation(firstPerson.getName(),
                firstPerson.getPhone(), firstPerson.getEmail().orElse(null),
                firstPerson.getAddress().orElse(null), Set.of(new Tag("unknown")));

        EditCommand editCommand = new EditCommand(targetInfo,
                new EditPersonDescriptorBuilder().withName("X").build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    /**
     * Edit still matches against the full address book even when current list is
     * filtered.
     */
    @Test
    public void execute_targetOutsideFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person targetPerson = model.getAddressBook().getPersonList()
                .get(INDEX_SECOND_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(targetPerson).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(
                new PersonInformation(new Name("Benson Meier"), null, null, null, null),
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()),
                new UserPrefs());
        expectedModel.setPerson(targetPerson, editedPerson);
        expectedModel.showPerson(editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void equals() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person secondPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        final EditCommand standardCommand = new EditCommand(targetInfoFromPerson(firstPerson),
                DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(targetInfoFromPerson(firstPerson),
                copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different target information -> returns false
        assertFalse(standardCommand
                .equals(new EditCommand(targetInfoFromPerson(secondPerson), DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand
                .equals(new EditCommand(targetInfoFromPerson(firstPerson), DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        PersonInformation targetInfo = new PersonInformation(new Name("Amy Bee"), null, null, null,
                null);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(targetInfo, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{targetInfo=" + targetInfo
                + ", editPersonDescriptor=" + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

    @Test
    public void execute_editPersonWithPhoto_success(@TempDir Path tempDir) throws Exception {
        Path appFolder = tempDir.resolve("app_storage");
        Path userFolder = tempDir.resolve("user_desktop");
        Files.createDirectory(appFolder);
        Files.createDirectory(userFolder);

        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);

        Path sourceFile = userFolder.resolve("test.jpg");
        Files.createFile(sourceFile);
        String pathToSourceFile = PhotoStorageUtil.formatPath(sourceFile);

        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_BOB).withPhoto(pathToSourceFile).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditCommand editCommand = new EditCommand(targetInfoFromPerson(lastPerson), descriptor, tempDirPath);
        CommandResult result = editCommand.execute(model);

        assertTrue(result.getFeedbackToUser().startsWith("Edited Person:"));
        Person storedPerson = model.getFilteredPersonList().get(0);
        assertTrue(storedPerson.getPhoto().isPresent());
        assertNotEquals(pathToSourceFile, storedPerson.getPhoto().get().getPath());
        assertTrue(PhotoStorageUtil.isSavedLocally(storedPerson.getPhoto().get(), tempDirPath));
        assertTrue(Files.exists(Path.of(storedPerson.getPhoto().get().getPath())));
    }

    @Test
    public void execute_editPersonWithInvalidPhoto_throwsCommandException(@TempDir Path tempDir)
            throws Exception {
        Path appFolder = tempDir.resolve("app_storage");
        Path userFolder = tempDir.resolve("user_desktop");
        Files.createDirectory(appFolder);
        Files.createDirectory(userFolder);

        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);

        String missingFilePath = PhotoStorageUtil.formatPath(userFolder.resolve("does_not_exist.jpg"));
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName(VALID_NAME_BOB).withPhoto(missingFilePath).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(lastPerson), descriptor, tempDirPath);
        assertThrows(CommandException.class, () -> editCommand.execute(model));
    }

    @Test
    public void execute_editPersonWithManagedDirectoryPhoto_throwsCommandException(
            @TempDir Path tempDir) throws Exception {
        Path appFolder = tempDir.resolve("app_storage");
        Files.createDirectory(appFolder);
        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);

        Path existingPhoto = appFolder.resolve("i_exists.jpg");
        Files.createFile(existingPhoto);
        String existingPhotoPath = PhotoStorageUtil.formatPath(existingPhoto);

        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
        Person personWithPhoto = new PersonBuilder(lastPerson).withPhoto(existingPhotoPath)
                .build();
        model.setPerson(lastPerson, personWithPhoto);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhoto(existingPhotoPath).build();

        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personWithPhoto),
                descriptor, tempDirPath);
        assertThrows(CommandException.class, () -> editCommand.execute(model));
    }

    @Test
    public void execute_editPersonPhotoDeletion_throwsCommandException(@TempDir Path tempDir) throws Exception {
        Path appFolder = tempDir.resolve("app_storage");
        Files.createDirectory(appFolder);
        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);

        // cannot_delete.jpg/locked.txt to prevent cannot_delete.jpg from being deleted
        Path undeletableOldPhoto = appFolder.resolve("cannot_delete.jpg");
        Files.createDirectory(undeletableOldPhoto);
        Files.createFile(undeletableOldPhoto.resolve("locked.txt"));
        String oldPhotoPath = PhotoStorageUtil.formatPath(undeletableOldPhoto);

        // Give the person "cannot_delete.jpg"
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());
        Person personWithOldPhoto = new PersonBuilder(lastPerson).withPhoto(oldPhotoPath).build();
        model.setPerson(lastPerson, personWithOldPhoto);

        // Create file to replace "cannot_delete.jpg" with
        Path newPhotoFile = tempDir.resolve("new_photo.jpg");
        Files.createFile(newPhotoFile);
        String newPhotoPath = PhotoStorageUtil.formatPath(newPhotoFile);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhoto(newPhotoPath).build();

        // THE FIX: Inject the temp directory
        EditCommand editCommand = new EditCommand(targetInfoFromPerson(personWithOldPhoto), descriptor, tempDirPath);
        assertThrows(CommandException.class, () -> editCommand.execute(model));
    }
}
