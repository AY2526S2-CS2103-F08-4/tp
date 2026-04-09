package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.event.TimeRange.MESSAGE_INVALID_DATETIME_FORMAT;
import static seedu.address.model.event.TimeRange.MESSAGE_INVALID_DATE_VALUE;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.model.event.TimeRange;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;

public class DeleteEventParserTest {

    private static final String VALID_NAME = "Alex Yeoh";
    private static final String VALID_START = "2026-02-21 1100";

    private final DeleteEventParser parser = new DeleteEventParser();

    @Test
    public void parse_requiredFieldsPresent_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        LocalDateTime expectedStart = LocalDateTime.parse(VALID_START, TimeRange.DATE_TIME_FORMATTER);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedStart);

        assertParseSuccess(parser, " n/" + VALID_NAME + " start/" + VALID_START, expectedCommand);
    }

    @Test
    public void parse_missingNamePrefix_failure() {
        assertParseFailure(parser, " start/" + VALID_START,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingStartPrefix_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " n/Bob start/" + VALID_START,
                MESSAGE_DUPLICATE_FIELDS + "n/");
    }

    @Test
    public void parse_duplicateStartPrefix_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " start/" + VALID_START + " start/2026-02-22 1000",
                MESSAGE_DUPLICATE_FIELDS + "start/");
    }

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "  ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nonEmptyPreamble_failure() {
        assertParseFailure(parser, "junk n/" + VALID_NAME + " start/" + VALID_START,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_withOptionalPhone_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), new Phone("91234567"), null, null, null);
        LocalDateTime expectedStart = LocalDateTime.parse(VALID_START, TimeRange.DATE_TIME_FORMATTER);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedStart);

        assertParseSuccess(parser, " n/" + VALID_NAME + " p/91234567 start/" + VALID_START, expectedCommand);
    }

    @Test
    public void parse_withOptionalAddress_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, new Address("Blk 123 Clementi Ave"), null);
        LocalDateTime expectedStart = LocalDateTime.parse(VALID_START, TimeRange.DATE_TIME_FORMATTER);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedStart);

        assertParseSuccess(parser, " n/" + VALID_NAME + " a/Blk 123 Clementi Ave start/" + VALID_START,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalEmail_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, new Email("alex@example.com"), null, null);
        LocalDateTime expectedStart = LocalDateTime.parse(VALID_START, TimeRange.DATE_TIME_FORMATTER);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedStart);

        assertParseSuccess(parser, " n/" + VALID_NAME + " e/alex@example.com start/" + VALID_START,
                expectedCommand);
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " p/notaphone start/" + VALID_START,
                Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " e/not-an-email start/" + VALID_START,
                Email.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidName_failure() {
        assertParseFailure(parser, " n/R@chel start/" + VALID_START,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDateTimeFormat_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " start/25-03-2026 0900",
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_nonExistentDate_failure() {
        assertParseFailure(parser, " n/" + VALID_NAME + " start/2026-02-30 0900",
                MESSAGE_INVALID_DATE_VALUE);
    }
}
