package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.event.TimeRange.MESSAGE_END_NOT_AFTER_START;
import static seedu.address.model.event.TimeRange.MESSAGE_INVALID_DATETIME_FORMAT;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;

public class AddEventParserTest {

    private static final String VALID_NAME = "Amy Bee";
    private static final String VALID_START = "2026-02-21 1100";
    private static final String VALID_END = "2026-02-21 1500";

    private final AddEventParser parser = new AddEventParser();

    @Test
    public void parse_allFieldsPresent_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME,
                expectedCommand);
    }

    @Test
    public void parse_withOptionalPhone_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), new Phone("91234567"), null, null, null);
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " p/91234567",
                expectedCommand);
    }

    @Test
    public void parse_withOptionalAddress_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null,
                new Address("Blk 123 Clementi Ave"), null);
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " a/Blk 123 Clementi Ave",
                expectedCommand);
    }

    @Test
    public void parse_withOptionalEmail_success() {
        PersonInformation expectedInfo =
                new PersonInformation(new Name(VALID_NAME), null, new Email("amy@example.com"), null, null);
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " e/amy@example.com",
                expectedCommand);
    }

    @Test
    public void parse_withoutDescription_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.empty(), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Complete feature list start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME,
                expectedCommand);
    }

    @Test
    public void parse_invalidPhone_failure() {
        assertParseFailure(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " p/notaphone",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidEmail_failure() {
        assertParseFailure(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME + " e/not-an-email",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_preamblePresent_failure() {
        assertParseFailure(parser,
                " unexpected title/Complete feature list desc/All tasks start/" + VALID_START
                        + " end/" + VALID_END + " to/" + VALID_NAME,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingToPrefix_failure() {
        assertParseFailure(parser,
                " title/Complete feature list desc/All tasks start/" + VALID_START + " end/" + VALID_END,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "  ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidDateTimeFormat_failure() {
        // Wrong date format: day/month/year instead of year-month-day
        assertParseFailure(parser,
                " title/Meeting start/25-03-2026 0900 end/25-03-2026 1000 to/" + VALID_NAME,
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_endNotAfterStart_failure() {
        // Same start and end time
        assertParseFailure(parser,
                " title/Meeting start/" + VALID_START + " end/" + VALID_START + " to/" + VALID_NAME,
                MESSAGE_END_NOT_AFTER_START);
    }

    @Test
    public void parse_endBeforeStart_failure() {
        // End is before start
        assertParseFailure(parser,
                " title/Meeting start/" + VALID_END + " end/" + VALID_START + " to/" + VALID_NAME,
                MESSAGE_END_NOT_AFTER_START);
    }

    @Test
    public void parse_invalidTitle_failure() {
        // Title contains '/' which violates Title constraints
        assertParseFailure(parser,
                " title/Meeting/Extra desc/All tasks start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME,
                Title.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidDescription_failure() {
        // Description contains '/' which violates Description constraints
        assertParseFailure(parser,
                " title/Meeting desc/invalid/desc start/" + VALID_START + " end/" + VALID_END
                        + " to/" + VALID_NAME,
                Description.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonExistentDate_failure() {
        assertParseFailure(parser,
                " title/Meeting start/2026-02-30 0900 end/2026-02-30 1000 to/" + VALID_NAME,
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_invalidEndDateTimeFormat_failure() {
        // Start is valid; only end is invalid — exercises second operand of the || condition
        assertParseFailure(parser,
                " title/Meeting start/" + VALID_START + " end/25-03-2026 1000 to/" + VALID_NAME,
                MESSAGE_INVALID_DATETIME_FORMAT);
    }

    @Test
    public void parse_emptyDescription_treatedAsNoDescription_success() {
        // desc/ present but whitespace-only — should be treated as absent (Optional.empty())
        PersonInformation expectedInfo = new PersonInformation(new Name(VALID_NAME), null, null, null, null);
        Event expectedEvent = new Event(new Title("Meeting"), Optional.empty(), new TimeRange(VALID_START, VALID_END));
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        assertParseSuccess(parser,
                " title/Meeting desc/   start/" + VALID_START + " end/" + VALID_END + " to/" + VALID_NAME,
                expectedCommand);
    }
}
