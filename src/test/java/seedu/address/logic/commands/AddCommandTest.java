package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.PhotoStorageUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Photo;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void execute_addPersonWithPhoto_success(@TempDir Path tempDir) throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

        Path appFolder = tempDir.resolve("app_storage");
        Path userFolder = tempDir.resolve("user_desktop");
        Files.createDirectory(appFolder);
        Files.createDirectory(userFolder);

        String originalDir = PhotoStorageUtil.getImageDirectory();
        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);
        PhotoStorageUtil.setImageDirectory(tempDirPath);

        try {
            Path sourceFile = userFolder.resolve("test.jpg");
            Files.createFile(sourceFile);
            String pathToSourceFile = PhotoStorageUtil.formatPath(sourceFile);

            Person validPersonWithPhoto = new PersonBuilder().withPhoto(pathToSourceFile).build();
            CommandResult commandResult = new AddCommand(validPersonWithPhoto).execute(modelStub);
            Person addedPerson = modelStub.personsAdded.get(0);

            assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(addedPerson)),
                    commandResult.getFeedbackToUser());
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }

    @Test
    public void execute_photoCopyFails_throwsCommandException(@TempDir Path tempDir) throws IOException {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();

        Path appFolder = tempDir.resolve("app_storage");
        Path userFolder = tempDir.resolve("user_desktop");
        Files.createDirectory(appFolder);
        Files.createDirectory(userFolder);

        String originalDir = PhotoStorageUtil.getImageDirectory();
        String tempDirPath = PhotoStorageUtil.formatPath(appFolder);
        PhotoStorageUtil.setImageDirectory(tempDirPath);

        try {
            String dummyFile = PhotoStorageUtil.formatPath(userFolder.resolve("does_not_exist.jpg"));
            Person personWithInvalidPhoto = new PersonBuilder().withPhoto(dummyFile).build();
            AddCommand addCommand = new AddCommand(personWithInvalidPhoto);

            // Photo does not exist
            assertThrows(CommandException.class, () -> addCommand.execute(modelStub));
        } finally {
            PhotoStorageUtil.setImageDirectory(originalDir);
        }
    }

    @Test
    public void parse_personWithPhoto_success() {

    }

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                commandResult.getFeedbackToUser());
        assertEquals(Arrays.asList(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person validPerson = new PersonBuilder().build();
        AddCommand addCommand = new AddCommand(validPerson);
        ModelStub modelStub = new ModelStubWithPerson(validPerson);

        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> addCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder().withName("Alice").build();
        Person bob = new PersonBuilder().withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        AddCommand addCommand = new AddCommand(ALICE);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + ALICE + "}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public Path getAddressBookFilePath() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBookFilePath(Path addressBookFilePath) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showAllPersons() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showAllPersonsPinnedFirst() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showPersons(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showMatchingPersons(java.util.Set<Person> persons) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showEventsForPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        public List<Person> findPersons(PersonInformation info) {
            throw new AssertionError("This method should not be called.");
        }

        public boolean hasEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }

        public void addEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }

        public void deleteEvent(Event target) {
            throw new AssertionError("This method should not be called.");
        }

        public void setEvent(Event target, Event editedEvent) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public Event linkPersonToEvent(Event eventToAdd) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public Event unlinkPersonFromEvent(Event eventToUnlink) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public boolean hasOverlappingEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void pinPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void unpinPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean isPersonPinned(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean isPhotoShared(Photo photo, Person personToExclude) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }
    }

    /**
     * A Model stub that always accept the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::isSamePerson);
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public void showAllPersons() {}

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

}
