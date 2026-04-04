package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
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
        Person expectedAlice = new PersonBuilder(alice).withTags("CS2030S", "CS2103").build();
        Person expectedJoe = new PersonBuilder(joe).withTags("Family", "CS2030S", "CS2103").build();
        expectedModel.setPerson(alice, expectedAlice);
        expectedModel.setPerson(joe, expectedJoe);
        expectedModel.showAllPersons();

        assertCommandSuccess(command, model,
                "Tagged 2 person(s) with [CS2030S, CS2103]: Alice, Joe", expectedModel);
    }

    @Test
    public void execute_duplicateResolvedPersons_deduplicates() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        Person alice = new PersonBuilder().withName("Alice").withPhone("90003333").build();
        model.addPerson(alice);

        AddTagCommand command = new AddTagCommand(
                List.of(info("Alice"), info("Alice")),
                Set.of(new Tag("CS2103"))
        );

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person expectedAlice = new PersonBuilder(alice).withTags("CS2103").build();
        expectedModel.setPerson(alice, expectedAlice);
        expectedModel.showAllPersons();

        assertCommandSuccess(command, model,
                "Tagged 1 person(s) with [CS2103]: Alice", expectedModel);
    }

    @Test
    public void execute_noMatch_failure() {
        Model model = new ModelManager(new AddressBook(), new UserPrefs());
        AddTagCommand command = new AddTagCommand(List.of(info("Ghost")), Set.of(new Tag("CS2103")));
        assertCommandFailure(command, model, Messages.MESSAGE_NO_MATCH);
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

        assertTrue(Messages.MESSAGE_MULTIPLE_MATCH.equals(thrown.getMessage()));
        assertTrue(model.getFilteredPersonList().size() == 2);
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
}
