package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportCommand;

public class ImportCommandParserTest {

    private final ImportCommandParser parser = new ImportCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        ImportCommand expectedCommand = new ImportCommand("overwrite", "test");
        assertParseSuccess(parser, " t/overwrite f/test", expectedCommand);
    }

    @Test
    public void parse_importTypeMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " f/test", expectedMessage);
    }
}
