package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.stream.Stream;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Event;

/**
 * Parses input arguments and creates a new {@link AddEventCommand} object.
 */
public class AddEventParser implements Parser<AddEventCommand> {

    private static final Prefix PREFIX_LABEL = new Prefix("l/");
    private static final Prefix PREFIX_DESCRIPTION = new Prefix("d/");
    private static final Prefix PREFIX_START = new Prefix("s/");
    private static final Prefix PREFIX_END = new Prefix("e/");
    private static final Prefix PREFIX_TO = new Prefix("to/");

    private static final String MESSAGE_NAME_LOOKUP_UNSUPPORTED =
            "Cannot resolve person by name in the parser. Use an index, or resolve name in the command.";

    /**
     * Parses the given {@code String} of arguments in the context of the AddEventCommand
     * and returns an AddEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddEventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        // Tokenize arguments and checking all required syntax is not empty & no duplicated syntax
        // for NOW it is label, start, end, and tagged person
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO);
        if (!arePrefixesPresent(argMultimap, PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO);
        // TODO: More checks are required
        String label = argMultimap.getValue(PREFIX_LABEL).get().trim();
        String description = argMultimap.getValue(PREFIX_DESCRIPTION).orElse("").trim();
        String startDateTime = argMultimap.getValue(PREFIX_START).get().trim();
        String endDateTime = argMultimap.getValue(PREFIX_END).get().trim();
        String taggedContact = argMultimap.getValue(PREFIX_TO).get().trim();
        Event event = new Event(description, startDateTime, endDateTime);

        return new AddEventCommand(taggedContact, event);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
