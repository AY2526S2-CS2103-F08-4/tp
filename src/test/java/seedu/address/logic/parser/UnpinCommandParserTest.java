package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnpinCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class UnpinCommandParserTest {

    private final UnpinCommandParser parser = new UnpinCommandParser();

    @Test
    public void parse_missingNamePrefix_throwsParseException() {
        assertParseFailure(parser, "     ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnpinCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonEmptyPreamble_throwsParseException() {
        assertParseFailure(parser, "Alex " + PREFIX_NAME + "Alex Tan",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnpinCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidOptionalField_throwsParseException() {
        assertParseFailure(parser, " " + PREFIX_NAME + "Alex Tan " + PREFIX_PHONE + "not-a-phone",
                                Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = " " + PREFIX_NAME + "Alex Tan"
                + " " + PREFIX_PHONE + "98765432"
                + " " + PREFIX_EMAIL + "alex@example.com"
                + " " + PREFIX_ADDRESS + "Block 123, NUS Street 1"
                + " " + PREFIX_TAG + "CS2103";

        assertParseFailure(parser, " " + PREFIX_NAME + "Alice" + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        assertParseFailure(parser,
                validExpectedPersonString
                        + " " + PREFIX_PHONE + "91234567"
                        + " " + PREFIX_EMAIL + "alex2@example.com"
                        + " " + PREFIX_NAME + "Alex2"
                        + " " + PREFIX_ADDRESS + "Block 456, NUS Street 2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));
    }

    @Test
    public void parse_nameOnly_success() {
        UnpinCommand expectedUnpinCommand =
                new UnpinCommand(new PersonInformation(new Name("Alex Tan"), null, null, null, null));
        assertParseSuccess(parser, " " + PREFIX_NAME + "Alex Tan", expectedUnpinCommand);
    }

    @Test
    public void parse_allFields_success() {
        PersonInformation info = new PersonInformation(
                new Name("Alex Tan"),
                new Phone("98765432"),
                new Email("alex@example.com"),
                new Address("Block 123, NUS Street 1"),
                Set.of(new Tag("CS2103"), new Tag("Lab"))
        );
        UnpinCommand expectedUnpinCommand = new UnpinCommand(info);

        assertParseSuccess(parser,
                " " + PREFIX_NAME + "Alex Tan"
                        + " " + PREFIX_PHONE + "98765432"
                        + " " + PREFIX_EMAIL + "alex@example.com"
                        + " " + PREFIX_ADDRESS + "Block 123, NUS Street 1"
                        + " " + PREFIX_TAG + "CS2103"
                        + " " + PREFIX_TAG + "Lab",
                expectedUnpinCommand);
    }
}

