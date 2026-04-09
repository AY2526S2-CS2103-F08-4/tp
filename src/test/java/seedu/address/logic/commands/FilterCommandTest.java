package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_NO_PERSONS;
import static seedu.address.logic.Messages.MESSAGE_ONE_PERSON_LISTED_OVERVIEW;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.testutil.PersonBuilder;

public class FilterCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_noMatchingTag_returnsNoPersonsMessage() {
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("nonexistenttag"));
        FilterCommand command = new FilterCommand(predicate);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.showPersons(predicate);

        assertCommandSuccess(command, model, MESSAGE_NO_PERSONS, expectedModel);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_uniqueTag_onePersonFound() {
        model.addPerson(new PersonBuilder().withName("Unique Tag Person").withPhone("91110001")
                .withTags("uniquetag123").build());

        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("uniquetag123"));
        FilterCommand command = new FilterCommand(predicate);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.showPersons(predicate);

        assertCommandSuccess(command, model, MESSAGE_ONE_PERSON_LISTED_OVERVIEW, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_sharedTag_multiplePersonsFound() {
        // Typical address book has Alice and Benson both tagged "friends"
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("friends"));
        FilterCommand command = new FilterCommand(predicate);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.showPersons(predicate);

        int expectedCount = (int) model.getAddressBook().getPersonList().stream()
                .filter(predicate)
                .count();
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, expectedCount);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(expectedCount, model.getFilteredPersonList().size());
        assertTrue(expectedCount > 1);
    }

    @Test
    public void execute_caseInsensitiveTag_matchesRegardlessOfCase() {
        model.addPerson(new PersonBuilder().withName("Case Tag Person").withPhone("91110002")
                .withTags("CaseSensitiveTag").build());

        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("casesensitivetag"));
        FilterCommand command = new FilterCommand(predicate);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.showPersons(predicate);

        assertCommandSuccess(command, model, MESSAGE_ONE_PERSON_LISTED_OVERVIEW, expectedModel);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void execute_matchingPinnedPersonShownBeforeUnpinnedMatch() {
        Person pinnedPerson = new PersonBuilder().withName("Pinned Friend").withPhone("91110003")
                .withTags("sharedtag").build();
        Person unpinnedPerson = new PersonBuilder().withName("Unpinned Friend").withPhone("91110004")
                .withTags("sharedtag").build();

        model.addPerson(unpinnedPerson);
        model.addPerson(pinnedPerson);
        model.pinPerson(pinnedPerson);

        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("sharedtag"));
        FilterCommand command = new FilterCommand(predicate);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.showPersons(predicate);

        assertCommandSuccess(command, model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), expectedModel);
        assertEquals("Pinned Friend", model.getFilteredPersonList().get(0).getName().fullName);
        assertEquals("Unpinned Friend", model.getFilteredPersonList().get(1).getName().fullName);
    }

    @Test
    public void equals() {
        TagContainsKeywordsPredicate firstPredicate = new TagContainsKeywordsPredicate(List.of("CS2103"));
        TagContainsKeywordsPredicate secondPredicate = new TagContainsKeywordsPredicate(List.of("EG1131"));

        FilterCommand filterFirst = new FilterCommand(firstPredicate);
        FilterCommand filterSecond = new FilterCommand(secondPredicate);

        assertTrue(filterFirst.equals(filterFirst));
        assertTrue(filterFirst.equals(new FilterCommand(firstPredicate)));
        assertFalse(filterFirst.equals(filterSecond));
        assertFalse(filterFirst.equals(1));
        assertFalse(filterFirst.equals(null));
    }

    @Test
    public void toStringMethod() {
        TagContainsKeywordsPredicate predicate = new TagContainsKeywordsPredicate(List.of("CS2103"));
        FilterCommand command = new FilterCommand(predicate);
        String expected = FilterCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, command.toString());
    }
}
