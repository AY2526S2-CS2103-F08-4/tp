package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhotoTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Photo(null));
    }

    @Test
    public void constructor_invalidPhotoPath_throwsNullPointerException() {
        String invalidPath = "virus.exe";
        assertThrows(IllegalArgumentException.class, () -> new Photo(invalidPath));
    }

    @Test
    public void equals() {
        Photo photo = new Photo("dummy.png");
        Photo otherPhoto = new Photo("friendly.png");

        // same values -> returns true
        assertTrue(photo.equals(new Photo("dummy.png")));

        // same object -> returns true
        assertTrue(photo.equals(photo));

        // null -> returns false
        assertFalse(photo.equals(null));

        // different object -> returns false
        assertFalse(photo.equals(otherPhoto));

        // invalid extension -> returns false
        assertFalse(photo.equals("socat.exe"));
    }

    @Test
    public void toStringMethod() {
        Photo photo = new Photo("test.png");
        assertEquals("test.png", photo.toString());
    }
}
