package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_EVENTS_LISTED_OVERVIEW;
import static seedu.address.logic.Messages.MESSAGE_MULTIPLE_MATCH;
import static seedu.address.logic.Messages.MESSAGE_NO_MATCH;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class FindEventCommandTest {

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindEventCommand(null));
    }

    @Test
    public void execute_noMatchingPerson_throwsCommandExceptionAndKeepsPanels() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int originalPersonCount = model.getFilteredPersonList().size();
        int originalEventCount = model.getFilteredEventList().size();
        FindEventCommand command = new FindEventCommand(
                new PersonInformation(new Name("Nobody Here"), null, null, null, null));

        assertThrows(CommandException.class, MESSAGE_NO_MATCH, () -> command.execute(model));

        assertEquals(originalPersonCount, model.getFilteredPersonList().size());
        assertEquals(originalEventCount, model.getFilteredEventList().size());
    }

    @Test
    public void execute_multipleMatchingPersons_throwsCommandExceptionAndShowsMatchesOnly() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person first = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person second = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(first);
        model.addPerson(second);

        FindEventCommand command = new FindEventCommand(
                new PersonInformation(new Name("Alex Tan"), null, null, null, null));

        assertThrows(CommandException.class, MESSAGE_MULTIPLE_MATCH, () -> command.execute(model));

        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream()
                .allMatch(person -> person.getName().equalsIgnoreCase(new Name("Alex Tan"))));
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_singleMatchingPerson_returnsEventsOverviewAndShowsOnlyMatchedPersonEvents()
            throws CommandException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person matchedPerson = model.getFilteredPersonList().stream()
                .filter(p -> p.getName().equalsIgnoreCase(new Name("Alice Pauline")))
                .findFirst()
                .orElseThrow();
        FindEventCommand command = new FindEventCommand(
                new PersonInformation(new Name("Alice Pauline"), null, null, null, null));

        CommandResult result = command.execute(model);

        assertEquals(String.format(MESSAGE_EVENTS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        matchedPerson = model.getFilteredPersonList().get(0);
        assertEquals(new Name("Alice Pauline"), matchedPerson.getName());
        assertEquals(1, model.getFilteredEventList().size());
        assertTrue(model.getFilteredEventList().stream().allMatch(matchedPerson.getEvents()::contains));
    }

    @Test
    public void execute_singleMatchingPersonByOptionalField_returnsEventsOverview() throws CommandException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person first = new PersonBuilder().withName("Ryan Lim").withPhone("81111111")
                .withEvents("Consult,2026-04-01 0900,2026-04-01 1000").build();
        Person second = new PersonBuilder().withName("Ryan Lim").withPhone("82222222")
                .withEvents("Demo,2026-04-01 1100,2026-04-01 1200").build();
        model.addPerson(first);
        model.addPerson(second);
        first.getEvents().forEach(model::addEvent);
        second.getEvents().forEach(model::addEvent);

        FindEventCommand command = new FindEventCommand(
                new PersonInformation(new Name("Ryan Lim"), new Phone("82222222"), null, null, null));

        CommandResult result = command.execute(model);

        assertEquals(String.format(MESSAGE_EVENTS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(second, model.getFilteredPersonList().get(0));
        assertEquals(1, model.getFilteredEventList().size());
        assertTrue(model.getFilteredEventList().stream().allMatch(second.getEvents()::contains));
    }

    @Test
    public void equals() {
        PersonInformation firstInfo = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        PersonInformation secondInfo = new PersonInformation(new Name("Beth Lee"), null, null, null, null);

        FindEventCommand firstCommand = new FindEventCommand(firstInfo);
        FindEventCommand secondCommand = new FindEventCommand(secondInfo);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(new FindEventCommand(firstInfo)));
        assertFalse(firstCommand.equals(1));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(secondCommand));
    }

    @Test
    public void toStringMethod() {
        PersonInformation info = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        FindEventCommand command = new FindEventCommand(info);
        String expected = FindEventCommand.class.getCanonicalName() + "{targetName=Alex Tan}";
        assertEquals(expected, command.toString());
    }
}
