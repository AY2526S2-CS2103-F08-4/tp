package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.PersonBuilder;

public class AddEventCommandTest {

    private static final String VALID_DESCRIPTION = "Complete feature list";
    private static final String VALID_START = "21-02-26 1100";
    private static final String VALID_END = "21-02-26 1500";
    private static final String VALID_NAME = "Amy Bee";

    private static PersonInformation infoOf(String name) {
        return new PersonInformation(new Name(name), null, null, null, null);
    }

    @Test
    public void constructor_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(infoOf(VALID_NAME), null));
    }

    @Test
    public void constructor_nullContact_throwsNullPointerException() {
        Event event = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null, event));
    }

    @Test
    public void execute_eventAcceptedByModel_addSuccessful() throws Exception {
        Event existingEvent = new Event("Prepare slides", "20-02-26 1000", "20-02-26 1200");
        Event eventToAdd = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        personToEdit.addEvent(existingEvent);
        ModelStubWithPerson modelStub = new ModelStubWithPerson(personToEdit);

        CommandResult commandResult = addEventCommand.execute(modelStub);

        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS, personToEdit.getName(), eventToAdd),
                commandResult.getFeedbackToUser());
        assertEquals(personToEdit, modelStub.targetPerson);
        assertTrue(modelStub.editedPerson.getEvents().contains(existingEvent));
        assertTrue(modelStub.editedPerson.getEvents().contains(eventToAdd));
        assertEquals(2, modelStub.editedPerson.getEvents().size());
    }

    @Test
    public void execute_duplicateEvent_throwsCommandException() {
        Event eventToAdd = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        personToEdit.addEvent(eventToAdd);
        ModelStubWithPerson modelStub = new ModelStubWithPerson(personToEdit);

        assertThrows(CommandException.class,
                String.format(AddEventCommand.MESSAGE_DUPLICATE_EVENT, eventToAdd), () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_duplicateEventSameTimeDifferentDescription_throwsCommandException() {
        Event existing = new Event("Original description", VALID_START, VALID_END);
        Event duplicate = new Event("Different description", VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), duplicate);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        personToEdit.addEvent(existing);
        ModelStubWithPerson modelStub = new ModelStubWithPerson(personToEdit);

        assertThrows(CommandException.class,
                String.format(AddEventCommand.MESSAGE_DUPLICATE_EVENT, duplicate), () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_contactNotFound_throwsCommandException() {
        Event eventToAdd = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);
        ModelStubWithNoPerson modelStub = new ModelStubWithNoPerson();

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_multipleContactsFound_throwsCommandException() {
        Person first = new PersonBuilder().withName(VALID_NAME).withPhone("90001111").build();
        Person second = new PersonBuilder().withName(VALID_NAME).withPhone("90002222").build();
        Event eventToAdd = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);
        ModelStubWithMultiplePersons modelStub = new ModelStubWithMultiplePersons(List.of(first, second));

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        Event eventA = new Event("Meeting A", "21-02-26 1100", "21-02-26 1200");
        Event eventB = new Event("Meeting B", "21-02-26 1300", "21-02-26 1400");

        AddEventCommand addEventACommand = new AddEventCommand(infoOf(VALID_NAME), eventA);
        AddEventCommand addEventBCommand = new AddEventCommand(infoOf(VALID_NAME), eventB);

        // same object -> returns true
        assertTrue(addEventACommand.equals(addEventACommand));

        // same values -> returns true
        AddEventCommand addEventACommandCopy = new AddEventCommand(infoOf(VALID_NAME), eventA);
        assertTrue(addEventACommand.equals(addEventACommandCopy));

        // different types -> returns false
        assertFalse(addEventACommand.equals(1));

        // null -> returns false
        assertFalse(addEventACommand.equals(null));

        // different event -> returns false
        assertFalse(addEventACommand.equals(addEventBCommand));

        // different person -> returns false
        AddEventCommand differentPerson = new AddEventCommand(infoOf("Bob Choo"), eventA);
        assertFalse(addEventACommand.equals(differentPerson));
    }

    @Test
    public void toStringMethod() {
        Event event = new Event(VALID_DESCRIPTION, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), event);
        String expected = "Adding Event: " + event.toString();
        assertEquals(expected, addEventCommand.toString());
    }

    // ==================== Model Stubs ====================

    private class ModelStub implements Model {
        @Override public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public Path getAddressBookFilePath() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setAddressBookFilePath(Path addressBookFilePath) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }
        @Override public void updateFilteredEventList(Predicate<Event> predicate) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public Person findPersonByName(Name name) {
            throw new AssertionError("This method should not be called.");
        }
        @Override public List<Person> findPersons(PersonInformation info) {
            throw new AssertionError("This method should not be called.");
        }
    }

    private class ModelStubWithPerson extends ModelStub {
        private final Person person;
        private Person targetPerson;
        private Person editedPerson;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            if (person.getName().equals(info.name)) {
                return List.of(person);
            }
            return List.of();
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            this.targetPerson = target;
            this.editedPerson = editedPerson;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

    private class ModelStubWithNoPerson extends ModelStub {
        @Override
        public List<Person> findPersons(PersonInformation info) {
            return List.of();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}
    }

    private class ModelStubWithMultiplePersons extends ModelStub {
        private final List<Person> persons;

        ModelStubWithMultiplePersons(List<Person> persons) {
            this.persons = persons;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return persons;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}
    }
}
