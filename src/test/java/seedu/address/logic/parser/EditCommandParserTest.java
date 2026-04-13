package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHOTO_DESC;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHOTO;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHOTO;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.PersonInformation;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);
    private static final String DELIMITER = " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " ";
    private static final String TARGET_NAME_AMY = "Amy Bee";
    private static final String TARGET_NAME_BENSON = "Benson Meier";
    private static final String TARGET_NAME_CARL = "Carl Kurz";
    private static final String TARGET_IDENTIFIER_AMY = PREFIX_NAME + TARGET_NAME_AMY;
    private static final String TARGET_IDENTIFIER_BENSON = PREFIX_NAME + TARGET_NAME_BENSON;
    private static final String TARGET_IDENTIFIER_CARL = PREFIX_NAME + TARGET_NAME_CARL;

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no target name specified
        assertParseFailure(parser, " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " " + NAME_DESC_AMY,
            MESSAGE_INVALID_FORMAT);

        // no target name prefix specified
        assertParseFailure(parser, TARGET_NAME_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " "
            + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " ",
            EditCommand.MESSAGE_NOT_EDITED);

        // no target name and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // invalid name in preamble
        assertParseFailure(parser, "Amy & Bee" + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " "
            + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // numeric preamble is not a valid name
        assertParseFailure(parser, "0" + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " " + NAME_DESC_AMY,
            MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "i/ string" + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " "
            + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // only whitespace preamble
        assertParseFailure(parser, "   " + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " " + NAME_DESC_AMY,
            MESSAGE_INVALID_FORMAT);

        // non-empty preamble before prefixed target name
        assertParseFailure(parser, "oops " + TARGET_IDENTIFIER_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER
            + " " + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // non-empty preamble with valid delimiter spacing (reaches target-segment preamble branch)
        assertParseFailure(parser, "oops " + TARGET_IDENTIFIER_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER
            + " " + PREFIX_NAME + "Bob", MESSAGE_INVALID_FORMAT);

        // missing target name prefix (empty preamble but no n/ value)
        assertParseFailure(parser, PREFIX_PHONE + "98765432" + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " "
            + NAME_DESC_AMY.trim(), MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidDelimiterSpacing_failure() {
        // no space before delimiter
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + EditCommandParser.EDIT_SEGMENT_DELIMITER
            + " " + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // no space after delimiter
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER
            + NAME_DESC_AMY.trim(), MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_multipleSpacesAroundDelimiter_failure() {
        String userInput = TARGET_IDENTIFIER_AMY + "   " + EditCommandParser.EDIT_SEGMENT_DELIMITER + "    "
            + PHONE_DESC_AMY;
        assertParseFailure(parser, userInput, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_reservedDelimiterTokenInFields_failure() {
        String userInput = TARGET_IDENTIFIER_AMY + " a/Block 50 >> West Coast"
            + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " " + PHONE_DESC_AMY.trim();
        assertParseFailure(parser, userInput, MESSAGE_INVALID_FORMAT);

        userInput = TARGET_IDENTIFIER_AMY + " " + EditCommandParser.EDIT_SEGMENT_DELIMITER + " "
            + "a/Block 50 >> West Coast";
        assertParseFailure(parser, userInput, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateTargetSingleValuedPrefixes_failure() {
        String userInput = TARGET_IDENTIFIER_AMY + PHONE_DESC_AMY + PHONE_DESC_BOB + DELIMITER
            + EMAIL_DESC_AMY.trim();
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        userInput = TARGET_IDENTIFIER_AMY + EMAIL_DESC_AMY + EMAIL_DESC_BOB + DELIMITER + PHONE_DESC_AMY.trim();
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        userInput = TARGET_IDENTIFIER_AMY + ADDRESS_DESC_AMY + ADDRESS_DESC_BOB + DELIMITER
            + PHONE_DESC_AMY.trim();
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_nonPrefixedUpdatePreamble_failure() {
        String userInput = TARGET_IDENTIFIER_AMY + DELIMITER + "oops " + PHONE_DESC_AMY.trim();
        assertParseFailure(parser, userInput, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_NAME_DESC.trim(),
            Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_PHONE_DESC.trim(),
            Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_EMAIL_DESC.trim(),
            Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_TAG_DESC.trim(),
            String.format("Invalid tag: %s.\n%s", "hubby*", Tag.MESSAGE_CONSTRAINTS));

        // invalid phone followed by valid email
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_PHONE_DESC.trim() + EMAIL_DESC_AMY,
                Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + TAG_DESC_FRIEND.trim()
            + TAG_DESC_HUSBAND + TAG_EMPTY,
            String.format("Invalid tag: .\n%s", Tag.MESSAGE_CONSTRAINTS));
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + TAG_DESC_FRIEND.trim()
            + TAG_EMPTY + TAG_DESC_HUSBAND,
            String.format("Invalid tag: .\n%s", Tag.MESSAGE_CONSTRAINTS));
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + TAG_EMPTY.trim()
            + TAG_DESC_FRIEND + TAG_DESC_HUSBAND,
            String.format("Invalid tag: .\n%s", Tag.MESSAGE_CONSTRAINTS));

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_NAME_DESC.trim() + INVALID_EMAIL_DESC
                + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);

        // invalid values in target segment report field constraints directly
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + INVALID_PHONE_DESC + DELIMITER + NAME_DESC_AMY.trim(),
            Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + INVALID_EMAIL_DESC + DELIMITER + NAME_DESC_AMY.trim(),
            Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, TARGET_IDENTIFIER_AMY + INVALID_ADDRESS_DESC + DELIMITER + NAME_DESC_AMY.trim(),
            Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        String userInput = TARGET_IDENTIFIER_BENSON + PHONE_DESC_BOB + TAG_DESC_HUSBAND + DELIMITER
            + PHONE_DESC_BOB.trim() + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
            .withTags(VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_BENSON), new Phone(VALID_PHONE_BOB), null,
                        null, Set.of(new Tag(VALID_TAG_HUSBAND))), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);

        userInput = TARGET_IDENTIFIER_AMY + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND
            + DELIMITER + NAME_DESC_AMY.trim();
        descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_AMY), new Phone(VALID_PHONE_BOB),
                new Email(VALID_EMAIL_BOB), new Address(VALID_ADDRESS_BOB),
                Set.of(new Tag(VALID_TAG_HUSBAND))), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        String userInput = TARGET_IDENTIFIER_AMY + DELIMITER + PHONE_DESC_BOB.trim() + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_AMY), null, null, null, null), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        String userInput = TARGET_IDENTIFIER_CARL + DELIMITER + NAME_DESC_AMY.trim();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = TARGET_IDENTIFIER_CARL + DELIMITER + PHONE_DESC_AMY.trim();
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = TARGET_IDENTIFIER_CARL + DELIMITER + EMAIL_DESC_AMY.trim();
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = TARGET_IDENTIFIER_CARL + DELIMITER + ADDRESS_DESC_AMY.trim();
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = TARGET_IDENTIFIER_CARL + DELIMITER + TAG_DESC_FRIEND.trim();
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // photo
        userInput = TARGET_IDENTIFIER_CARL + DELIMITER + PHOTO_DESC.trim();
        descriptor = new EditPersonDescriptorBuilder().withPhoto(VALID_PHOTO).build();
        expectedCommand = new EditCommand(
            new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        String userInput = TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_PHONE_DESC.trim() + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = TARGET_IDENTIFIER_AMY + DELIMITER + PHONE_DESC_BOB.trim() + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // mulltiple valid fields repeated
        userInput = TARGET_IDENTIFIER_AMY + DELIMITER + PHONE_DESC_AMY.trim() + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple invalid values
        userInput = TARGET_IDENTIFIER_AMY + DELIMITER + INVALID_PHONE_DESC.trim()
            + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_resetTags_success() {
        String userInput = TARGET_IDENTIFIER_CARL + DELIMITER + TAG_EMPTY.trim();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_clearAddress_success() {
        String userInput = TARGET_IDENTIFIER_CARL + DELIMITER + PREFIX_ADDRESS;

        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setAddress(null);
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_clearEmail_success() {
        String userInput = TARGET_IDENTIFIER_CARL + DELIMITER + PREFIX_EMAIL;

        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setEmail(null);
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_clearPhoto_success() {
        String userInput = TARGET_IDENTIFIER_CARL + DELIMITER + PREFIX_PHOTO;

        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setPhoto(null);
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_CARL), null, null, null, null), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_mixedCaseTagValues_success() {
        String mixedCaseTargetTag = "HuSbAnD";
        String mixedCaseUpdateTag = "FrIeNd";
        String userInput = TARGET_IDENTIFIER_BENSON + " t/" + mixedCaseTargetTag
            + DELIMITER + "t/" + mixedCaseUpdateTag;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags(mixedCaseUpdateTag).build();
        EditCommand expectedCommand = new EditCommand(
                new PersonInformation(new Name(TARGET_NAME_BENSON), null, null,
                        null, Set.of(new Tag(mixedCaseTargetTag))), descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_duplicateTags_failure() {
        // duplicate update tags are rejected directly
        String userInput = TARGET_IDENTIFIER_AMY + DELIMITER + TAG_DESC_FRIEND.trim() + TAG_DESC_FRIEND;
        assertParseFailure(parser, userInput,
                String.format("%s: %s", ParserUtil.MESSAGE_DUPLICATE_TAGS, VALID_TAG_FRIEND));

        // duplicate update tags are case-insensitive
        userInput = TARGET_IDENTIFIER_AMY + DELIMITER + TAG_DESC_FRIEND.trim() + " t/FRIEND";
        assertParseFailure(parser, userInput,
                String.format("%s: %s", ParserUtil.MESSAGE_DUPLICATE_TAGS, "friend"));

        // duplicate target tags are rejected directly
        userInput = TARGET_IDENTIFIER_BENSON + TAG_DESC_HUSBAND + " t/HUSBAND" + DELIMITER + PHONE_DESC_AMY.trim();
        assertParseFailure(parser, userInput,
                String.format("%s: %s", ParserUtil.MESSAGE_DUPLICATE_TAGS, "husband"));
    }

    @Test
    public void parseUpdateSegment_noEditableFields_throwsParseException() throws Exception {
        Method parseUpdateSegment = EditCommandParser.class.getDeclaredMethod("parseUpdateSegment", String.class);
        parseUpdateSegment.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, (
                ) -> parseUpdateSegment.invoke(parser, "   "));

        ParseException cause = (ParseException) thrown.getCause();
        org.junit.jupiter.api.Assertions.assertEquals(EditCommand.MESSAGE_NOT_EDITED, cause.getMessage());
    }
}
