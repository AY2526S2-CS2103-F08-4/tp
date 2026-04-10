package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.ParserUtil.arePrefixesPresent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TagContainsKeywordsPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FilterCommand object
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FilterCommand
     * and returns a FilterCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FilterCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        checkingTagPrefixes(argMultimap);

        String allTags = argMultimap.getValue(PREFIX_TAG).get();

        if (allTags.isBlank()) {
            throw new ParseException("Error: Tag value cannot be empty.\n"
                    + "Fix: Provide a valid tag name after 't/' (e.g. filter t/CS2103 Group)");
        }

        List<String> tagKeywords = Arrays.stream(allTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());

        for (String tag : tagKeywords) {
            if (!tag.matches(Tag.VALIDATION_REGEX)) {
                throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
            }
        }

        return new FilterCommand(new TagContainsKeywordsPredicate(tagKeywords));
    }

    private static void checkingTagPrefixes(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getAllValues(PREFIX_TAG).size() > 1) {
            throw new ParseException("Please provide all tags after a single 't/' prefix, separated by commas.");
        }

        if (!arePrefixesPresent(argMultimap, PREFIX_TAG)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    "Missing required tag prefix 't/'.\n" + FilterCommand.MESSAGE_USAGE));
        }

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
        }
    }
}


