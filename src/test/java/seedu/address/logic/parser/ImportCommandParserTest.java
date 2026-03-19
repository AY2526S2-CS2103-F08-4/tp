package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_EMPTY_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_INVALID_FILENAME;
import static seedu.address.logic.parser.ImportCommandParser.MESSAGE_INVALID_IMPORT_TYPE;

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

    @Test
    public void parse_missingPrefix_throwsParseException() {
        // Missing 't/' prefix
        assertParseFailure(parser, " f/myFile",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));

        // Missing 'f/' prefix
        assertParseFailure(parser, " t/add",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidValue_throwsParseException() {
        // Invalid import type
        assertParseFailure(parser, " t/invalid f/testImport",
                String.format(MESSAGE_INVALID_IMPORT_TYPE, ImportCommand.MESSAGE_USAGE));

        // Empty filename
        assertParseFailure(parser, " t/overwrite f/ ",
                String.format(MESSAGE_EMPTY_FILENAME, ImportCommand.MESSAGE_USAGE));

        // Illegal characters in filename
        assertParseFailure(parser, " t/add f/data@@@",
                String.format(MESSAGE_INVALID_FILENAME, ImportCommand.MESSAGE_USAGE));
    }
}
