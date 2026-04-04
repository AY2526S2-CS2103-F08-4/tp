package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.testutil.PersonBuilder;

public class AddEventCommandTest {

    private static final String VALID_TITLE = "Feature Review";
    private static final String VALID_DESC = "Complete feature list";
    private static final String VALID_START = "2026-02-21 1100";
    private static final String VALID_END = "2026-02-21 1500";
    private static final String VALID_NAME = "Amy Bee";

    private static PersonInformation infoOf(String name) {
        return new PersonInformation(new Name(name), null, null, null, null);
    }

    private static Event eventOf(String title, String desc, String start, String end) {
        Optional<Description> description = Optional.empty();
        if (desc != null) {
            description = Optional.of(new Description(desc));
        }
        return new Event(new Title(title), description, new TimeRange(start, end));
    }

    @Test
    public void constructor_nullEvent_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(infoOf(VALID_NAME), null));
    }

    @Test
    public void constructor_nullContact_throwsNullPointerException() {
        Event event = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null, event));
    }

    @Test
    public void execute_newEvent_addSuccessful() throws Exception {
        Event existingEvent = eventOf("Prepare slides", "Draft deck", "2026-02-20 1000", "2026-02-20 1200");
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        personToEdit.addEvent(existingEvent);
        ModelStubWithPersonNoEvent modelStub = new ModelStubWithPersonNoEvent(personToEdit);

        CommandResult commandResult = addEventCommand.execute(modelStub);

        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS, personToEdit.getName(), eventToAdd),
                commandResult.getFeedbackToUser());
        assertEquals(personToEdit, modelStub.targetPerson);
        assertTrue(modelStub.editedPerson.getEvents().contains(existingEvent));
        assertTrue(modelStub.editedPerson.getEvents().contains(eventToAdd));
        assertEquals(2, modelStub.editedPerson.getEvents().size());
        assertEquals(eventToAdd, modelStub.addedEvent);
        assertFalse(modelStub.linkCalled);
    }

    @Test
    public void execute_existingEvent_linksExistingEvent() throws Exception {
        Event existingEvent = eventOf(VALID_TITLE, "Existing", VALID_START, VALID_END);
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person personToEdit = new PersonBuilder().withName(VALID_NAME).build();
        ModelStubWithPersonExistingEvent modelStub = new ModelStubWithPersonExistingEvent(personToEdit, existingEvent);

        CommandResult commandResult = addEventCommand.execute(modelStub);

        assertEquals(String.format(AddEventCommand.MESSAGE_SUCCESS, personToEdit.getName(), eventToAdd),
                commandResult.getFeedbackToUser());
        assertTrue(modelStub.editedPerson.getEvents().contains(existingEvent));
        assertEquals(1, modelStub.editedPerson.getEvents().size());
        assertTrue(modelStub.linkCalled);
        assertFalse(modelStub.addCalled);
    }

    @Test
    public void execute_personAlreadyHasEvent_throwsCommandException() {
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person personWithEvent = new PersonBuilder().withName(VALID_NAME).build();
        personWithEvent.addEvent(eventToAdd);
        ModelStubWithPersonNoEvent modelStub = new ModelStubWithPersonNoEvent(personWithEvent);

        assertThrows(CommandException.class,
                String.format(AddEventCommand.MESSAGE_DUPLICATE_EVENT, eventToAdd), () ->
                    addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_clashingEvent_throwsCommandException() {
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);

        Person person = new PersonBuilder().withName(VALID_NAME).build();
        ModelStubWithOverlappingEvent modelStub = new ModelStubWithOverlappingEvent(person);

        assertThrows(CommandException.class,
            AddEventCommand.MESSAGE_CLASHING_EVENT, () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_contactNotFound_throwsCommandException() {
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);
        ModelStubWithNoPerson modelStub = new ModelStubWithNoPerson();

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () ->
                addEventCommand.execute(modelStub));
    }

    @Test
    public void execute_multipleContactsFound_throwsCommandException() {
        Person first = new PersonBuilder().withName(VALID_NAME).withPhone("90001111").build();
        Person second = new PersonBuilder().withName(VALID_NAME).withPhone("90002222").build();
        Event eventToAdd = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), eventToAdd);
        ModelStubWithMultiplePersons modelStub = new ModelStubWithMultiplePersons(List.of(first, second));

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () ->
                addEventCommand.execute(modelStub));
        assertTrue(modelStub.filteredPersonsUpdated);
        assertTrue(modelStub.filteredEventsUpdated);
    }

    @Test
    public void equals() {
        Event eventA = eventOf("Meeting A", "Sync", "2026-02-21 1100", "2026-02-21 1200");
        Event eventB = eventOf("Meeting B", "Review", "2026-02-21 1300", "2026-02-21 1400");

        AddEventCommand addEventACommand = new AddEventCommand(infoOf(VALID_NAME), eventA);
        AddEventCommand addEventBCommand = new AddEventCommand(infoOf(VALID_NAME), eventB);

        assertTrue(addEventACommand.equals(addEventACommand));
        AddEventCommand addEventACommandCopy = new AddEventCommand(infoOf(VALID_NAME), eventA);
        assertTrue(addEventACommand.equals(addEventACommandCopy));
        assertFalse(addEventACommand.equals(1));
        assertFalse(addEventACommand.equals(null));
        assertFalse(addEventACommand.equals(addEventBCommand));
        AddEventCommand differentPerson = new AddEventCommand(infoOf("Bob Choo"), eventA);
        assertFalse(addEventACommand.equals(differentPerson));
    }

    @Test
    public void toStringMethod() {
        Event event = eventOf(VALID_TITLE, VALID_DESC, VALID_START, VALID_END);
        AddEventCommand addEventCommand = new AddEventCommand(infoOf(VALID_NAME), event);
        String expected = "Adding Event: " + event.toString();
        assertEquals(expected, addEventCommand.toString());
    }

    // ==================== Model Stubs ====================

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
        @Override
        public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public List<Person> findPersons(PersonInformation info) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public boolean hasEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public void addEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
        public void deleteEvent(Event target) {
            throw new AssertionError("This method should not be called.");
        }
        @Override
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
    }

    private class ModelStubWithPersonNoEvent extends ModelStub {
        private final Person person;
        private Person targetPerson;
        private Person editedPerson;
        private Event addedEvent;
        private boolean addCalled;
        private boolean linkCalled;

        ModelStubWithPersonNoEvent(Person person) {
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
        public void showEventsForPerson(Person person) {}

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }

        @Override
        public boolean hasEvent(Event event) {
            return false;
        }

        @Override
        public void addEvent(Event event) {
            addCalled = true;
            addedEvent = event;
        }

        @Override
        public Event linkPersonToEvent(Event eventToAdd) {
            linkCalled = true;
            return eventToAdd;
        }

        @Override
        public boolean hasOverlappingEvent(Event event) {
            return false;
        }
    }

    private class ModelStubWithPersonExistingEvent extends ModelStub {
        private final Person person;
        private final Event existingEvent;
        private Person editedPerson;
        private boolean addCalled;
        private boolean linkCalled;

        ModelStubWithPersonExistingEvent(Person person, Event existingEvent) {
            this.person = person;
            this.existingEvent = existingEvent;
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
            this.editedPerson = editedPerson;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}

        @Override
        public void showEventsForPerson(Person person) {}

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }

        @Override
        public boolean hasEvent(Event event) {
            return true;
        }

        @Override
        public void addEvent(Event event) {
            addCalled = true;
        }

        @Override
        public Event linkPersonToEvent(Event eventToAdd) {
            linkCalled = true;
            return existingEvent;
        }

        @Override
        public boolean hasOverlappingEvent(Event event) {
            return false;
        }
    }

    private class ModelStubWithOverlappingEvent extends ModelStub {
        private final Person person;

        ModelStubWithOverlappingEvent(Person person) {
            this.person = person;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return List.of(person);
        }

        @Override
        public boolean hasEvent(Event event) {
            return false;
        }

        @Override
        public boolean hasOverlappingEvent(Event event) {
            return true;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {}

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {}
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

        @Override
        public boolean hasOverlappingEvent(Event event) {
            return false;
        }
    }

    private class ModelStubWithMultiplePersons extends ModelStub {
        private final List<Person> persons;
        private boolean filteredPersonsUpdated;
        private boolean filteredEventsUpdated;

        ModelStubWithMultiplePersons(List<Person> persons) {
            this.persons = persons;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return persons;
        }

        @Override
        public void showMatchingPersons(java.util.Set<Person> persons) {
            filteredPersonsUpdated = true;
            filteredEventsUpdated = true;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            filteredPersonsUpdated = true;
        }

        @Override
        public void updateFilteredEventList(Predicate<Event> predicate) {
            filteredEventsUpdated = true;
        }

        @Override
        public boolean hasOverlappingEvent(Event event) {
            return false;
        }
    }
}
