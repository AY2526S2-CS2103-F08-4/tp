package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occur inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, " " + PREFIX_NAME + "John Doe",
                new DeleteCommand(new PersonInformation(new Name("John Doe"), null, null, null, null)));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "John Doe", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                DeleteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidPreamble_throwsParseException() {
        assertParseFailure(parser, " A " + PREFIX_NAME + "John Doe",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                DeleteCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidPhone_throwsParseException() {
        assertParseFailure(parser, " " + PREFIX_NAME + "John Doe " + PREFIX_PHONE + "abc",
                                "Phone numbers must be at least 7 digits and at-most 15 digits long.");
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = " " + PREFIX_NAME + "John Doe"
                + " " + PREFIX_PHONE + "98765432"
                + " " + PREFIX_EMAIL + "john@example.com"
                + " " + PREFIX_ADDRESS + "Block 123, NUS Street 1"
                + " " + PREFIX_TAG + "CS2103";

        // multiple names
        assertParseFailure(parser, " " + PREFIX_NAME + "Jane Doe" + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString
                        + " " + PREFIX_PHONE + "91234567"
                        + " " + PREFIX_EMAIL + "john2@example.com"
                        + " " + PREFIX_NAME + "John2"
                        + " " + PREFIX_ADDRESS + "Block 456, NUS Street 2",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));
    }
}
