package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DESC;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_END;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_START;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TITLE;
import static seedu.address.logic.parser.ParserUtil.arePrefixesPresent;

import java.util.Optional;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Description;
import seedu.address.model.event.Event;
import seedu.address.model.event.TimeRange;
import seedu.address.model.event.Title;
import seedu.address.model.person.PersonInformation;

/**
 * Parses input arguments and creates a new {@link AddEventCommand} object.
 */
public class AddEventParser implements Parser<AddEventCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddEventCommand
     * and returns an AddEventCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddEventCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_TITLE, PREFIX_DESC, PREFIX_START, PREFIX_END, PREFIX_NAME,
                PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_TITLE, PREFIX_START, PREFIX_END, PREFIX_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_TITLE, PREFIX_DESC, PREFIX_START, PREFIX_END,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        Event event = createEvent(argMultimap);
        try {
            PersonInformation targetInfo = new PersonInformationParser().parse(argMultimap);
            return new AddEventCommand(targetInfo, event);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddEventCommand.MESSAGE_USAGE), pe);
        }
    }

    private static Event createEvent(ArgumentMultimap argMultimap) throws ParseException {
        String titleStr = argMultimap.getValue(PREFIX_TITLE).get().trim();
        if (!Title.isValidTitle(titleStr)) {
            throw new ParseException(Title.MESSAGE_CONSTRAINTS);
        }
        Title title = new Title(titleStr);
        String descStr = argMultimap.getValue(PREFIX_DESC).map(String::trim).orElse(null);
        if (descStr != null && !descStr.isEmpty() && !Description.isValidDescription(descStr)) {
            throw new ParseException(Description.MESSAGE_CONSTRAINTS);
        }
        Optional<Description> description = (descStr != null && !descStr.isEmpty())
                ? Optional.of(new Description(descStr))
                : Optional.empty();
        String startDateTime = argMultimap.getValue(PREFIX_START).get().trim();
        String endDateTime = argMultimap.getValue(PREFIX_END).get().trim();
        if (!TimeRange.isValidDateTimeFormat(startDateTime) || !TimeRange.isValidDateTimeFormat(endDateTime)) {
            throw new ParseException(TimeRange.MESSAGE_INVALID_DATETIME_FORMAT);
        }
        try {
            TimeRange timeRange = new TimeRange(startDateTime, endDateTime);
            return new Event(title, description, timeRange);
        } catch (IllegalArgumentException e) {
            throw new ParseException(TimeRange.MESSAGE_END_NOT_AFTER_START, e);
        }
    }
}
