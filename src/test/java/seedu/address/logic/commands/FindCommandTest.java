package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_NO_PERSONS;
import static seedu.address.logic.Messages.MESSAGE_ONE_PERSON_LISTED_OVERVIEW;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    @Test
    public void equals() {
        PersonInformation firstInfo = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        PersonInformation secondInfo = new PersonInformation(new Name("Beth Lee"), null, null, null, null);

        FindCommand findFirstCommand = new FindCommand(firstInfo);
        FindCommand findSecondCommand = new FindCommand(secondInfo);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstInfo);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void execute_noMatchingPerson_noPersonFound() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        PersonInformation info = new PersonInformation(new Name("Nobody Here"), null, null, null, null);
        FindCommand command = new FindCommand(info);
        expectedModel.updateFilteredPersonList(p -> false);
        expectedModel.updateFilteredEventList(event -> false);

        assertCommandSuccess(command, model, MESSAGE_NO_PERSONS, expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_uniqueName_onePersonFound() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        Person target = model.getAddressBook().getPersonList().stream()
                .filter(p -> p.getName().equals(new Name("Elle Meyer")))
                .findFirst()
                .orElseThrow();

        PersonInformation info = new PersonInformation(new Name("Elle Meyer"), null, null, null, null);
        FindCommand command = new FindCommand(info);
        expectedModel.updateFilteredPersonList(p -> p.equals(target));
        expectedModel.updateFilteredEventList(event -> target.getEvents().contains(event));

        assertCommandSuccess(command, model, MESSAGE_ONE_PERSON_LISTED_OVERVIEW, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(target, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_sameNameMultiplePersons_multiplePersonsFound() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(p -> p.equals(firstMatch) || p.equals(secondMatch));
        expectedModel.updateFilteredEventList(event -> false);

        FindCommand command = new FindCommand(new PersonInformation(new Name("Alex Tan"), null, null, null, null));
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(2, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_sameNameWithPhoneRefinement_onePersonFound() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(p -> p.equals(secondMatch));

        PersonInformation info = new PersonInformation(new Name("Alex Tan"), new Phone("90002222"),
                null, null, null);
        FindCommand command = new FindCommand(info);
        expectedModel.updateFilteredEventList(event -> secondMatch.getEvents().contains(event));

        assertCommandSuccess(command, model, MESSAGE_ONE_PERSON_LISTED_OVERVIEW, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(secondMatch, model.getFilteredPersonList().get(0));
    }

    @Test
    public void toStringMethod() {
        PersonInformation info = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        FindCommand findCommand = new FindCommand(info);
        String expected = FindCommand.class.getCanonicalName() + "{targetName=Alex Tan}";
        assertEquals(expected, findCommand.toString());
    }
}
