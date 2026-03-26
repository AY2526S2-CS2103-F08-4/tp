package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.event.TimeRange.MESSAGE_END_NOT_AFTER_START;
import static seedu.address.model.event.TimeRange.MESSAGE_INVALID_DATETIME_FORMAT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;

public class DeleteEventParserTest {

    private static final String VALID_NAME = "Alex Yeoh";
    private static final String VALID_START = "2026-02-21 1100";
    private static final String VALID_END = "2026-02-21 1500";

    private final DeleteEventParser parser = new DeleteEventParser();

    @Test
    public void parse_allFieldsPresent_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event(new Title("Project review"), Optional.empty(),
                new TimeRange(VALID_START, VALID_END));
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Project review n/" + VALID_NAME + " start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_missingNamePrefix_failure() {
        assertParseFailure(parser,
                " title/Project review start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingStartPrefix_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingEndPrefix_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " start/" + VALID_START,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_duplicateNamePrefix_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " n/Bob start/" + VALID_START + " end/" + VALID_END,
                MESSAGE_DUPLICATE_FIELDS + "n/");
    }

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "  ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_withOptionalPhone_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), new Phone("91234567"), null, null, null);
        Event expectedEvent = new Event(new Title("Project review"), Optional.empty(),
                new TimeRange(VALID_START, VALID_END));
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Project review n/" + VALID_NAME + " p/91234567 start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalAddress_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, null, new Address("Blk 123 Clementi Ave"), null);
        Event expectedEvent = new Event(new Title("Project review"), Optional.empty(),
                new TimeRange(VALID_START, VALID_END));
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Project review n/" + VALID_NAME + " a/Blk 123 Clementi Ave "
                        + "start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalEmail_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, new Email("alex@example.com"), null, null);
        Event expectedEvent = new Event(new Title("Project review"), Optional.empty(),
                new TimeRange(VALID_START, VALID_END));
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Project review n/" + VALID_NAME + " e/alex@example.com "
                        + "start/" + VALID_START + " end/" + VALID_END,
                expectedCommand);
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " p/notaphone start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " e/not-an-email start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidTitle_failure() {
        // Title contains '/' which violates Title constraints
        assertParseFailure(parser,
                " title/Project review desc/optional n/" + VALID_NAME + " start/" + VALID_START + " end/" + VALID_END,
                Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDateTimeFormat_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " start/25-03-2026 0900 end/25-03-2026 1000",
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_nonExistentDate_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " start/2026-02-30 0900 end/2026-02-30 1000",
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_endNotAfterStart_failure() {
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " start/" + VALID_START + " end/" + VALID_START,
                MESSAGE_END_NOT_AFTER_START);
    }

    @Test
    public void parse_invalidEndDateTimeFormat_failure() {
        // Start is valid; only end is invalid — exercises second operand of the || condition
        assertParseFailure(parser,
                " title/Project review n/" + VALID_NAME + " start/" + VALID_START + " end/25-03-2026 1000",
                MESSAGE_INVALID_DATETIME_FORMAT);
    }
}
