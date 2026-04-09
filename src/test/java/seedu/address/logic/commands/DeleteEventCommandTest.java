package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.time.LocalDateTime;
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
import seedu.address.model.person.Photo;
import seedu.address.testutil.PersonBuilder;

public class DeleteEventCommandTest {

    private static final String VALID_NAME = "Amy Bee";
    private static final String VALID_START = "2026-02-21 1100";
    private static final String VALID_END = "2026-02-21 1500";
    private static final String OTHER_START = "2026-02-22 1000";
    private static final String OTHER_END = "2026-02-22 1200";

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

    private static LocalDateTime startOf(String start) {
        return LocalDateTime.parse(start, TimeRange.DATE_TIME_FORMATTER);
    }

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteEventCommand(null, startOf(VALID_START)));
    }

    @Test
    public void constructor_nullStart_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteEventCommand(infoOf(VALID_NAME), null));
    }

    @Test
    public void execute_eventDeleted_success() throws Exception {
        Event eventToDelete = eventOf("CS2103 Meeting", "Weekly", VALID_START, VALID_END);
        Event otherEvent = eventOf("Lunch", "Team", OTHER_START, OTHER_END);

        Person person = new PersonBuilder().withName(VALID_NAME).build();
        person.addEvent(eventToDelete);
        person.addEvent(otherEvent);

        ModelStubWithPerson modelStub = new ModelStubWithPerson(person, eventToDelete);
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), startOf(VALID_START));

        CommandResult result = command.execute(modelStub);

        assertEquals(String.format(DeleteEventCommand.MESSAGE_SUCCESS, VALID_NAME, eventToDelete),
                result.getFeedbackToUser());
        assertFalse(modelStub.editedPerson.getEvents().contains(eventToDelete));
        assertTrue(modelStub.editedPerson.getEvents().contains(otherEvent));
        assertEquals(1, modelStub.editedPerson.getEvents().size());
        assertTrue(modelStub.unlinkCalled);
        assertEquals(modelStub.editedPerson, modelStub.shownPerson);
    }

    @Test
    public void execute_contactNotFound_throwsCommandException() {
        ModelStubWithNoPerson modelStub = new ModelStubWithNoPerson();
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), startOf(VALID_START));

        assertThrows(CommandException.class, Messages.MESSAGE_NO_MATCH, () -> command.execute(modelStub));
    }

    @Test
    public void execute_multipleContactsFound_throwsCommandException() {
        Person firstMatch = new PersonBuilder().withName(VALID_NAME).withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName(VALID_NAME).withPhone("90002222").build();

        ModelStubWithMultiplePersons modelStub = new ModelStubWithMultiplePersons(
                List.of(firstMatch, secondMatch));
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), startOf(VALID_START));

        assertThrows(CommandException.class, Messages.MESSAGE_MULTIPLE_MATCH, () -> command.execute(modelStub));
        assertTrue(modelStub.filteredPersonsUpdated);
        assertTrue(modelStub.filteredEventsUpdated);
    }

    @Test
    public void execute_eventNotFound_throwsCommandException() {
        Person person = new PersonBuilder().withName(VALID_NAME).build();
        ModelStubWithPerson modelStub = new ModelStubWithPerson(person, null);
        DeleteEventCommand command = new DeleteEventCommand(infoOf(VALID_NAME), startOf(VALID_START));

        String expectedMessage = String.format(DeleteEventCommand.MESSAGE_EVENT_NOT_FOUND, VALID_START);
        assertThrows(CommandException.class, expectedMessage, () -> command.execute(modelStub));
    }

    @Test
    public void equals() {
        PersonInformation info = infoOf(VALID_NAME);
        DeleteEventCommand command = new DeleteEventCommand(info, startOf(VALID_START));
        DeleteEventCommand commandCopy = new DeleteEventCommand(info, startOf(VALID_START));
        DeleteEventCommand differentStart = new DeleteEventCommand(info, startOf(OTHER_START));
        DeleteEventCommand differentPerson = new DeleteEventCommand(infoOf("Bob Choo"), startOf(VALID_START));

        assertTrue(command.equals(command));
        assertTrue(command.equals(commandCopy));
        assertFalse(command.equals(1));
        assertFalse(command.equals(null));
        assertFalse(command.equals(differentStart));
        assertFalse(command.equals(differentPerson));
    }

    @Test
    public void toStringMethod() {
        PersonInformation info = infoOf(VALID_NAME);
        DeleteEventCommand command = new DeleteEventCommand(info, startOf(VALID_START));
        String expected = String.format("Deleting Event for %s at start time %s", VALID_NAME, VALID_START);
        assertEquals(expected, command.toString());
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
        public void showPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showEventsForPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showNoEvents() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Person> searchPersons(PersonInformation info) {
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
        public List<Event> getOverlappingEvent(Event event) {
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

    private class ModelStubWithPerson extends ModelStub {
        private final Person person;
        private final Event eventToUnlink;
        private Person editedPerson;
        private Person shownPerson;
        private boolean unlinkCalled;

        ModelStubWithPerson(Person person, Event eventToUnlink) {
            requireNonNull(person);
            this.person = person;
            this.eventToUnlink = eventToUnlink;
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
        public void updateFilteredPersonList(Predicate<Person> predicate) {
        }

        @Override
        public void showEventsForPerson(Person person) {
            shownPerson = person;
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }

        @Override
        public Event unlinkPersonFromEvent(Event eventToUnlink) {
            unlinkCalled = true;
            return this.eventToUnlink;
        }
    }

    private class ModelStubWithNoPerson extends ModelStub {
        @Override
        public List<Person> findPersons(PersonInformation info) {
            return List.of();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
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
    }
}
