package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format! \n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid";
    public static final String MESSAGE_ONE_PERSON_LISTED_OVERVIEW = "1 matching contact found!";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d matching contacts listed!";
    public static final String MESSAGE_NO_PERSONS = "No matching contacts found!";
    public static final String MESSAGE_EVENTS_LISTED_OVERVIEW = "%1$d events listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
            "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_MULTIPLE_MATCH =
            "Multiple matches identified! Please provide more arguments.";
    public static final String MESSAGE_NO_MATCH =
            "No matches identified!";
    public static final String MESSAGE_SAVE_PHOTO_FAIL = "Error saving photo : ";
    public static final String MESSAGE_DELETE_PHOTO_FAIL = "Error deleting photo : ";
    public static final String MESSAGE_CLEAR_USER_IMAGE_FAIL = "Address book cleared, "
            + "but some images could not be deleted: ";
    public static final String MESSAGE_FAILED_OFFLINE_GUIDE = "Something went wrong with loading the offline guide: ";
    public static final String MESSAGE_MISSING_INTERNAL_RESOURCE = "Missing internal application resource: ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail().map(email -> email.toString()).orElse(""))
                .append("; Address: ")
                .append(person.getAddress().map(address -> address.toString()).orElse(""))
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        builder.append("; Photo: ")
                .append(person.getPhoto().map(photo -> photo.toString()).orElse(""));
        return builder.toString();
    }

}
