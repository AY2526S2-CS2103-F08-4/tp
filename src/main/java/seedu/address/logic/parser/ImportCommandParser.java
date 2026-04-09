package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.ExportCommandParser.FILENAME_VALIDATION_REGEX;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_EMPTY_FILENAME;
import static seedu.address.logic.parser.ExportCommandParser.MESSAGE_INVALID_FILENAME;
import static seedu.address.logic.parser.ParserUtil.arePrefixesPresent;

import seedu.address.logic.commands.ImportCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ImportCommand object.
 */
public class ImportCommandParser implements Parser<ImportCommand> {

    public static final Prefix PREFIX_TYPE = new Prefix("t/");
    public static final Prefix PREFIX_FILENAME = new Prefix("f/");

    public static final String MESSAGE_INVALID_IMPORT_TYPE = "Invalid import type! Use 'overwrite' or 'add'.";
    /**
     * Parses the given {@code String} of arguments in the context of the {@code ImportCommand}
     * and returns an {@code ImportCommand} object for execution.
     *
     * @param args The raw user input string following the command word.
     * @return An {@code ImportCommand} initialized with the specified import type and filename.
     * @throws ParseException If the user input does not conform to the expected format,
     *                        contains invalid characters in the filename, or specifies an unknown import type.
     */
    public ImportCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TYPE, PREFIX_FILENAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_TYPE, PREFIX_FILENAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ImportCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TYPE, PREFIX_FILENAME);

        String importType = argMultimap.getValue(PREFIX_TYPE).get().trim();
        if (!importType.equalsIgnoreCase("overwrite") && !importType.equalsIgnoreCase("add")) {
            throw new ParseException(String.format(MESSAGE_INVALID_IMPORT_TYPE, ImportCommand.MESSAGE_USAGE));
        }

        String filename = argMultimap.getValue(PREFIX_FILENAME).get().trim();
        if (filename.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_FILENAME, ImportCommand.MESSAGE_USAGE));
        }

        if (!filename.matches(FILENAME_VALIDATION_REGEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_FILENAME, ImportCommand.MESSAGE_USAGE));
        }

        return new ImportCommand(importType, filename);
    }
}
