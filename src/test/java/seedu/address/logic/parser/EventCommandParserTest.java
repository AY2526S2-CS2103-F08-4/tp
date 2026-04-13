package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.commands.FindEventCommand;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;

public class EventCommandParserTest {

    private final EventCommandParser parser = new EventCommandParser();

    @Test
    public void parse_emptyArgs_failure() {
        assertParseFailure(parser, "   ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EventCommandParser.MESSAGE_USAGE));
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        assertParseFailure(parser, "xyz",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, EventCommandParser.MESSAGE_USAGE));
    }

    @Test
    public void parse_addSubcommandNoArgs_failure() {
        assertParseFailure(parser, "add",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_viewSubcommandNoArgs_failure() {
        assertParseFailure(parser, "view",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_addSubcommand_success() {
        Event expectedEvent = new Event(new Title("Complete feature list"),
                Optional.of(new Description("All tasks")),
                new TimeRange("2026-02-21 1100", "2026-02-21 1500"));
        PersonInformation expectedInfo = new PersonInformation(new Name("Amy Bee"), null, null, null, null);
        AddEventCommand expectedCommand = new AddEventCommand(expectedInfo, expectedEvent);

        String userInput = "add title/Complete feature list desc/All tasks start/2026-02-21 1100 "
                + "end/2026-02-21 1500 "
                + "n/Amy Bee";

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_deleteSubcommand_success() {
        PersonInformation expectedInfo = new PersonInformation(new Name("Amy Bee"), null, null, null, null);
        LocalDateTime expectedStart = LocalDateTime.parse("2026-02-21 1100", TimeRange.DATE_TIME_FORMATTER);
        DeleteEventCommand expectedCommand = new DeleteEventCommand(expectedInfo, expectedStart);

        assertParseSuccess(parser, "delete start/2026-02-21 1100 n/Amy Bee", expectedCommand);
    }

    @Test
    public void parse_viewSubcommand_success() {
        FindEventCommand expectedCommand =
                new FindEventCommand(new PersonInformation(new Name("Amy Bee"), null, null, null, null));

        assertParseSuccess(parser, "view n/Amy Bee", expectedCommand);
    }
}
