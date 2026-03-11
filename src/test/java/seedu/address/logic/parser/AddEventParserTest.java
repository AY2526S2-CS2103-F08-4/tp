package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;

public class AddEventParserTest {

    private final AddEventParser parser = new AddEventParser();

    @Test
    public void parse_missingLabelPrefix_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE);
        assertParseFailure(parser,
                "d/Complete feature list s/21-02-26 1100 e/21-02-26 1500 to/Amy Bee",
                expectedMessage);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE);
        assertParseFailure(parser,
                "l/CS2103 Meeting l/CS2103 Meeting Updated s/21-02-26 1100 e/21-02-26 1500 to/Amy Bee",
                expectedMessage);
    }
}
