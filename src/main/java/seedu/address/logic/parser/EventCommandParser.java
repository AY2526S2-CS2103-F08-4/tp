package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates the corresponding event subcommand.
 */
public class EventCommandParser implements Parser<Command> {
    public static final String MESSAGE_USAGE = "The 'event' command requires a valid subcommand.\n"
            + "Available subcommands:\n" + "1. Adding an event: event add\n" + "2. Viewing events: event view\n"
            + "3. Deleting an event: event delete\n"
            + "To see the full usage for a specific subcommand, type it without any parameters (e.g., 'event add').";

    @Override
    public Command parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE));
        }

        String[] parts = trimmedArgs.split("\\s+", 2);
        String subcommand = parts[0];

        switch (subcommand) {
        case AddEventCommand.COMMAND_WORD:
            return new AddEventParser().parse(parts.length > 1 ? " " + parts[1] : "");
        case FindEventCommand.COMMAND_WORD:
            return new FindEventParser().parse(parts.length > 1 ? " " + parts[1] : "");
        case DeleteEventCommand.COMMAND_WORD:
            return new DeleteEventParser().parse(parts.length > 1 ? " " + parts[1] : "");
        default:
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE));
        }
    }
}
