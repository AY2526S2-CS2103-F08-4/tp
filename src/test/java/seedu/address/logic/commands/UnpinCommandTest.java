package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
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
 * Contains integration tests (interaction with the Model) and unit tests for {@code UnpinCommand}.
 */
public class UnpinCommandTest {

    @Test
    public void constructor_nullTargetInfo_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnpinCommand(null));
    }

    @Test
    public void execute_validPinnedName_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person personToUnpin = model.getFilteredPersonList().get(0);
        model.pinPerson(personToUnpin);

        UnpinCommand unpinCommand = new UnpinCommand(createNameOnlyInfo(personToUnpin.getName()));

        String expectedMessage = String.format(UnpinCommand.MESSAGE_UNPIN_PERSON_SUCCESS,
                Messages.format(personToUnpin));

        try {
            CommandResult result = unpinCommand.execute(model);
            assertEquals(new CommandResult(expectedMessage), result);
            assertFalse(model.isPersonPinned(personToUnpin));
        } catch (CommandException ce) {
            throw new AssertionError("Execution of command should not fail.", ce);
        }
    }

    @Test
    public void execute_unpinnedPerson_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person person = model.getFilteredPersonList().get(0);
        UnpinCommand unpinCommand = new UnpinCommand(createNameOnlyInfo(person.getName()));

        assertCommandFailure(unpinCommand, model, UnpinCommand.MESSAGE_ALREADY_UNPINNED);
    }

    @Test
    public void execute_invalidName_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        UnpinCommand unpinCommand = new UnpinCommand(createNameOnlyInfo(new Name("Nobody Here")));

        assertCommandFailure(unpinCommand, model, Messages.MESSAGE_NO_MATCH);
    }

    @Test
    public void execute_multipleNameMatches_throwsCommandExceptionAndShowsMatches() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("Alex Tan").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("Alex Tan").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);
        model.pinPerson(firstMatch);
        model.pinPerson(secondMatch);

        UnpinCommand unpinCommand = new UnpinCommand(createNameOnlyInfo(new Name("Alex Tan")));

        CommandException thrown = assertThrows(CommandException.class, () -> unpinCommand.execute(model));
        assertEquals(Messages.MESSAGE_MULTIPLE_MATCH, thrown.getMessage());
    }

    @Test
    public void execute_sameNameDifferentPhone_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person firstMatch = new PersonBuilder().withName("David Ng").withPhone("90001111").build();
        Person secondMatch = new PersonBuilder().withName("David Ng").withPhone("90002222").build();
        model.addPerson(firstMatch);
        model.addPerson(secondMatch);
        model.pinPerson(secondMatch);

        PersonInformation info = new PersonInformation(new Name("David Ng"), new Phone("90002222"), null, null, null);
        UnpinCommand unpinCommand = new UnpinCommand(info);

        String expectedMessage = String.format(UnpinCommand.MESSAGE_UNPIN_PERSON_SUCCESS,
                Messages.format(secondMatch));

        try {
            CommandResult result = unpinCommand.execute(model);
            assertEquals(new CommandResult(expectedMessage), result);
            assertFalse(model.isPersonPinned(secondMatch));
        } catch (CommandException ce) {
            throw new AssertionError("Execution of command should not fail.", ce);
        }
    }

    @Test
    public void equals() {
        PersonInformation firstInfo = new PersonInformation(new Name("Alex Tan"), null, null, null, null);
        PersonInformation secondInfo = new PersonInformation(new Name("Beth Lee"), null, null, null, null);

        UnpinCommand unpinFirstCommand = new UnpinCommand(firstInfo);
        UnpinCommand unpinSecondCommand = new UnpinCommand(secondInfo);

        assertTrue(unpinFirstCommand.equals(unpinFirstCommand));
        assertTrue(unpinFirstCommand.equals(new UnpinCommand(firstInfo)));
        assertFalse(unpinFirstCommand.equals(1));
        assertFalse(unpinFirstCommand.equals(null));
        assertFalse(unpinFirstCommand.equals(unpinSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Name targetName = new Name("Alice Pauline");
        UnpinCommand unpinCommand = new UnpinCommand(createNameOnlyInfo(targetName));
        String expected = UnpinCommand.class.getCanonicalName() + "{targetName=" + targetName + "}";
        assertEquals(expected, unpinCommand.toString());
    }

    private static PersonInformation createNameOnlyInfo(Name name) {
        return new PersonInformation(name, null, null, null, null);
    }
}
