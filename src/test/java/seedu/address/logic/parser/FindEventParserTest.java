package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class FindEventParserTest {

    private final FindEventParser parser = new FindEventParser();

    @Test
    public void parse_missingNamePrefix_failure() {
        assertParseFailure(parser, " Amy Bee",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        assertParseFailure(parser, " n/Amy Bee n/Bob Choo",
                MESSAGE_DUPLICATE_FIELDS + PREFIX_NAME);
    }

    @Test
    public void parse_duplicatePhonePrefix_failure() {
        assertParseFailure(parser, " n/Amy Bee p/91234567 p/98765432",
                MESSAGE_DUPLICATE_FIELDS + PREFIX_PHONE);
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, " Amy n/Amy Bee",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidOptionalField_failure() {
        assertParseFailure(parser, " n/Amy Bee p/abc",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validNameOnlyArgs_returnsFindEventCommand() {
        FindEventCommand expectedCommand =
                new FindEventCommand(new PersonInformation(new Name("Amy Bee"), null, null, null, null));

        assertParseSuccess(parser, " n/Amy Bee", expectedCommand);
    }

    @Test
    public void parse_validAllFieldsArgs_returnsFindEventCommand() {
        PersonInformation info = new PersonInformation(
                new Name("Amy Bee"),
                new Phone("91234567"),
                new Email("amy@example.com"),
                null,
                Set.of(new Tag("friends"), new Tag("cs2103")));
        FindEventCommand expectedCommand =
                new FindEventCommand(info);

        assertParseSuccess(parser, " n/Amy Bee p/91234567 e/amy@example.com t/friends t/cs2103", expectedCommand);
    }
}
