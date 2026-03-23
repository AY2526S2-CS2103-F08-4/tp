package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

public class Photo {

    public static final String MESSAGE_CONSTRAINTS = "Photos should only be in .pdf, .jpg, .jpeg format";
    public static final String DEFAULT_PHOTO_PATH = "/images/pepe-default.png";
    /**
     * Path to photo must end with the extension any of the extensions .pdf, .jpg or .jpeg
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

        // Check if provided file exists
        if (path.trim().isEmpty() || path.equals(DEFAULT_PHOTO_PATH)) {
            this.value = DEFAULT_PHOTO_PATH;
            return;
        }

        // Check Extension is .pdf .jpg or .jpeg
        checkArgument(isValidPhoto(path), MESSAGE_CONSTRAINTS);
        this.value = path;
    }

    /**
     * Returns true if a given string is a valid file path and the image is of the format .pdf, .jpg, .jpeg.
     */
    public static boolean isValidPhoto(String test) {
        return test.matches(VALIDATION_REGEX);
    }
}
