package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_EMPTY_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_INVALID_EXPORT_TYPE;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_INVALID_FILENAME;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExportCommand;

public class ExportCommandParserTest {

    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, " t/all f/myContacts", new ExportCommand("all", "myContacts"));
        assertParseSuccess(parser, " t/current f/my_contacts", new ExportCommand("current", "my_contacts"));
    }

    @Test
    public void parse_exportTypeMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);

        // Missing export type prefix (t/)
        assertParseFailure(parser, " f/data", expectedMessage);

        // Missing filename prefix (f/)
        assertParseFailure(parser, " t/all", expectedMessage);
    }

    @Test
    public void parse_invalidValue_throwsParseException() {
        // Invalid export type
        assertParseFailure(parser, " t/fake f/data",
                String.format(MESSAGE_INVALID_EXPORT_TYPE, ExportCommand.MESSAGE_USAGE));

        // Duplicate export types
        assertParseFailure(parser, " t/all t/current f/testExport",
                String.format(MESSAGE_DUPLICATE_FIELDS + "t/"));

        assertParseFailure(parser, " t/all f/testExport1 f/testExport2",
                String.format(MESSAGE_DUPLICATE_FIELDS + "f/"));

        // Empty filename
        assertParseFailure(parser, " t/all f/ ",
                String.format(MESSAGE_EMPTY_FILENAME, ExportCommand.MESSAGE_USAGE));

        // Illegal characters in filename
        assertParseFailure(parser, " t/all f/data*",
                String.format(MESSAGE_INVALID_FILENAME, ExportCommand.MESSAGE_USAGE));
    }
}
