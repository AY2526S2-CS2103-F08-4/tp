package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Event title.
 * Guarantees: immutable; is valid as declared in {@link #isValidTitle(String)}.
 */
public class Title {
    public static final String MESSAGE_CONSTRAINTS =
            "Title must be 1 to 50 characters, alphanumeric and spaces only, "
                    + "with some special characters (. - : ()) allowed, "
                    + "no leading/trailing spaces, and no consecutive spaces.";

    private static final String VALIDATION_REGEX =
            "^(?=.{1,50}$)[A-Za-z0-9.()\\-:]+(?: [A-Za-z0-9.()\\-:]+)*$";

    public final String fullTitle;

    /**
     * Constructs a {@code Title}.
     *
     * @param title A valid title string.
     */
    public Title(String title) {
        requireNonNull(title);
        checkArgument(isValidTitle(title), MESSAGE_CONSTRAINTS);
        this.fullTitle = title;
    }

    /**
     * Returns true if a given string is a valid title.
     */
    public static boolean isValidTitle(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullTitle;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Title otherTitle) {
            return fullTitle.equals(otherTitle.fullTitle);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullTitle.hashCode();
    }
}
