package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonInformation;

/**
 * Parses input arguments and creates a new {@link FindEventCommand} object.
 */
public class FindEventParser implements Parser<FindEventCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindEventCommand
     * and returns a FindEventCommand object for execution.
     *
     * @param args the input arguments (keywords to search for in person names)
     * @return a FindEventCommand object for execution
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindEventCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        PersonInformation info = new PersonInformationParser().parse(argMultimap);
        return new FindEventCommand(info);
    }
}
