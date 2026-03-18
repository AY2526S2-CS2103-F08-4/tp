package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;

public class AddEventParserTest {

    private static final String VALID_NAME = "Amy Bee";
    private static final String VALID_START = "21-02-26 1100";
    private static final String VALID_END = "21-02-26 1500";

    private final AddEventParser parser = new AddEventParser();

    @Test
    public void parse_allFieldsPresent_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event("Complete feature list", VALID_START, VALID_END);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalPhone_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), new Phone("91234567"), null, null, null);
        Event expectedEvent = new Event("Complete feature list", VALID_START, VALID_END);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " p/91234567",
                expectedCommand);
    }

    @Test
    public void parse_withOptionalAddress_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null,
                new Address("Blk 123 Clementi Ave"), null);
        Event expectedEvent = new Event("Complete feature list", VALID_START, VALID_END);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " a/Blk 123 Clementi Ave",
                expectedCommand);
    }

    @Test
    public void parse_withOptionalEmail_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event("Complete feature list", VALID_START, VALID_END);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " e/amy@example.com",
                expectedCommand);
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " p/notaphone",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " e/not-an-email",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_preamblePresent_failure() {
        assertParseFailure(parser,
                " unexpected d/Complete feature list start/" + VALID_START
                        + " end/" + VALID_END + " to/" + VALID_NAME,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingToPrefix_failure() {
        assertParseFailure(parser,
                " d/Complete feature list start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "  ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }
}
