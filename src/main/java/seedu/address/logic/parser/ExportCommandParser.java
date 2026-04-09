package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.ParserUtil.arePrefixesPresent;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ExportCommand object.
 */
public class ExportCommandParser implements Parser<ExportCommand> {

    public static final Prefix PREFIX_TYPE = new Prefix("t/");
    public static final Prefix PREFIX_FILENAME = new Prefix("f/");

    public static final String MESSAGE_INVALID_EXPORT_TYPE = "Invalid export type! Use 'all' or 'current'.";
    public static final String MESSAGE_EMPTY_FILENAME = "Filename cannot be empty!";
    public static final String MESSAGE_INVALID_FILENAME = "Invalid filename! Use alphanumeric characters. ";

    /**
     * Parses the given {@code String} of arguments in the context of the {@code ExportCommand}
     * and returns an {@code ExportCommand} object for execution.
     *
     * @param args The raw user input string following the command word.
     * @return An {@code ExportCommand} initialized with the specified export type and target filename.
     * @throws ParseException If the user input does not conform to the expected format,
     *                        specifies an invalid export type, or contains illegal characters in the filename.
     */
    public ExportCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TYPE, PREFIX_FILENAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_TYPE, PREFIX_FILENAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TYPE, PREFIX_FILENAME);

        String exportType = argMultimap.getValue(PREFIX_TYPE).get().trim();
        if (!exportType.equalsIgnoreCase("all") && !exportType.equalsIgnoreCase("current")) {
            throw new ParseException(String.format(MESSAGE_INVALID_EXPORT_TYPE, ExportCommand.MESSAGE_USAGE));
        }

        String filename = argMultimap.getValue(PREFIX_FILENAME).get().trim();
        if (filename.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_FILENAME, ExportCommand.MESSAGE_USAGE));
        }

        if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new ParseException(String.format(MESSAGE_INVALID_FILENAME, ExportCommand.MESSAGE_USAGE));
        }

        return new ExportCommand(exportType, filename);
    }
}
