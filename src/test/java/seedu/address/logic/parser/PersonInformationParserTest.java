package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class PersonInformationParserTest {

    private final PersonInformationParser parser = new PersonInformationParser();

    @Test
    public void parse_nullArgumentMultimap_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_nameOnly_success() throws Exception {
        ArgumentMultimap argsMap = tokenize(" " + PREFIX_NAME + "John Doe");

        PersonInformation parsedInfo = parser.parse(argsMap);
        PersonInformation expectedInfo = new PersonInformation(new Name("John Doe"), null, null, null, Set.of());

        assertEquals(expectedInfo, parsedInfo);
    }

    @Test
    public void parse_allFieldsPresent_success() throws Exception {
        ArgumentMultimap argsMap = tokenize(" "
                + PREFIX_NAME + "John Doe "
                + PREFIX_PHONE + "91234567 "
                + PREFIX_EMAIL + "john@example.com "
                + PREFIX_ADDRESS + "123, Clementi Ave 3 "
                + PREFIX_TAG + "friends "
                + PREFIX_TAG + "cs2103");

        PersonInformation parsedInfo = parser.parse(argsMap);
        PersonInformation expectedInfo = new PersonInformation(
                new Name("John Doe"),
                new Phone("91234567"),
                new Email("john@example.com"),
                new Address("123, Clementi Ave 3"),
                Set.of(new Tag("friends"), new Tag("cs2103")));

        assertEquals(expectedInfo, parsedInfo);
    }

    @Test
    public void parse_invalidPhone_throwsParseException() {
        ArgumentMultimap argsMap = tokenize(" "
                + PREFIX_NAME + "John Doe "
                + PREFIX_PHONE + "91ab");

        assertThrows(ParseException.class, Phone.MESSAGE_CONSTRAINTS, () -> parser.parse(argsMap));
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        ArgumentMultimap argsMap = tokenize(" "
                + PREFIX_NAME + "John Doe "
                + PREFIX_TAG + "friends "
                + PREFIX_TAG + "#invalid");

        assertThrows(ParseException.class, Tag.MESSAGE_CONSTRAINTS, () -> parser.parse(argsMap));
    }

    private static ArgumentMultimap tokenize(String args) {
        return ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);
    }
}
