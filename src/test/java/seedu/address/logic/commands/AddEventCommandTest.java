package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddEventCommandTest {

    private static final String VALID_DESCRIPTION = "Complete feature list";
    private static final String VALID_START = "21-02-26 1100";
    private static final String VALID_END = "21-02-26 1500";
    private static final String VALID_NAME = "Amy Bee";

    @Test
    public void constructor_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null, null));
    }

    @Test
    public void execute_eventAcceptedByModel_addSuccessful() throws Exception {
        Event existingEvent = new Event("Prepare slides", "20-02-26 1000", "20-02-26 1200");
        Event eventToAdd = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(VALID_NAME, eventToAdd);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        personToEdit.addEvent(existingEvent);
        ModelStubWithPerson modelStub = new ModelStubWithPerson(personToEdit);

        CommandResult commandResult = addEventCommand.execute(modelStub);

        Person expectedEditedPerson = new PersonBuilder(personToEdit).build();
        expectedEditedPerson.addEvent(existingEvent);
        expectedEditedPerson.addEvent(eventToAdd);

        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS, VALID_NAME, eventToAdd),
                commandResult.getFeedbackToUser());
        assertEquals(personToEdit, modelStub.targetPerson);
        assertTrue(modelStub.editedPerson.getEvents().contains(existingEvent));
        assertTrue(modelStub.editedPerson.getEvents().contains(eventToAdd));
        assertEquals(2, modelStub.editedPerson.getEvents().size());
    }

    @Test
    public void equals() {
        Event eventA = new Event("Meeting A", "21-02-26 1100", "21-02-26 1200");
        Event eventB = new Event("Meeting B", "21-02-26 1300", "21-02-26 1400");

        AddEventCommand addEventACommand = new AddEventCommand(VALID_NAME, eventA);
        AddEventCommand addEventBCommand = new AddEventCommand(VALID_NAME, eventB);

        // same object -> returns true
        assertTrue(addEventACommand.equals(addEventACommand));

        // same values -> returns true
        AddEventCommand addEventACommandCopy = new AddEventCommand(VALID_NAME, eventA);
        assertTrue(addEventACommand.equals(addEventACommandCopy));

        // different types -> returns false
        assertFalse(addEventACommand.equals(1));

        // null -> returns false
        assertFalse(addEventACommand.equals(null));

        // different event -> returns false
        assertFalse(addEventACommand.equals(addEventBCommand));
    }

    @Test
    public void toStringMethod() {
        Event event = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(VALID_NAME, event);
        String expected = "Adding Event: " + event.toString();
        assertEquals(expected, addEventCommand.toString());
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
        public void addPerson(Person person) {
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

        public Person findPersonByName(Name name) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();
        private final Person person;
        private Person targetPerson;
        private Person editedPerson;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
            this.persons.add(person);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return persons;
        }

        @Override
        public Person findPersonByName(Name name) {
            if (person.getName().equals(name)) {
                return person;
            }
            return null;
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            this.targetPerson = target;
            this.editedPerson = editedPerson;
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }
}
