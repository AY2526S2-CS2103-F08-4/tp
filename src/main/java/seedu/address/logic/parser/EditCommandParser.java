package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHOTO;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {
    private static final String SEGMENT_DELIMITER = " --";

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();

        int delimiterIndex = trimmedArgs.indexOf(SEGMENT_DELIMITER);
        if (delimiterIndex < 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        String targetSegment = trimmedArgs.substring(0, delimiterIndex);
        String updateSegmentRaw = trimmedArgs.substring(delimiterIndex + SEGMENT_DELIMITER.length());
        // Reject additional spaces adjacent to the delimiter.
        if (targetSegment.endsWith(" ")
                || (!updateSegmentRaw.isEmpty() && !updateSegmentRaw.startsWith(" "))
                || updateSegmentRaw.startsWith("  ")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        targetSegment = targetSegment.trim();
        String updateSegment = updateSegmentRaw.trim();

        if (targetSegment.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }
        if (updateSegment.isEmpty()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }
        
        PersonInformation targetInfo = parseTargetSegment(targetSegment);
        EditPersonDescriptor editPersonDescriptor = parseUpdateSegment(updateSegment);
        return new EditCommand(targetInfo, editPersonDescriptor);
    }

    private PersonInformation parseTargetSegment(String targetSegment) throws ParseException {
        ArgumentMultimap targetMultimap =
            ArgumentTokenizer.tokenize(" " + targetSegment, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_TAG);
        targetMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        if (!targetMultimap.getPreamble().trim().isEmpty() || targetMultimap.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        return new PersonInformationParser().parse(targetMultimap);
    }

    private EditPersonDescriptor parseUpdateSegment(String updateSegment) throws ParseException {
        ArgumentMultimap updateMultimap = tokenizeAndValidateUpdateSegment(updateSegment);
        EditPersonDescriptor editPersonDescriptor = buildEditPersonDescriptor(updateMultimap);
        parseTagsForEdit(updateMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);
        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return editPersonDescriptor;
    }

    private ArgumentMultimap tokenizeAndValidateUpdateSegment(String updateSegment) throws ParseException {
        ArgumentMultimap updateMultimap =
            ArgumentTokenizer.tokenize(" " + updateSegment, PREFIX_NAME, PREFIX_PHONE,
                PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG, PREFIX_PHOTO);
        updateMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_PHOTO);

        if (!updateMultimap.getPreamble().trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
        }

        return updateMultimap;
    }

    private EditPersonDescriptor buildEditPersonDescriptor(ArgumentMultimap updateMultimap) throws ParseException {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        if (updateMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(updateMultimap.getValue(PREFIX_NAME).get()));
        }
        if (updateMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(updateMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (updateMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(updateMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (updateMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(updateMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (updateMultimap.getValue(PREFIX_PHOTO).isPresent()) {
            editPersonDescriptor.setPhoto(ParserUtil.parsePhoto(updateMultimap.getValue(PREFIX_PHOTO).get()));
        }
        return editPersonDescriptor;
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contain only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }

        boolean allBlank = tags.stream().allMatch(String::isBlank);
        if (allBlank) {
            return Optional.of(Collections.emptySet());
        }

        return Optional.of(ParserUtil.parseTags(tags));
    }

}
