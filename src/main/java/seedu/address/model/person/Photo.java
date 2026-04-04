package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's photo in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhoto(String)}
 */
public class Photo {

    public static final String MESSAGE_CONSTRAINTS = "Photos should only be in .png, .jpg, .jpeg format";
    /**
     * Path to photo must end with the extension any of the extensions .png, .jpg or .jpeg
     */
    public static final String VALIDATION_REGEX = "(?i)^.*\\.(png|jpg|jpeg)$";
    public final String value;


    /**
     * Constructs an {@code Photo}.
     *
     * @param path A valid file path to a photo.
     */
    public Photo(String path) {
        requireNonNull(path);

        // Check Extension is .png, .jpg or .jpeg
        checkArgument(isValidPhoto(path), MESSAGE_CONSTRAINTS);
        this.value = path;
    }

    /**
     * Returns true if a given string is a valid file path and the image is of the format .png, .jpg, .jpeg.
     */
    public static boolean isValidPhoto(String test) {
        return test.matches(VALIDATION_REGEX);
    }



    /**
     * Returns the path pointing to image variable.
     */
    public String getPath() {
        return this.value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Photo)) {
            return false;
        }

        Photo otherPhoto = (Photo) other;
        return value.equals(otherPhoto.value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
