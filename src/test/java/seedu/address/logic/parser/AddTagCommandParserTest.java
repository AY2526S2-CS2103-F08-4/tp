package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG_ASSIGN;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddTagCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class AddTagCommandParserTest {

    private final AddTagCommandParser parser = new AddTagCommandParser();

    @Test
    public void parse_validInput_success() {
        String input = " " + PREFIX_TAG_ASSIGN + "CS2103 " + PREFIX_TAG_ASSIGN + "CS2030S "
                + PREFIX_NAME + "Alice "
                + PREFIX_NAME + "Joe " + PREFIX_TAG + "Family "
                + PREFIX_NAME + "Bob " + PREFIX_PHONE + "81234567 " + PREFIX_EMAIL + "bob@example.com "
                + PREFIX_ADDRESS + "NUS";

        AddTagCommand expected = new AddTagCommand(
                List.of(
                        new PersonInformation(new Name("Alice"), null, null, null, null),
                        new PersonInformation(new Name("Joe"), null, null, null, Set.of(new Tag("Family"))),
                        new PersonInformation(
                                new Name("Bob"),
                                new Phone("81234567"),
                                new Email("bob@example.com"),
                                new Address("NUS"),
                                null)
                ),
                Set.of(new Tag("CS2103"), new Tag("CS2030S"))
        );

        assertParseSuccess(parser, input, expected);
    }

    @Test
    public void parse_missingPersons_failure() {
        String input = " " + PREFIX_TAG_ASSIGN + "CS2103";
        assertParseFailure(parser, input,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingAssignTags_failure() {
        String input = " " + PREFIX_NAME + "Alice";
        assertParseFailure(parser, input,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonEmptyTagSectionPreamble_failure() {
        String input = " nonsense " + PREFIX_TAG_ASSIGN + "CS2103 " + PREFIX_NAME + "Alice";
        assertParseFailure(parser, input,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateAssignTags_failure() {
        String input = " " + PREFIX_TAG_ASSIGN + "CS2103 " + PREFIX_TAG_ASSIGN + "CS2103 "
                + PREFIX_NAME + "Alice";
        assertParseFailure(parser, input,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidPersonSection_failure() {
        String input = " " + PREFIX_TAG_ASSIGN + "CS2103 " + PREFIX_NAME + "Alice " + PREFIX_PHONE + "invalid";
        assertParseFailure(parser, input,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
    }
}
