package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG_ASSIGN;

import java.util.List;
import java.util.Set;

import seedu.address.logic.commands.AddTagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates an {@link AddTagCommand}.
 */
public class AddTagCommandParser implements Parser<AddTagCommand> {

    /**
     * Parses the given {@code args} into an {@link AddTagCommand}.
     * <p>
     * Expected structure:
     * tag-assignment segment (label/...) followed by one or more person segments (n/...).
     *
     * @throws ParseException if the input does not conform to command format or contains invalid values
     */
    public AddTagCommand parse(String args) throws ParseException {
        // Splitting the Person segment and Tag segment
        int personSectionStart = args.indexOf(" n/");
        if (personSectionStart == -1) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
        }
        // Splitting the commands into 2 sections, personsSection and tagsSection
        String tagsSection = args.substring(0, personSectionStart);
        String personsSection = args.substring(personSectionStart);

        // Parsing Tag segment
        Set<Tag> tagsToAssign = parseTags(tagsSection);
        // Parsing Person segment
        List<PersonInformation> targets;
        try {
            targets = ParserUtil.parsePersons(personsSection);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE), pe);
        }

        return new AddTagCommand(targets, tagsToAssign);
    }

    /**
     * Parses the tag-assignment segment and returns the set of tags to apply.
     *
     * @param tagsSection input section containing only {@code label/...} prefixes
     * @throws ParseException if preamble text exists, no tags are provided, or tag values are invalid
     */
    private Set<Tag> parseTags(String tagsSection) throws ParseException {
        ArgumentMultimap tagsMap = ArgumentTokenizer.tokenize(tagsSection, PREFIX_TAG_ASSIGN);
        if (!tagsMap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
        }
        List<String> tagValues = tagsMap.getAllValues(PREFIX_TAG_ASSIGN);
        if (tagValues.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE));
        }
        Set<Tag> tagsToAssign;
        try {
            tagsToAssign = ParserUtil.parseTags(tagValues);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTagCommand.MESSAGE_USAGE), pe);
        }
        return tagsToAssign;
    }
}
