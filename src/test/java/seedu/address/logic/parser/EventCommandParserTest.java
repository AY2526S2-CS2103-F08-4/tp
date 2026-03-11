package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.model.person.Event;

public class EventCommandParserTest {

    private final EventCommandParser parser = new EventCommandParser();

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "   ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        assertParseFailure(parser, "delete",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_addSubcommand_success() {
        Event expectedEvent = new Event("Complete feature list", "21-02-26 1100",
                "21-02-26 1500");
        AddEventCommand expectedCommand = new AddEventCommand("Amy Bee", expectedEvent);

        String userInput = "add l/CS2103 Meeting d/Complete feature list s/21-02-26 1100 e/21-02-26 1500 "
                + "to/Amy Bee";

        assertParseSuccess(parser, userInput, expectedCommand);
    }
}
