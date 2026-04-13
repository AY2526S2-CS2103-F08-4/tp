package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddTagCommandTest {

    @Test
    public void execute_validTargets_success() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Person alice = new PersonBuilder().withName("Alice").withPhone("90001111")
                .withEvents("Meeting,2026-03-20 1200,2026-03-20 1300").build();
        Person joe = new PersonBuilder().withName("Joe").withPhone("90002222").withTags("Family").build();
        model.addPerson(alice);
        model.addPerson(joe);

        AddTagCommand command = new AddTagCommand(
                List.of(info("Alice"), info("Joe")),
                Set.of(new Tag("CS2030S"), new Tag("CS2103"))
        );

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedAlice = new PersonBuilder(alice).withTags("cs2030s", "cs2103").build();
        Person expectedJoe = new PersonBuilder(joe).withTags("family", "cs2030s", "cs2103").build();
        expectedModel.setPerson(alice, expectedAlice);
        expectedModel.setPerson(joe, expectedJoe);
        expectedModel.showAllPersons();

        assertCommandSuccess(command, model,
                "Tagged 2 person(s) with [cs2030s, cs2103]: Alice, Joe", expectedModel);
    }

    @Test
    public void execute_duplicateResolvedPersons_failure() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Person alice = new PersonBuilder().withName("Alice").withPhone("90003333").build();
        model.addPerson(alice);

        AddTagCommand command = new AddTagCommand(
                List.of(info("Alice"), info("Alice")),
                Set.of(new Tag("CS2103"))
        );

        assertCommandFailure(command, model,
                String.format(AddTagCommand.MESSAGE_DUPLICATE_TARGET_PERSON, "Alice"));
    }

    @Test
    public void execute_noMatch_failure() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        AddTagCommand command = new AddTagCommand(List.of(info("Ghost")), Set.of(new Tag("CS2103")));
        assertCommandFailure(command, model,
                "No matching contact found for target: name=Ghost.");
    }

    @Test
    public void execute_multipleMatch_failureAndShowsMatches() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Person joeOne = new PersonBuilder().withName("Joe").withPhone("81111111").build();
        Person joeTwo = new PersonBuilder().withName("Joe").withPhone("82222222").build();
        model.addPerson(joeOne);
        model.addPerson(joeTwo);

        AddTagCommand command = new AddTagCommand(List.of(info("Joe")), Set.of(new Tag("CS2103")));
        CommandException thrown = assertThrows(CommandException.class, () -> command.execute(model));

        assertTrue(AddTagCommand.MESSAGE_MULTIPLE_MATCHES_FOR_TARGET.formatted("name=Joe")
                .equals(thrown.getMessage()));
        assertTrue(model.getFilteredPersonList().size() == 2);
    }

    @Test
    public void execute_multipleMatchWithFullTargetInfo_failureAndShowsFormattedSummary() {
        AddTagCommand command = new AddTagCommand(List.of(new PersonInformation(
                new Name("Alex"),
                new Phone("81111111"),
                new Email("alex.one@example.com"),
                new Address("NUS"),
                Set.of(new Tag("Friends")))), Set.of(new Tag("CS2103")));
        Model model = new ModelStubWithMultipleMatchingPersons(List.of(
                new PersonBuilder().withName("Alex").withPhone("81111111")
                        .withEmail("alex.one@example.com").withAddress("NUS").withTags("Friends").build(),
                new PersonBuilder().withName("Alex").withPhone("82222222")
                        .withEmail("alex.two@example.com").withAddress("COM2").withTags("Friends").build()));
        CommandException thrown = assertThrows(CommandException.class, () -> command.execute(model));

        assertTrue(AddTagCommand.MESSAGE_MULTIPLE_MATCHES_FOR_TARGET.formatted(
                "name=Alex, phone=81111111, email=alex.one@example.com, address=NUS, tags=friends")
                .equals(thrown.getMessage()));
    }

    @Test
    public void execute_targetResolutionThrowsUnexpectedCommandException_rethrowsOriginalException() {
        CommandException expectedException = new CommandException("Unexpected target resolution failure");
        AddTagCommand command = new AddTagCommand(List.of(info("Alice")), Set.of(new Tag("CS2103")));

        CommandException thrown = assertThrows(CommandException.class, () ->
                command.execute(new ModelStubThrowingCommandException(expectedException)));

        assertTrue(expectedException == thrown);
    }

    @Test
    public void equals() {
        AddTagCommand first = new AddTagCommand(List.of(info("Alice")), Set.of(new Tag("CS2103")));
        AddTagCommand second = new AddTagCommand(List.of(info("Bob")), Set.of(new Tag("CS2030S")));
        AddTagCommand firstCopy = new AddTagCommand(List.of(info("Alice")), Set.of(new Tag("CS2103")));
        AddTagCommand sameTargetDifferentTags = new AddTagCommand(
                List.of(info("Alice")), Set.of(new Tag("CS2040")));

        assertTrue(first.equals(first));
        assertTrue(first.equals(firstCopy));
        assertFalse(first.equals(sameTargetDifferentTags));
        assertFalse(first.equals(1));
        assertFalse(first.equals(null));
        assertFalse(first.equals(second));
    }

    private static PersonInformation info(String name) {
        return new PersonInformation(new Name(name), null, null, null, null);
    }

    @Test
    public void constructor_nullTargets_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddTagCommand(null, Set.of(new Tag("CS2103"))));
    }

    @Test
    public void constructor_nullTags_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddTagCommand(List.of(info("Alice")), null));
    }

    @Test
    public void toString_containsFields() {
        AddTagCommand command = new AddTagCommand(
                List.of(new PersonInformation(new Name("Alice"), new Phone("90009999"), null, null, null)),
                Set.of(new Tag("CS2103")));
        String stringForm = command.toString();
        assertTrue(stringForm.contains("targets"));
        assertTrue(stringForm.contains("tagsToAssign"));
    }

    /**
     * A default model stub that fails all methods unless overridden.
     */
    private static class ModelStub implements Model {
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
        public void setAddressBook(ReadOnlyAddressBook addressBook) {
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
        public void showMatchingPersons(Set<Person> persons) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showPerson(Person person) {
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
        public boolean hasOverlappingEvent(Event event) {
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
        public ObservableList<Event> getFilteredEventList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void showNoEvents() {
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
        public void showEventsForPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean isPhotoShared(Photo photo, Person personToExclude) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Event> getOverlappingEvent(Event e) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public String getNamesLinkedToEvent(Event event) {
            throw new AssertionError("This method should not be called.");
        }
    }

    private static class ModelStubThrowingCommandException extends ModelStub {
        private final CommandException exceptionToThrow;

        private ModelStubThrowingCommandException(CommandException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return sneakyThrow(exceptionToThrow);
        }
    }

    private static class ModelStubWithMultipleMatchingPersons extends ModelStub {
        private final List<Person> matchingPersons;

        private ModelStubWithMultipleMatchingPersons(List<Person> matchingPersons) {
            this.matchingPersons = matchingPersons;
        }

        @Override
        public List<Person> findPersons(PersonInformation info) {
            return matchingPersons;
        }

        @Override
        public void showMatchingPersons(Set<Person> persons) {
            // no-op for this test
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, E extends Throwable> T sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
