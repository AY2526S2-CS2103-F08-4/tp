package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Event description in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidDescription(String)}.
 */
public class Description {
    public static final String MESSAGE_CONSTRAINTS =
            "Description must be 1 to 1000 characters if provided, alphanumeric and spaces only, "
                    + "with some special characters (. - : ()) allowed, "
                    + "no leading/trailing spaces, and no consecutive spaces.";

    // 1-1000 chars, alphanumeric words (with . - : ( ) allowed) separated by single spaces.
    private static final String VALIDATION_REGEX =
            "^(?=.{1,1000}$)[A-Za-z0-9.()\\-:]+(?: [A-Za-z0-9.()\\-:]+)*$";

    public final String fullDescription;

    /**
     * Constructs a {@code Description}.
     *
     * @param description A valid description string.
     */
    public Description(String description) {
        requireNonNull(description);
        checkArgument(isValidDescription(description), MESSAGE_CONSTRAINTS);
        this.fullDescription = description;
    }

    /**
     * Returns true if a given string is a valid description.
     */
    public static boolean isValidDescription(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullDescription;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Description otherDescription) {
            return fullDescription.equals(otherDescription.fullDescription);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullDescription.hashCode();
    }
}
