package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates the corresponding event subcommand.
 */
public class EventCommandParser implements Parser<Command> {
    @Override
    public Command parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        String[] parts = trimmedArgs.split("\\s+", 2);
        String subcommand = parts[0];

        // need to
        switch (subcommand) {
        case AddEventCommand.COMMAND_WORD:
            return new AddEventParser().parse(args);
        default:
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }
    }
}
