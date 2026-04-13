package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code PinCommand}.
 */
public class PinCommandTest {

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PinCommand(null));
    }

    @Test
    public void execute_validName_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person personToPin = model.getFilteredPersonList().get(0);
        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(personToPin.getName()));

        String expectedMessage = String.format(PinCommand.MESSAGE_PIN_PERSON_SUCCESS, Messages.format(personToPin));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.pinPerson(personToPin);
        expectedModel.showNoEvents();

        assertCommandSuccess(pinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidName_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(new Name("Nobody Here")));

        assertCommandFailure(pinCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_multipleNameMatches_throwsCommandExceptionAndShowsMatches() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(new Name("Alex Tan")));

        CommandException thrown = assertThrows(CommandException.class, () -> pinCommand.execute(model));
        assertEquals(Messages.MESSAGE_MULTIPLE_MATCH, thrown.getMessage());
        assertEquals(2, model.getFilteredPersonList().size());
        assertTrue(model.getFilteredPersonList().stream()
                .allMatch(person -> person.getName().equalsIgnoreCase(new Name("Alex Tan"))));
    }

    @Test
    public void execute_sameNameDifferentPhone_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("David Ng").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("David Ng").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);

        PersonInformation info = new PersonInformation(new Name("David Ng"), new Phone("90002222"), null, null, null);
        PinCommand pinCommand = new PinCommand(info);

        String expectedMessage = String.format(PinCommand.MESSAGE_PIN_PERSON_SUCCESS, Messages.format(secondMatch));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.pinPerson(secondMatch);
        expectedModel.showNoEvents();

        assertCommandSuccess(pinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_sameNameOnePinnedOneUnpinned_pinsOnlyUnpinnedMatch() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("David Ng").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("David Ng").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);
        model.pinPerson(firstMatch);

        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(new Name("David Ng")));

        String expectedMessage = String.format(PinCommand.MESSAGE_PIN_PERSON_SUCCESS, Messages.format(secondMatch));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.pinPerson(secondMatch);
        expectedModel.showNoEvents();

        assertCommandSuccess(pinCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_alreadyPinned_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person personToPin = model.getFilteredPersonList().get(0);
        model.pinPerson(personToPin);

        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(personToPin.getName()));

        CommandException thrown = assertThrows(CommandException.class, () -> pinCommand.execute(model));
        assertTrue(thrown.getMessage().contains("already pinned"));
        assertTrue(model.isPersonPinned(personToPin));
    }

    @Test
    public void equals() {
        PersonInformation firstInfo = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        PersonInformation secondInfo = new PersonInformation(new Name("Beth Lee"), null, null, null, null);

        PinCommand pinFirstCommand = new PinCommand(firstInfo);
        PinCommand pinSecondCommand = new PinCommand(secondInfo);

        assertTrue(pinFirstCommand.equals(pinFirstCommand));
        assertTrue(pinFirstCommand.equals(new PinCommand(firstInfo)));
        assertFalse(pinFirstCommand.equals(1));
        assertFalse(pinFirstCommand.equals(null));
        assertFalse(pinFirstCommand.equals(pinSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Name targetName = new Name("Alice Pauline");
        PinCommand pinCommand = new PinCommand(createNameOnlyInfo(targetName));
        String expected = PinCommand.class.getCanonicalName() + "{targetName=" + targetName + "}";
        assertEquals(expected, pinCommand.toString());
    }

    private static PersonInformation createNameOnlyInfo(Name name) {
        return new PersonInformation(name, null, null, null, null);
    }
}
