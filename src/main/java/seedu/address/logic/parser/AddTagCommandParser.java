package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
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
    private static final String MESSAGE_MISSING_TARGET_PERSONS =
            "Missing target person(s). Provide at least one person starting with 'n/'.\n";

    private static final String MESSAGE_MISSING_ASSIGN_TAGS =
            "Missing tag(s) to assign. Provide at least one 'label/' value before the target person(s).\n";

    private static final String MESSAGE_INVALID_TAG_SECTION =
            "Invalid tag assignment section. Use only 'label/' prefixes before the target person(s).\n";

    private static final String MESSAGE_INVALID_TARGET_SECTION =
            "Invalid target person section. Each target person must start with 'n/' and use valid field values.\n";

    /**
     * Parses the given {@code args} into an {@link AddTagCommand}.
     * <p>
     * Expected structure:
     * tag-assignment segment (label/...) followed by one or more person segments (n/...).
     *
     * @throws ParseException if the input does not conform to command format or contains invalid values
     */
    public AddTagCommand parse(String args) throws ParseException {
        int personSectionStart = args.indexOf(" " + PREFIX_NAME.getPrefix());

        if (personSectionStart == -1) {
            checkPrefix(args);
        }

        // Splitting the commands into 2 sections, personsSection and tagsSection
        String tagsSection = args.substring(0, personSectionStart);
        String personsSection = args.substring(personSectionStart);

        Set<Tag> tagsToAssign = parseTags(tagsSection);
        List<PersonInformation> targets;
        try {
            targets = ParserUtil.parsePersons(personsSection);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    pe.getMessage() + "\n" + AddTagCommand.MESSAGE_USAGE), pe);
        }

        return new AddTagCommand(targets, tagsToAssign);
    }

    private static void checkPrefix(String args) throws ParseException {
        boolean hasTagAssignPrefix = args.contains(PREFIX_TAG_ASSIGN.getPrefix());
        boolean hasNamePrefix = args.contains(PREFIX_NAME.getPrefix());

        if (hasTagAssignPrefix && !hasNamePrefix) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_MISSING_TARGET_PERSONS + AddTagCommand.MESSAGE_USAGE));
        }

        if (hasNamePrefix && !hasTagAssignPrefix) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_MISSING_ASSIGN_TAGS + AddTagCommand.MESSAGE_USAGE));
        }

        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                AddTagCommand.MESSAGE_USAGE));
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
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_INVALID_TAG_SECTION + AddTagCommand.MESSAGE_USAGE));
        }

        List<String> tagValues = tagsMap.getAllValues(PREFIX_TAG_ASSIGN);

        if (tagValues.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_MISSING_ASSIGN_TAGS + AddTagCommand.MESSAGE_USAGE));
        }

        try {
            return ParserUtil.parseTags(tagValues);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    pe.getMessage() + "\n" + AddTagCommand.MESSAGE_USAGE), pe);
        }
    }
}
