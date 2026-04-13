package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Photo;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index is not a non-zero unsigned integer.";
    public static final String MESSAGE_DUPLICATE_TAGS = "Duplicate tag specified in the command";
    public static final String MESSAGE_RESERVED_EDIT_SEGMENT_DELIMITER =
            "The token '" + EditCommandParser.EDIT_SEGMENT_DELIMITER
            + "' is reserved and cannot appear in contact fields.";
    public static final String MESSAGE_INVALID_PERSONS_FORMAT = "Invalid persons format.";
    public static final String MESSAGE_MISSING_NAME_PREFIX_IN_PERSONS =
            "Each target person must start with 'n/'.";
    public static final String MESSAGE_UNEXPECTED_PREAMBLE_IN_PERSONS =
            "Unexpected text before the first target person. Each target person must start with 'n/'.";

    /**
     * Returns true if none of the specified prefixes contain empty {@code Optional} values
     * in the given {@code ArgumentMultimap}.
     */
    public static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(String.format("Invalid tag: %s.\n%s",
                    trimmedTag, Tag.MESSAGE_CONSTRAINTS));
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            Tag parsedTag = parseTag(tagName);
            if (!tagSet.add(parsedTag)) {
                throw new ParseException(String.format("%s: %s", MESSAGE_DUPLICATE_TAGS, parsedTag.tagName));
            }
        }
        return tagSet;
    }

    /**
     * Validates that values for the specified prefixes do not contain the reserved edit delimiter token.
     *
     * @param argumentMultimap the map of parsed arguments to check
     * @param prefixes the prefixes whose values should be checked for the reserved token
     * @throws ParseException if any value for the specified prefixes contains the reserved edit delimiter token
     */
    public static void verifyNoReservedEditDelimiterInValues(ArgumentMultimap argumentMultimap,
                                                             Prefix... prefixes) throws ParseException {
        requireNonNull(argumentMultimap);
        requireNonNull(prefixes);

        for (Prefix prefix : prefixes) {
            for (String value : argumentMultimap.getAllValues(prefix)) {
                if (value.contains(EditCommandParser.EDIT_SEGMENT_DELIMITER)) {
                    throw new ParseException(MESSAGE_RESERVED_EDIT_SEGMENT_DELIMITER);
                }
            }
        }
    }

    /**
     * Parses a {@code String PHOTO} into an {@code Photo}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code photo} is invalid.
     */
    public static Photo parsePhoto(String photo) throws ParseException {
        requireNonNull(photo);
        String trimmedPhoto = photo.trim();
        if (!Photo.isValidPhoto(trimmedPhoto)) {
            throw new ParseException(Photo.MESSAGE_CONSTRAINTS);
        }
        return new Photo(trimmedPhoto);
    }

    /**
     * Parses a concatenated persons section into a list of {@link PersonInformation}.
     * <p>
     * Each person segment must start with {@code n/}. Optional prefixes that follow belong
     * to that person segment until the next {@code n/}.
     *
     * @param personsSection raw input section containing one or more person segments
     * @return ordered list of parsed person criteria
     * @throws ParseException if the section is malformed or any segment is invalid
     */
    public static List<PersonInformation> parsePersons(String personsSection) throws ParseException {
        List<Integer> nPositions = getNamePrefixesPosition(personsSection);
        if (nPositions.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_NAME_PREFIX_IN_PERSONS);
        }

        String preamble = personsSection.substring(0, nPositions.get(0)).trim();
        if (!preamble.isEmpty()) {
            throw new ParseException(MESSAGE_UNEXPECTED_PREAMBLE_IN_PERSONS);
        }

        return parseEachPerson(personsSection, nPositions);
    }

    private static List<Integer> getNamePrefixesPosition(String personsSection) {
        List<Integer> nPositions = new ArrayList<>();
        int searchFrom = 0;
        while (true) {
            int pos = personsSection.indexOf(" n/", searchFrom);
            if (pos == -1) {
                break;
            }
            nPositions.add(pos);
            searchFrom = pos + 1;
        }
        return nPositions;
    }

    /**
     * Parses each person segment identified by {@code nPos} into {@link PersonInformation}.
     *
     * @param personsSection full persons section
     * @param nPos start positions of each {@code n/} segment in {@code personsSection}
     * @return parsed targets in input order
     * @throws ParseException if any segment is invalid or no valid targets are produced
     */
    private static List<PersonInformation> parseEachPerson(String personsSection, List<Integer> nPos)
            throws ParseException {
        List<PersonInformation> targets = new ArrayList<>();
        for (int i = 0; i < nPos.size(); i++) {
            int start = nPos.get(i) + 1; // skip the leading space, begin at "n/"
            int end = (i + 1 < nPos.size()) ? nPos.get(i + 1) : personsSection.length();
            // Prepend a space so that ArgumentTokenizer recognises the leading "n/" prefix
            String segment = " " + personsSection.substring(start, end).trim();

            ArgumentMultimap segMap = ArgumentTokenizer.tokenize(
                    segment, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

            segMap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

            if (segMap.getValue(PREFIX_NAME).isEmpty()) {
                throw new ParseException(MESSAGE_MISSING_NAME_PREFIX_IN_PERSONS);
            }

            targets.add(new PersonInformationParser().parse(segMap));
        }
        // Return the list of PersonInformation of the targets provided
        if (targets.isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_PERSONS_FORMAT);
        }
        return targets;
    }
}
