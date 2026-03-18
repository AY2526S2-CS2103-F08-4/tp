package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Event;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@link AddEventCommand} object.
 */
public class AddEventParser implements Parser<AddEventCommand> {

    private static final Prefix PREFIX_LABEL = new Prefix("l/");
    private static final Prefix PREFIX_DESCRIPTION = new Prefix("d/");
    private static final Prefix PREFIX_START = new Prefix("s/");
    private static final Prefix PREFIX_END = new Prefix("e/");
    private static final Prefix PREFIX_TO = new Prefix("to/");

    // Note: email disambiguation is excluded here because e/ is reserved for event end datetime.
    // Use p/, a/, t/ for contact disambiguation instead.

    /**
     * Parses the given {@code String} of arguments in the context of the AddEventCommand
     * and returns an AddEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddEventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_LABEL, PREFIX_DESCRIPTION, PREFIX_START, PREFIX_END, PREFIX_TO,
                PREFIX_PHONE, PREFIX_ADDRESS);

        // TODO: More checks are required
        // String label = argMultimap.getValue(PREFIX_LABEL).get().trim();
        String description = argMultimap.getValue(PREFIX_DESCRIPTION).orElse("").trim();
        String startDateTime = argMultimap.getValue(PREFIX_START).get().trim();
        String endDateTime = argMultimap.getValue(PREFIX_END).get().trim();
        String taggedContact = argMultimap.getValue(PREFIX_TO).get().trim();
        Event event = new Event(description, startDateTime, endDateTime);

        // Manually construct PersonInformation — cannot use PersonInformationParser here
        // because e/ is reserved for event end datetime, not email.
        try {
            Name name = ParserUtil.parseName(taggedContact);
            Phone phone = argMultimap.getValue(PREFIX_PHONE).isPresent()
                    ? ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get())
                    : null;
            Address address = argMultimap.getValue(PREFIX_ADDRESS).isPresent()
                    ? ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get())
                    : null;
            Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
            PersonInformation targetInfo = new PersonInformation(name, phone, null, address, tags);
            return new AddEventCommand(targetInfo, event);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
