package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses fields from an {@link ArgumentMultimap} into {@link PersonInformation}.
 * Command-specific rules (e.g. required prefixes, preamble checks) should be handled by caller.
 */
public class PersonInformationParser {

    /**
     * Parses a {@code PersonInformation} from the given {@code argMultimap}.
     * Assumes {@code n/} exists.
     */
    public PersonInformation parse(ArgumentMultimap argMultimap) throws ParseException {
        requireNonNull(argMultimap);

        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());

        Optional<Phone> phone = argMultimap.getValue(PREFIX_PHONE).isPresent()
                ? Optional.of(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()))
                : Optional.empty();

        Optional<Email> email = argMultimap.getValue(PREFIX_EMAIL).isPresent()
                ? Optional.of(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()))
                : Optional.empty();

        Optional<Address> address = argMultimap.getValue(PREFIX_ADDRESS).isPresent()
                ? Optional.of(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()))
                : Optional.empty();

        Set<Tag> tags = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        return new PersonInformation(
                name,
                phone.orElse(null),
                email.orElse(null),
                address.orElse(null),
                tags
        );
    }
}
